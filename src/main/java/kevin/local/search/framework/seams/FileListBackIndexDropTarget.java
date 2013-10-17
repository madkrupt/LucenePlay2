package kevin.local.search.framework.seams;

import kevin.local.search.framework.engine.indexers.BackgroundIndexingEngine;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 2/8/13
 * Time: 8:41 AM
 */
public class FileListBackIndexDropTarget extends DropTarget {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileListBackIndexDropTarget.class);

    private final BackgroundIndexingEngine<Path> backEngine;
    private final JComponent messageAbove;

    public FileListBackIndexDropTarget(BackgroundIndexingEngine<Path> backEngine, JComponent messageAbove) throws HeadlessException {
        this.backEngine = backEngine;
        this.messageAbove = messageAbove;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        super.dragEnter(dtde);
        //Potentially start processing at this point to speed things along
    }

    public void drop(DropTargetDropEvent evt) {
        int action = evt.getDropAction();
        evt.acceptDrop(action);
        try {
            Transferable data = evt.getTransferable();
            if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                Object rawData = data.getTransferData(DataFlavor.javaFileListFlavor);
                if (rawData instanceof List) {
                    List<File> fileList = new LinkedList<>();
                    for (Object o : (List) rawData) {
                        if (o instanceof File) {
                            fileList.add((File) o);
                        }
                        else {
                            log.error("Expecting only File Objects");
                        }
                    }

                    processFiles(fileList);
                }
                else {
                    throw new IOException("Expecting a FileList");
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        } finally {
            evt.dropComplete(true);
        }
    }

    private void processFiles(List<File> files) throws IOException {
        JOptionPane.showMessageDialog(messageAbove, "Files Dragged to Table: " + files.toString());
        for (File file : files) {
            backEngine.triggerUpdaters(file.toPath());
        }
    }
}
