package kevin.local.search.framework.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/14/12
 * Time: 12:47 PM
 */
public class PathCellRender extends StandardCellRender {
        private static final long serialVersionUID = 1L;

       public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int col) {

           if (value != null) {
               value = value.toString().replace("/Users/madkrupt", "~");
           }

           Component c = super.getTableCellRendererComponent(table, value,
                   isSelected, hasFocus, row, col);
          return c;
       }
}
