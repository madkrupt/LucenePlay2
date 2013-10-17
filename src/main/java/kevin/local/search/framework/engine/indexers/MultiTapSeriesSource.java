package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 10/7/13
 * Time: 9:34 AM
 */
public class MultiTapSeriesSource<T> implements IndexItemSource<T> {
    private final List<IndexItemSource<T>> indexers;

    public MultiTapSeriesSource(List<IndexItemSource<T>> indexers) {
        this.indexers = indexers;
    }

    @Override
    public void indexSource(Directory directory, Analyzer analyzer, Collection<ForegroundIndexer<T>> foregroundIndexers) throws IOException, IndexerException {
        for (IndexItemSource<T> indexer : indexers) {
            indexer.indexSource(directory, analyzer, foregroundIndexers);
        }
    }
}
