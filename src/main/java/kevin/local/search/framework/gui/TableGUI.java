package kevin.local.search.framework.gui;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/13/12
 * Time: 7:04 AM
 */


public class TableGUI extends JPanel {
    //private final JFrame frame;
    private final JTextField searchText;
    private final SearchableJTableAdapter searchTable;


    private final JScrollPane scrollPane;


    public TableGUI(final SearchableJTableAdapter searchTable) {
        searchText = new JTextField();
        this.searchTable = searchTable;

        this.scrollPane = new JScrollPane(searchTable);
        //this.selectMenu = searcher.getResultContextMenu();
        //this.selectMenu = new ResultsTableContextMenu(table, searcher.getRowDocProvider());


        this.setLayout(new BorderLayout());
        this.add(searchText, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        searchText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //doSearch(50);
            }
        });
        searchText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //showKeyEvent("KeyTyped", e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //showKeyEvent("KeyPressed", e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() >= 65 && e.getKeyCode() <= 90) {
                    // a - z
                    doSearch(15, false);
                }
                else if (e.getKeyCode() >= 48 && e.getKeyCode() <= 57) {
                    // 0 - 9
                    doSearch(15, false);
                }
                else if (e.getKeyCode() == 8) {
                    // backspace
                    doSearch(15, false);
                }
                else if (e.getKeyCode() == 10) {
                    // Enter?
                    doSearch(50, true);
                }
                else if (e.getKeyCode() == 27) {
                    // ESC
                    searchText.setText("");
                    doSearch(0, false);
                }
                else {
                    showKeyEvent("Unknown KeyReleased", e);
                }
            }
        });
    }

        //menuItem.addActionListener(new InsertRowsActionAdapter(this));


    private void doSearch(int limit, boolean forceRefresh) {
        searchTable.searchData(searchText.getText(), limit, forceRefresh);
    }

    private static void showKeyEvent(String action, KeyEvent e) {
        System.out.printf("%s: (%s)(%s)%n", action, e.getKeyChar(), e.getKeyCode());

    }
}
