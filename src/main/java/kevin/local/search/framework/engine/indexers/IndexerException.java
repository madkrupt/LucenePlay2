package kevin.local.search.framework.engine.indexers;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 1:58 PM
 */
public class IndexerException extends Exception {
    public IndexerException() {
    }

    public IndexerException(String message) {
        super(message);
    }

    public IndexerException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexerException(Throwable cause) {
        super(cause);
    }

    public IndexerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
