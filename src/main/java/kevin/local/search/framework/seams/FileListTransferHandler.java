package kevin.local.search.framework.seams;

import local.kevin.data.data.ListWrapper;
import kevin.local.search.framework.engine.indexers.BackgroundIndexingEngine;
import org.apache.lucene.document.Document;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 2/8/13
 * Time: 8:34 AM
 */


public class FileListTransferHandler extends TransferHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileListTransferHandler.class);

    private final ResultsTableModel rtm;
    private final JTable resultTable;
    private final BackgroundIndexingEngine<Path> backEngine;
    private final String pathField;

    public FileListTransferHandler(ResultsTableModel rtm, JTable resultTable, BackgroundIndexingEngine<Path> backEngine, String pathField) {
        this.rtm = rtm;
        this.resultTable = resultTable;
        this.backEngine = backEngine;
        this.pathField = pathField;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        final List<File> files = new ArrayList<>();

        for (Document doc : rtm.getRowDocsAtomic(ListWrapper.wrap(resultTable.getSelectedRows()))) {
            String fileName = doc.get(pathField);

            File toAdd = new File(fileName);
            if (toAdd.exists()) {
                files.add(toAdd);

                //Wait 10 seconds per song
                backEngine.triggerUpdaters(toAdd.toPath(), 10 * files.size(), TimeUnit.SECONDS);
            }
            else {
                JOptionPane.showMessageDialog(resultTable, "File Missing: " + fileName, "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
        return new FileListTransferable(files);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                List<File> fileList = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
                for (File f : fileList) {
                    backEngine.triggerUpdaters(f.toPath());
                }
            } catch (Exception e) {
                log.error("Error Updating Files Post Drap/Drop Export", e);
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        //return TransferHandler.COPY_OR_MOVE;
        return TransferHandler.COPY;
    }
}