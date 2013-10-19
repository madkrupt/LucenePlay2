package kevin.local.search.framework.gui;

import javafx.util.Pair;
import kevin.local.search.framework.ListWrapper;
import kevin.local.search.framework.SearcherBootstrap;
import kevin.local.search.framework.engine.indexers.BackgroundIndexingEngine;
import kevin.local.search.framework.seams.FileListTransferable;
import kevin.local.search.framework.seams.ResultsTableModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 10/5/13
 * Time: 3:30 PM
 */
public class TableGUIFrame<T> {
    private final static Logger log = LoggerFactory.getLogger(TableGUIFrame.class);

    private final BackgroundIndexingEngine backEngine;
    public TableGUIFrame(final SearcherBootstrap bootstrap, Directory directory, Analyzer analyzer, BackgroundIndexingEngine<T> backEngine) {
        this.backEngine = backEngine;
        try (SearcherBootstrap.Searcher searcher = bootstrap.makeSearcher(directory, analyzer)) {

            final String[] colData = bootstrap.getDataElements();
            final Pair<TableColumnModel, JPopupMenu> modelMenuPair = makeColumnModel(colData);
            final ResultsTableModel rtm = new ResultsTableModel(searcher, colData);

            final SearchableJTableAdapter resultTable = new SearchableJTableAdapter(rtm, modelMenuPair.getKey());

            // add the listener to the jtable
            //MouseListener popupListener = new PopupListener();
            // add the listener specifically to the header
            //table.addMouseListener(popupListener);

            resultTable.getTableHeader().addMouseListener(ContextMenuMouseAdapter.makeMouseAdapter(null, modelMenuPair.getValue()));
            resultTable.addMouseListener(ContextMenuMouseAdapter.makeMouseAdapter(resultTable, makeContextMenu(rtm, resultTable)));

            //For Drag Drop
            //resultTable.setDragEnabled(true);
            //resultTable.setTransferHandler(new FileListTransferHandler(rtm, resultTable, backEngine, bootstrap.getIdField()));
            //resultTable.setDropTarget(new FileListBackIndexDropTarget(backEngine, resultTable));


            TableGUI gui = new TableGUI(resultTable);

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(gui, BorderLayout.CENTER);
            frame.setSize(600, 400);
            frame.setVisible(true);

            waitForExit();
        }
        catch (Exception e) {
            log.error("Failed while searching", e);
        }
    }

