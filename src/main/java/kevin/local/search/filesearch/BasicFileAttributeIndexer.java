package kevin.local.search.filesearch;

import kevin.local.search.framework.engine.indexers.ForegroundIndexer;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/30/12
 * Time: 2:35 PM
 */
public class BasicFileAttributeIndexer implements ForegroundIndexer<Path> {
    private final Logger log = LoggerFactory.getLogger(BasicFileAttributeIndexer.class);

    enum IndxFields {
        SIZE,
        CREATED_DATE,
        LAST_ACCESS_DATE,
        LAST_MODIFIED_DATE,


    }

    @Override
    public Iterable<IndexableField> visitItem(Path item) {
        try {
            LinkedList<IndexableField> ret = new LinkedList<>();

            BasicFileAttributes attr = null;
            try {
                attr = Files.readAttributes(item, BasicFileAttributes.class);
                System.out.println("File size: " + attr.size());
                System.out.println("File creation time: " + attr.creationTime());
                System.out.println("File was last accessed at: " + attr.lastAccessTime());
                System.out.println("File was last modified at: " + attr.lastModifiedTime());
                System.out.println("Is directory? " + attr.isDirectory());
                System.out.println("Is regular file? " + attr.isRegularFile());
                System.out.println("Is symbolic link? " + attr.isSymbolicLink());
                System.out.println("Is other? " + attr.isOther());
            }
            catch (IOException e) {
                log.error("Failed to get File Attributes for " + item, e);
            }
        }
        finally {
          System.out.println("finally");
        }
        throw new RuntimeException("NYI");
    }

    public static void main(String[] args) {
        // this is for testing
        new BasicFileAttributeIndexer().visitItem(Paths.get("/Users/madkrupt/Desktop/tstmusic/2/Daylight/01 Daylight.mp3"));
    }
}
