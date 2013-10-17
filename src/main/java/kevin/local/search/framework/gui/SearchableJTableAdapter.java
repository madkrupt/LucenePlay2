package kevin.local.search.framework.gui;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 2/8/13
 * Time: 8:54 AM
 */


public class SearchableJTableAdapter extends JTable implements SearchableElement {
    private final SearchableTableModel searchableTableModel;

    public SearchableJTableAdapter(SearchableTableModel searchableTableModel, TableColumnModel columnModel) {
        super(searchableTableModel, columnModel);
        this.searchableTableModel = searchableTableModel;
    }

    @Override
    public void searchData(String search, int limit, boolean ensureRefresh) {
        searchableTableModel.searchData(search, limit, ensureRefresh);
    }
}
