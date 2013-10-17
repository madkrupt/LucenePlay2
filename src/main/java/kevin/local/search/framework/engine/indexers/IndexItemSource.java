package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 8/2/13
 * Time: 6:49 AM
 */
public interface IndexItemSource<T> {
    interface ElementIndexer<T> {
        List<IndexableField> indexElement(T element, Collection<ForegroundIndexer<T>> foregroundIndexers) throws IndexerException;
    }

    public void indexSource(Directory directory, Analyzer analyzer, Collection<ForegroundIndexer<T>> indexers) throws IOException, IndexerException;
}
