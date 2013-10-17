package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.index.IndexableField;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 3:18 PM
 */
class IndexingEventResponseImpl implements IndexingEventResponse {
    private final Action action;
    private final Collection<IndexableField> fields;

    IndexingEventResponseImpl(Action action) {
        this(action, null);
    }

    IndexingEventResponseImpl(Action action, Collection<IndexableField> fields) {
        this.action = action;
        if ((fields == null) || fields.isEmpty()) {
            this.fields = Collections.emptyList();
        }
        else {
            List<IndexableField> use = new LinkedList<>();
            use.addAll(fields);
            this.fields = Collections.unmodifiableList(use);
        }
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public Collection<IndexableField> getFields() {
        return fields;
    }

    public static IndexingEventResponse doNothingResponse() {
        return new IndexingEventResponseImpl(Action.DO_NOTHING);
    }
}
