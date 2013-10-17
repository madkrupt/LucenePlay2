package kevin.local.search.framework.seams;

import kevin.local.search.framework.SearcherBootstrap;
import kevin.local.search.framework.gui.RowDocProvider;
import kevin.local.search.framework.gui.SearchableTableModel;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.*;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/13/12
 * Time: 10:19 AM
 */
public class ResultsTableModel extends AbstractTableModel implements RowDocProvider, SearchableTableModel {
    private final static Logger log = LoggerFactory.getLogger(ResultsTableModel.class);

    private final SearcherBootstrap.Searcher audSearch;

    private final String[] translateMapping;

    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    private List<Document> data = Collections.emptyList();

    public ResultsTableModel(SearcherBootstrap.Searcher audSearch, String[] translateMapping) {
        this.audSearch = audSearch;
        this.translateMapping = translateMapping;
    }

    @Override
    public int getRowCount() {
        try {
            dataLock.readLock().lock();
            return data.size();
        }
        finally {
            dataLock.readLock().unlock();
        }
    }

    @Override
    public int getColumnCount() {
        return translateMapping.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        dataLock.readLock().lock();
        List<Document> localData = data;
        dataLock.readLock().unlock();

        try {
            IndexableField field = localData.get(rowIndex).getField(translateMapping[columnIndex]);
            if (field == null) {
                return "[No Value]";
            }
            else {
                return field.stringValue();
            }
        }
        catch (Throwable t) {
            return "[Error]";
        }
    }

    @Override
    public List<Document> getRowDocsAtomic(List<Integer> rowIndexes) {
        if ((rowIndexes == null) || (rowIndexes.size() < 1)) return Collections.emptyList();
        try {
            // Otherwise we need a Lock
            dataLock.readLock().lock();

            List<Document> ret = new LinkedList<>();
            for (Integer i : rowIndexes) {
                ret.add(data.get(i));
            }
            return ret;
        }
        finally {
            dataLock.readLock().unlock();
        }
    }

    public void setData(List<Document> newData) {
        try {
            if(dataLock.writeLock().tryLock(2, TimeUnit.SECONDS)) {
                if ((newData == null) || (newData.isEmpty())) {
                    data = Collections.emptyList();
                }
                else {
                    data = Collections.unmodifiableList(newData);
                }
            }
            else {
                log.error("setData Failed due to potential deadlock");
            }
        }
        catch (InterruptedException e) {
                log.error("Interrupted", e);
        } finally {
            // fire data changed here so that table is consistent with
            // underlying data
            fireTableDataChanged();
            dataLock.writeLock().unlock();
        }
    }

    public void searchData(String searchString, int limit, boolean forceRefresh) {
        if (searchString.trim().isEmpty()) {
            setData(null);
            return;
        }
        //if (!searchString.startsWith("*")) searchString = "*" + searchString;
        //if (!searchString.endsWith("*")) searchString = searchString + "*";
        audSearch.searchData(searchString, limit, forceRefresh, null, new CompletionHandler<List<Document>, Void>() {
            @Override
            public void completed(List<Document> result, Void attachment) {
                setData(result);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                log.error("SearchData Error!", exc);
            }
        });
    }
}
