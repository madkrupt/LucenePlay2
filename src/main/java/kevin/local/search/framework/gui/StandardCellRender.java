package kevin.local.search.framework.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/14/12
 * Time: 12:47 PM
 */
public class StandardCellRender extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    protected Color NORMAL_FORE = Color.BLACK;
    protected Color NORMAL_BACK = Color.LIGHT_GRAY;

    protected Color ALTERN_FORE = Color.BLACK;
    protected Color ALTERN_BACK = Color.GRAY;

    protected Color SELECT_FORE = Color.WHITE;
    protected Color SELECT_BACK = Color.PINK;

    protected Color FOCUSD_FORE = Color.ORANGE;
    protected Color FOCUSD_BACK = Color.DARK_GRAY;






    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        if (hasFocus || isSelected) {
            c.setForeground(SELECT_FORE);
            c.setBackground(SELECT_BACK);
        }
        else if (row % 2 == 0) {
            c.setForeground(NORMAL_FORE);
            c.setBackground(NORMAL_BACK);
        }
        else {
            c.setForeground(ALTERN_FORE);
            c.setBackground(ALTERN_BACK);
        }

        return c;
    }
}
