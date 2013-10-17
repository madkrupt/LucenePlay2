package kevin.local.search.framework.engine.indexers;

import java.nio.file.Path;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 3:51 PM
 */


public class BackgroundIndexers {
    private BackgroundIndexers() {}

    public static BackgroundIndexer<Path> getFileDeletedIndexUpdater() {
        return new FileDeletedIndexUpdater();
    }
    public static BackgroundIndexer<Path> getHashBackgroundIndexer(String hashField) {
        return new HashBackgroundIndexer(hashField);
    }

}
