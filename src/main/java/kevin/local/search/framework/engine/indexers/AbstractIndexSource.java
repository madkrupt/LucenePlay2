package kevin.local.search.framework.engine.indexers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 8/3/13
 * Time: 3:04 PM
 */
public abstract class AbstractIndexSource<T> implements IndexItemSource<T>  {
    private final static Logger log = LoggerFactory.getLogger(AbstractIndexSource.class);

    protected class StandardElementIndexer<T> implements ElementIndexer<T> {
        @Override
        public List<IndexableField> indexElement(T element, Collection<ForegroundIndexer<T>> foregroundIndexers) throws IndexerException {
            List<IndexableField> fields = new LinkedList<>();

            for (ForegroundIndexer<T> foreIndexer : foregroundIndexers) {
                fields.addAll(colFromIter(foreIndexer.visitItem(element)));
            }
            return fields;
        }

        private <T> Collection<T> colFromIter(Iterable<T> iterable) {
                Collection<T> ret = new ArrayList<>();
                for (T item : iterable) {
                    ret.add(item);
                }
                return ret;
            }
    }

    public abstract void indexSourceElements(IndexSearcher indexSearcher, IndexWriter indexWriter, Analyzer analyzer, Collection<ForegroundIndexer<T>> foregroundIndexers, ElementIndexer<T> elementIndexer) throws IOException, IndexerException;

    protected ElementIndexer<T> getElementIndexer() {
        return new StandardElementIndexer<T>();
    }

    @Override
    public void indexSource(Directory directory, Analyzer analyzer, Collection<ForegroundIndexer<T>> foregroundIndexers) throws IOException, IndexerException {

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_40, analyzer));

         indexWriter.waitForMerges();
         indexWriter.commit();

         log.debug("Index Writer, numdocs: " + indexWriter.numDocs());

         IndexSearcher openedSearcher;
         try {
             openedSearcher = new IndexSearcher(DirectoryReader.open(directory));
             log.debug("Using existing index");
         }
         catch (IndexNotFoundException | NoSuchDirectoryException e) {
             //No Index to Read, start from scratch
             log.warn("No existing index was loaded");
             openedSearcher = null;
         }


         indexSourceElements(openedSearcher, indexWriter, analyzer, foregroundIndexers, getElementIndexer());



         try {
             indexWriter.close(true);
         }
         catch (Exception e) {
             log.warn("Error Closing index Writer", e);
         }
         try {
             openedSearcher.getIndexReader().close();
         }
         catch (Exception e) {
             log.warn("Error Closing Index Reader", e);
         }
    }
}
