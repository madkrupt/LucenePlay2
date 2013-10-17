package kevin.local.search.framework;

import kevin.local.search.framework.engine.indexers.BackgroundIndexer;
import kevin.local.search.framework.engine.indexers.ForegroundIndexer;
import kevin.local.search.framework.engine.indexers.IndexItemSource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 8/1/13
 * Time: 6:50 AM
 */
public interface SearcherBootstrap<T> {
    public interface Searcher extends AutoCloseable {
        public <A> void searchData(String search, int limit, boolean forceRefresh, A attachment, CompletionHandler<List<Document>, A> handler);
    }
    public String getName();
    public String[] getDataElements();
    public Collection<String> getIdFields();
    public Map<String, String> pullId(Iterable<IndexableField> doc);
    public Map<String, String> pullId(T doc);
    public T from(Map<String, String> id);
    public Searcher makeSearcher(Directory directory, Analyzer analyzer) throws IOException;
    public IndexItemSource<T> getIndexItemSource();
    public List<ForegroundIndexer<T>> getForegroundIndexers();
    public List<BackgroundIndexer<T>> getBackgroundIndexers();

}
