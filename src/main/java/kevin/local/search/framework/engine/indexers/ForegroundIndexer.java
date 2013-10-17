package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.index.IndexableField;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/30/12
 * Time: 2:27 PM
 */
public interface ForegroundIndexer<T> {
    public Iterable<IndexableField> visitItem(T item);
}