    public void waitForExit(boolean blocking) {
        if (!blocking) throw new RuntimeException("Not Yet Implemented");

        try {
            waitForExit();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void waitForExit() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    public void run() {
                        cdl.countDown();


                    }
                }, "Background Indexing Closer Thread"));

        cdl.await();

    }

    private static Pair<TableColumnModel, JPopupMenu> makeColumnModel(String[] colData) {
        final TableColumnModel columnModel = new DefaultTableColumnModel();
        final JPopupMenu headerMenu = new JPopupMenu("Column Chooser");

        int colIndx = 0;
        for (String colDataItem : colData) {
            final TableColumn tempColumn = new TableColumn(colIndx++);
            tempColumn.setHeaderValue(colDataItem);
            tempColumn.setResizable(true);
            if (colDataItem.equalsIgnoreCase("FILE_NAME")) {
                tempColumn.setCellRenderer(new PathCellRender());
            }
            else {
                tempColumn.setCellRenderer(new StandardCellRender());
            }
            columnModel.addColumn(tempColumn);

            //----------------------------------------------------//
            final JCheckBoxMenuItem tempMenuItem = new JCheckBoxMenuItem(colDataItem, true);
            tempMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (tempMenuItem.isSelected()) {
                        columnModel.addColumn(tempColumn);
                    }
                    else {
                        columnModel.removeColumn(tempColumn);
                    }
                }

            });

            headerMenu.add(tempMenuItem);
        }
        return new Pair<>(columnModel, headerMenu);
    }

    private JPopupMenu makeContextMenu(final RowDocProvider rowDocProvider, final JTable owningTable) {

        final JMenuItem allInfoBox = new JMenuItem("Get Info");
        final JMenuItem dobRefresh = new JMenuItem("Force Refresh [Background]");
        final JMenuItem moveOption = new JMenuItem("Move to Organizing Folder");
        final JMenuItem copyAsList = new JMenuItem("Copy FileList");

        //Set initially to to disabled
        allInfoBox.setEnabled(false);
        dobRefresh.setEnabled(false);
        moveOption.setEnabled(false);
        copyAsList.setEnabled(false);


        copyAsList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Document> selectedDocs = rowDocProvider.getRowDocsAtomic(ListWrapper.wrap(owningTable.getSelectedRows()));
                List<File> files = new LinkedList<>();
                for (Document doc : selectedDocs) {
                    files.add(new File(doc.get("ID")));
                }

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new FileListTransferable(files),
                        new ClipboardOwner() {
                            @Override
                            public void lostOwnership(Clipboard clipboard, Transferable contents) {
                                // Do I need this for anything?
                            }
                        });
            }
        });

        moveOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Document> selectedDocs = rowDocProvider.getRowDocsAtomic(ListWrapper.wrap(owningTable.getSelectedRows()));
                //ToDo: Move to a Seperate Thread
                for (Document doc : selectedDocs) {
                    try {
                        Path currDocPath = Paths.get(doc.get("FILE_NAME"));
                        Path postDocPath = Paths.get(doc.get("/tmp/movetodir/"), currDocPath.getFileName().toString());

                        if (!Files.exists(currDocPath) || Files.exists(postDocPath))
                            throw new IOException("Failed to move file, source doesn't exist or destination does");
                        if (!Files.isRegularFile(currDocPath)) throw new IOException("Source is not a regular file");

                        //Files.move(currDocPath, postDocPath);
                        JOptionPane.showMessageDialog(owningTable, String.format("Would Move File: %s --> %s", currDocPath, postDocPath), "No Action Performed", JOptionPane.WARNING_MESSAGE);

                        backEngine.triggerUpdaters(currDocPath);
                        backEngine.triggerUpdaters(postDocPath);

                    } catch (IOException ioe) {
                        log.error("Failed to move file", ioe);
                        JOptionPane.showMessageDialog(owningTable, "Failed to Move File: " + ioe.getMessage(), "No Action Performed", JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

        dobRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Document> selectedDocs = rowDocProvider.getRowDocsAtomic(ListWrapper.wrap(owningTable.getSelectedRows()));
                for (Document doc : selectedDocs) {
                    Path currDocPath = Paths.get(doc.get("FILE_NAME"));

                    log.info(String.format("Forcing Refresh on: %s", currDocPath));

                    backEngine.triggerUpdaters(currDocPath);
                }
            }
        });

        allInfoBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Document doc = rowDocProvider.getRowDocsAtomic(Collections.singletonList(owningTable.getSelectedRow())).get(0);
                String allInfo = "";

                for (IndexableField field : doc.getFields()) {
                    allInfo += field.name() + ": " + field.stringValue() + "\n";
                }

                JOptionPane.showMessageDialog(owningTable, allInfo);
            }
        });


        JPopupMenu ret = new JPopupMenu("Actions Menu");
        ret.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (owningTable.getSelectedRows().length < 1) {
                    allInfoBox.setEnabled(false);
                    moveOption.setEnabled(false);
                    dobRefresh.setEnabled(false);
                    copyAsList.setEnabled(false);

                }
                else {
                    moveOption.setEnabled(true);
                    dobRefresh.setEnabled(true);
                    copyAsList.setEnabled(true);
                    if (owningTable.getSelectedRows().length == 1) {
                        allInfoBox.setEnabled(true);
                    }
                    else {
                        allInfoBox.setEnabled(false);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        ret.add(allInfoBox);
        ret.add(dobRefresh);
        ret.add(copyAsList);
        ret.add(moveOption);


        return ret;
    }
}
