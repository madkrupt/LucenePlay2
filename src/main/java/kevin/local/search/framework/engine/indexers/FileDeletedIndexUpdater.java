package kevin.local.search.framework.engine.indexers;

import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/15/12
 * Time: 3:11 PM
 */
class FileDeletedIndexUpdater implements BackgroundIndexer<Path> {
    @Override
    public <A> Runnable createResponder(final Path rep, final A attachment, final CompletionHandler<IndexingEventResponse, A> completionHandler) {
        return new Runnable() {
            @Override
            public void run() {
                if (!Files.exists(rep) || Files.isDirectory(rep)) {
                    completionHandler.completed(new IndexingEventResponseImpl(IndexingEventResponse.Action.DELETE_DOC), attachment);
                }
                completionHandler.completed(IndexingEventResponseImpl.doNothingResponse(), attachment);
            }
        };
    }
}
