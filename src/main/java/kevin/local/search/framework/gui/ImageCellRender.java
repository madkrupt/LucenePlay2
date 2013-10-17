package kevin.local.search.framework.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/14/12
 * Time: 12:47 PM
 */
public class ImageCellRender implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

       public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int col) {

           ImageIcon c = new ImageIcon("No_icon.png");

           return new JLabel(c);

       }
}
