package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.index.IndexableField;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 2:03 PM
 */
public interface IndexingEventResponse {
    enum Action {
        DO_NOTHING,
        CONTRIBUTE,
        //REMOVE_FIELDS,
        DELETE_DOC
    }

    public Action getAction();
    public Collection<IndexableField> getFields();
}
