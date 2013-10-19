package kevin.local.search.framework.seams;


import kevin.local.search.framework.ListWrapper;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 2/9/13
 * Time: 2:42 PM
 */
public class FileListTransferable implements Transferable {
    private static final DataFlavor[] supportedFlavors =
        new DataFlavor[] {
            DataFlavor.javaFileListFlavor,
            DataFlavor.getTextPlainUnicodeFlavor()
        };
    private final List<File> files;



    public FileListTransferable(List<File> files) {
        this.files = files;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }


    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ListWrapper.wrap(supportedFlavors).contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            return files;
        }
        else if (DataFlavor.getTextPlainUnicodeFlavor().equals(flavor)) {
            return new ByteArrayInputStream(files.toString().getBytes("utf-16le"));
        }
        else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
