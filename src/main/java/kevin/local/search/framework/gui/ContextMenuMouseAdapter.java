package kevin.local.search.framework.gui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 2/8/13
 * Time: 8:28 AM
 */

public class ContextMenuMouseAdapter {
    public static MouseAdapter makeMouseAdapter(final JTable table, final JPopupMenu contextMenu) {
            return new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    showPopup(e);
                }

                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
    //                if (e.isPopupTrigger()) {
    //                    if (table != null) {
    //                        int r = table.rowAtPoint(e.getPoint());
    //                        HashSet<Integer> selected = new HashSet<>();
    //                        for (int i : table.getSelectedRows()) {
    //                            selected.add(i);
    //                        }
    //
    //                        if (!selected.isEmpty()) {
    //                            if (selected.contains(r)) {
    //                                if (r >= 0 && r < table.getRowCount()) {
    //                                    table.setRowSelectionInterval(r, r);
    //                                }
    //                                else {
    //                                    table.clearSelection();
    //                                }
    //                            }
    //                            else {
    //                                //Did not Click on a selected row (but rows are selected)
    //                            }
    //                        }
    //                        else {
    //                            //Nothing selected
    //                        }
    //                    }
    //                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
    //                }
    //                else {
    //                    //Not a popup trigger
    //                }
                    if (e.isPopupTrigger()) contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            };
        }


}
