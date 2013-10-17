package kevin.local.search.framework.engine.indexers;

import kevin.local.search.framework.SearcherBootstrap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiTermsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 8/3/13
 * Time: 7:07 AM
 */
public class Indexers {
    public static <T> void contribute(SearcherBootstrap<T> bootstrap,
                                  IndexSearcher indexSearcher,
                                  IndexWriter indexWriter,
                                  Map<String, String> docKey,
                                  Iterable<IndexableField> contributions,
                                  boolean forceNulls, boolean allowCreate,
                                  Analyzer analyzer) throws IOException {

        final Logger log = LoggerFactory.getLogger(IndexItemSource.class);

        Document oldDoc = null;

        Query q = Indexers.makeIdQuery(docKey);


        // Lookup old doc if applicable
        if (indexSearcher != null) {
            TopDocs topDocs = indexSearcher.search(q, 2);

            if (topDocs.scoreDocs.length > 1) {
                throw new IOException("Found more than 1 matching documents");
            }
            else if (topDocs.scoreDocs.length == 1) {
                oldDoc = indexSearcher.doc(topDocs.scoreDocs[0].doc);
            }
        }

        // check to make sure we are allowed to create this doc
        if (oldDoc == null && !allowCreate) throw new IOException("Didn't find any documents that match");

        final DocBuilder builder = DocBuilder.ni(oldDoc);
        for (IndexableField field : contributions) {
            builder.add(field);
        }

        // WTF was i thinking with the below line!?
        //Analyzer idFixAnalyzer = new PerFieldAnalyzerWrapper(analyzer, Collections.<String, Analyzer>singletonMap(idFields, new KeywordAnalyzer()));

        if (oldDoc == null) {
            //Check for primary key fields

            //if (!builder.containsField(idFields)) {
                //Add the primary key
                //log.error("Document devoid of primary key!");
                //builder.add(new StringField(IndxFld.ID.name(), docKey, Field.Store.YES));
            //}

            Iterable<IndexableField> toAdd = builder.buildAndReset();

            // This should verify that we have all elements of a composite Key (should throw if we can't pullId)
            Map<String, String> compositeId = bootstrap.pullId(toAdd);
            indexWriter.addDocument(toAdd, analyzer);
            log.debug("Created doc: " + toAdd);
        }
        else {
            Iterable<IndexableField> toUp = builder.buildAndReset();
            // due to the modifications allowing composite IDs we can no longer use updateDocument, we must delete and add manually
            //indexWriter.updateDocument(toMatch, toUp, idFixAnalyzer);
            updateDocument(indexWriter, bootstrap.pullId(toUp), toUp);
            log.debug("updated document: " + toUp);
        }

        indexWriter.commit();
    }

    public static Query makeIdQuery(Map<String, String> compositeId) {
        BooleanQuery bq = new BooleanQuery();

        for (Map.Entry<String, String> id : compositeId.entrySet()) {
            bq.add(new TermQuery(new Term(id.getKey(), id.getValue())), BooleanClause.Occur.MUST);

        }
        return bq;

    }
    public static void updateDocument(IndexWriter indexWriter, Map<String, String> compositeID, Iterable<IndexableField> updated) throws IOException {
        deleteDocument(indexWriter, compositeID);
        indexWriter.addDocument(updated);

    }
    public static void deleteDocument(IndexWriter indexWriter, Map<String, String> compositeId) throws IOException {
        // isolate this functionality should I want to make additional modifications or checks OR for debugging
        Query q = Indexers.makeIdQuery(compositeId);
        indexWriter.deleteDocuments(q);
    }
}
