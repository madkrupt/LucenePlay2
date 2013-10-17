package kevin.local.search.framework;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/8/12
 * Time: 2:25 PM
 */                   
public class SimpleSearcher implements SearcherBootstrap.Searcher {
    private static final Logger log = LoggerFactory.getLogger(SimpleSearcher.class);

    private static final int DEFAULT_LIMIT = 25;

    private final SearcherManager sm;

    private final QueryParser parser;

    private volatile int refreshCount = 0;


    public SimpleSearcher(Directory directory, Analyzer analyzer, String defaultField) throws IOException {
        this.sm = new SearcherManager(directory, new SearcherFactory());

        parser = new QueryParser(Version.LUCENE_40, defaultField, analyzer);

        parser.setAllowLeadingWildcard(true);
    }
    private List<Document> search(String inLine, int limit, boolean forceRefresh) throws ParseException, IOException {
        if (inLine.trim().isEmpty()) return Collections.emptyList();
        if (forceRefresh) {
            sm.maybeRefreshBlocking();
        }
        else if (sm.isSearcherCurrent() || (++refreshCount % 10 == 0)) {
            sm.maybeRefresh();
        }

        List<Document> ret = new LinkedList<>();
        Query query = parser.parse(inLine);
        log.trace("Searching for: " + query.toString());


        IndexSearcher searcher = sm.acquire();
        try {
            TopDocs topDocs = searcher.search(query, limit);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                ret.add(searcher.doc(scoreDoc.doc));
            }
            return ret;
        }
        finally {
            sm.release(searcher);
        }
    }

    @Override
    public <A> void searchData(String inLine, int limit, boolean forceRefresh, A attachment, CompletionHandler<List<Document>, A> handler) {
        List<Document> ret;
        try {
            ret = search(inLine, limit, forceRefresh);
            handler.completed(ret, attachment);
        }
        catch (ParseException | IOException e) {
            handler.failed(e, attachment);
        }
    }
    @Override
    public void close() throws IOException {
        sm.close();
    }
}
