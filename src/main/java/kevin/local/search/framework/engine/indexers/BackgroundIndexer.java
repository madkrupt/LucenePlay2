package kevin.local.search.framework.engine.indexers;

import java.nio.channels.CompletionHandler;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 1:54 PM
 */

public interface BackgroundIndexer<T> {
    public <A> Runnable createResponder(final T rep, A attachment, CompletionHandler<IndexingEventResponse, A> completionHandler);
}
