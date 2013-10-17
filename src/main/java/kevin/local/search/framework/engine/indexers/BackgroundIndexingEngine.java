package kevin.local.search.framework.engine.indexers;

import kevin.local.search.framework.SearcherBootstrap;
import local.kevin.data.data.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 12:45 PM
 */


public class BackgroundIndexingEngine<T> implements Runnable, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BackgroundIndexingEngine.class);

    private final ScheduledExecutorService executor;
    private final List<BackgroundIndexer<T>> indexers;
    private final IndexWriter indexWriter;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final BlockingQueue<T> updates;
    private final SearcherBootstrap<T> bootstrap;
    private final Analyzer analyzer;

    public BackgroundIndexingEngine(SearcherBootstrap<T> bootstrap, Directory directory, Analyzer analyzer, List<BackgroundIndexer<T>> indexers) throws IOException {
        this.bootstrap = bootstrap;
        executor = Executors.newSingleThreadScheduledExecutor(makeThreadFactory());
        this.analyzer = analyzer;
        indexWriter = new IndexWriter(directory,
                new IndexWriterConfig(Version.LUCENE_40, analyzer));

        indexReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(indexReader);

        this.indexers = Collections.synchronizedList(new LinkedList<BackgroundIndexer<T>>());
        if (indexers != null) this.indexers.addAll(indexers);
        updates = new LinkedBlockingQueue<>();
    }

    private static ThreadFactory makeThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread ret = Executors.defaultThreadFactory().newThread(r);
                ret.setDaemon(true);
                ret.setName("SummingEngine");
                return ret;
            }
        };
    }
    public void startUp(boolean isDaemon) {
        Thread engineThread = new Thread(this, "BackgroundIndexingEngine-Main-SelfStart");
        Thread actionThread = new Thread(new ActionTaker(), "BackgroundIndexingEngine-ActionTaker-SelfStart");
        engineThread.setDaemon(isDaemon);
        actionThread.setDaemon(isDaemon);
        engineThread.start();
        actionThread.start();
    }

    //private final SnapshotListBuilder<T, IndexingEventResponse> listBuilder = new SnapshotListBuilder<>();
    private final BlockingDeque<Pair<T, IndexingEventResponse>> actionQueue = new LinkedBlockingDeque<>();

    class ActionTaker implements Runnable {
        @Override
        public void run() {
            while (!executor.isTerminated() || !actionQueue.isEmpty()) {
                try {
                    Pair<T, IndexingEventResponse> nextAction = actionQueue.poll(15, TimeUnit.SECONDS);

                    if (nextAction == null) {
                        //Timeout was hit
                        continue;
                    }
                    else if (nextAction.getRight().getAction() == IndexingEventResponse.Action.DO_NOTHING) {
                        continue;
                    }
                    else if (nextAction.getRight().getAction() == IndexingEventResponse.Action.DELETE_DOC) {
                        Map<String, String> compositeId = bootstrap.pullId(nextAction.getLeft());
                        Query q = Indexers.makeIdQuery(compositeId);
                        indexWriter.deleteDocuments(q);

                    }
                    else if (nextAction.getRight().getAction() == IndexingEventResponse.Action.CONTRIBUTE) {
                        Indexers.contribute(bootstrap, indexSearcher, indexWriter, bootstrap.pullId(nextAction.getLeft()), nextAction.getRight().getFields(), false, false, analyzer);
                    }

                }
                catch (InterruptedException e) {
                    log.error("Interrupted in ActionTaker", e);
                    break;
                }
                catch (IOException e) {
                    log.error("Error performing action in ActionTaker", e);
                }

            }

        }
    }

    public void run() {
        try {
            while (!executor.isShutdown()) {
                final T update = updates.poll(15, TimeUnit.SECONDS);
                for (BackgroundIndexer<T> indexer : indexers) {
                        Runnable toQueue = indexer.createResponder(update, null, new CompletionHandler<IndexingEventResponse, Void>() {
                            @Override
                            public void completed(IndexingEventResponse result, Void attachment) {
                                actionQueue.push(new Pair<>(update, result));
                            }

                            @Override
                            public void failed(Throwable exc, Void attachment) {
                                log.error("Background Indexing Error", exc);
                            }
                        });
                    executor.submit(toQueue);
                }
            }
        }
        catch (InterruptedException e) {
            log.error("Background Indexing Engine Interrupted!", e);
        }

    }
    public void triggerUpdaters(final T rep, long delay, TimeUnit timeUnit) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                triggerUpdaters(rep);
            }
        }, delay, timeUnit);

    }
    public void triggerUpdaters(T rep) {
        updates.add(rep);
    }

    public void shutdown() {
        executor.shutdown();
    }
    private boolean awaitTermination() throws InterruptedException {
        return awaitTermination(1, TimeUnit.MINUTES);
    }
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
    public void close() throws InterruptedException, IOException {
        if (!executor.isShutdown()) {
            shutdown();
            awaitTermination();
        }
        indexWriter.close();
    }
}
