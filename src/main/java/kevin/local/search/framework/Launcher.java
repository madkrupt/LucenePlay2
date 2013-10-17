package kevin.local.search.framework;

import kevin.local.search.framework.engine.indexers.*;
import kevin.local.search.framework.gui.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/10/12
 * Time: 1:35 PM
 */
public class Launcher<T> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Launcher.class);

    private static final int REINDEX_HARD_LIMIT = 1000;


    private static final boolean REBUILD = true;
    private static final boolean USE_FILE = true;
    //private static final boolean BACKGROUND_INDEXING_ENABLED = true;


    private final SearcherBootstrap<T> bootstrap;
    private final Directory directory;
    private final Analyzer analyzer;
    private BackgroundIndexingEngine<T> backEngine = null;

    private static Directory makeDirectory(final SearcherBootstrap bootstrap) throws IOException {
        if (USE_FILE || !REBUILD) {
            return FSDirectory.open(new File("temp/" + bootstrap.getName() + ".indx"));
        }
        return new RAMDirectory();
    }

    public Launcher(final SearcherBootstrap bootstrap) throws IOException, ParseException {
        this.bootstrap = bootstrap;
        this.directory = makeDirectory(bootstrap);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    directory.close();
                } catch (IOException e) {
                    log.error("Failure Attempting To Close Directory", e);
                }
            }
        });

        analyzer = new StandardAnalyzer(Version.LUCENE_40);
        if (REBUILD) {
            System.out.println("Building Index...");
            try {
                bootstrap.getIndexItemSource().indexSource(directory, analyzer, bootstrap.getForegroundIndexers());
            } catch (IOException | IndexerException e) {
                throw new IOException("Failed to Build Index", e);
            }
            System.out.println("Initial Index Build Complete");
        }
    }

    @Deprecated
    public void showGUI(boolean waitForExit) {
        TableGUIFrame tableGUIFrame = new TableGUIFrame<>(bootstrap, directory, analyzer, backEngine);
        if (waitForExit) tableGUIFrame.waitForExit(true);
    }

    public synchronized BackgroundIndexingEngine<T> enableBackgroundIndexing() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (backEngine == null) return;
                try {
                    backEngine.shutdown();
                    log.info("Shutting Down Background Indexing");
                    while (!backEngine.awaitTermination(15, TimeUnit.SECONDS)) {
                        log.info("Waiting for Background Indexing...");
                    }
                    backEngine.close();
                    log.info("Background Indexing has been shutdown");
                } catch (Exception e1) {
                    log.error("Error shutting down background indexing", e1);
                }
            }
        });

        return null;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }



    public BackgroundIndexingEngine<T> getBackEngine() {
        return backEngine;
    }

    private synchronized void enableBackgroundIndexing(final SearcherBootstrap<T> bootstrap) throws IOException {
        log.info("Starting Background Indexing");
        List<BackgroundIndexer<T>> backIndexers = bootstrap.getBackgroundIndexers();
        backEngine = new BackgroundIndexingEngine<T>(bootstrap, directory, analyzer, backIndexers);
        backEngine.startUp(false);

        DirectoryReader reader = DirectoryReader.open(directory);

                        /*
                            Because of the way the documentations states deletions occur with index readers
                            the functionality of the code below is technically 'undefined' but as long as
                            it runs somewhat reliably from the api it should work, i tried to cover all the
                            bases here....

                         */

        final Set<String> uniquePaths = new HashSet<>();
        final AtomicInteger added = new AtomicInteger(0);

        for (int i = 0; i < reader.maxDoc(); i++) {
            try {
                final Map<String, String> docId = new HashMap<>();
                reader.document(i, new StoredFieldVisitor() {
                    @Override
                    public Status needsField(FieldInfo fieldInfo) throws IOException {
                        if ((fieldInfo != null) && bootstrap.getIdFields().contains(fieldInfo.name)) {
                            return Status.YES;
                        }
                        return Status.NO;
                    }

                    @Override
                    public void stringField(FieldInfo fieldInfo, String value) throws IOException {
                        //since only 1 type of field should be returned lets not check the name, besides
                        // if its a problem it still shouldn't "break" the code
                        if ((fieldInfo != null) && (value != null)) {
                            if (added.incrementAndGet() < REINDEX_HARD_LIMIT && uniquePaths.add(value))
                                docId.put(fieldInfo.name, value);
                        }
                    }
                });
                backEngine.triggerUpdaters(bootstrap.from(docId));
            } catch (Throwable t) {
                log.warn("Error with initial index refresh", t);
            }
        }
        if (added.get() > uniquePaths.size()) {
            log.warn(String.format("Background indexing started (%s duplicates noticed)", added.get() - uniquePaths.size()));

        }
        else {
            log.info("Background indexing started (no duplicates detected)");
        }
    }

    private synchronized BackgroundIndexingEngine<T> getBackgroundEngine() {
        return backEngine;
    }

    public Directory getDirectory() {
        return directory;
    }
}
