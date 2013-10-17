package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/11/12
 * Time: 1:06 PM
 */

class HashBackgroundIndexer implements BackgroundIndexer<Path> {
    private final String hashField;

    HashBackgroundIndexer(String hashField) {
        this.hashField = hashField;
    }

    public <A> Runnable createResponder(final Path rep, final A attachment, final CompletionHandler<IndexingEventResponse, A> completionHandler) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    final String checkSum = new CheckSumOffloader(rep).call();
                    completionHandler.completed(
                    new IndexingEventResponse() {
                        @Override
                        public Action getAction() {
                            return Action.CONTRIBUTE;
                        }

                        @Override
                        public Collection<IndexableField> getFields() {
                            return Arrays.<IndexableField>asList(new StringField(hashField, checkSum, Field.Store.YES));
                        }
                    }, attachment);
                }
                catch (IOException | NoSuchAlgorithmException e) {
                    completionHandler.failed(new IndexerException("Failed to contribute checksum for file", e), attachment);
                }
            }
        };
    }
}
