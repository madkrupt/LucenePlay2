package kevin.local.search.framework.gui;

import org.apache.lucene.document.Document;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: madkrupt
* Date: 8/4/13
* Time: 7:33 AM
*/
public interface RowDocProvider {
    public List<Document> getRowDocsAtomic(List<Integer> rowIndexes);
}
