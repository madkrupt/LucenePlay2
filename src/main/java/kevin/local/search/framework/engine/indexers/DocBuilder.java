package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/9/12
 * Time: 12:08 PM
 */

class DocBuilder {
    static final String ALL_FIELD_NAME = "content";

    private static final ConcurrentLinkedQueue<DocBuilder> pool = new ConcurrentLinkedQueue<>();

    private Document doc = new Document();
    private StringBuilder allText = new StringBuilder();

    private DocBuilder() { }

    public static DocBuilder ni(Iterable<IndexableField> fromDoc) {
        if (fromDoc == null) return DocBuilder.ni();
        return DocBuilder.ni().addAll(fromDoc);
    }
    static DocBuilder ni() {
        DocBuilder use = pool.poll();
        if (use == null) use = new DocBuilder();
        return use;
    }
    public DocBuilder add(IndexableField field) {
        String fieldString = field.stringValue();
        if (fieldString != null) allText.append(fieldString).append(' ');
        processedAdd(field);
        return this;
    }
    private void processedAdd(IndexableField field) {
        if ((field != null) && (field.name().equalsIgnoreCase(ALL_FIELD_NAME))) throw new RuntimeException("You Cannot Use This Name!");
        //this prevents duplicate fields, and overwrites with only the latest value
        if ((field != null) && (doc.getFields(field.name()).length > 0)) doc.removeFields(field.name());
        doc.add(field);
    }
    public DocBuilder addAll(Iterable<IndexableField> fromDoc) {
        for (IndexableField field : fromDoc) {
            if (ALL_FIELD_NAME.equalsIgnoreCase(field.name())) continue;
            add(field);
        }
        return this;
    }
    public boolean containsField(String fieldName) {
        return ALL_FIELD_NAME.equalsIgnoreCase(fieldName) || doc.getField(fieldName) != null;
    }

    public Iterable<IndexableField> buildAndReset() {
        doc.add(new TextField(ALL_FIELD_NAME, allText.toString(), Field.Store.NO));
        Document ret = doc;
        doc = new Document();
        allText = new StringBuilder();
        pool.add(this);
        return ret;
    }
}
