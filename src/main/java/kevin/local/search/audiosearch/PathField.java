package kevin.local.search.audiosearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

/**
 * Created with IntelliJ IDEA.
 * User: madkrupt
 * Date: 12/14/12
 * Time: 1:04 PM
 */


public class PathField implements IndexableField {
    public static FieldType makeFieldType() {
        FieldType ret = new FieldType();
        ret.setIndexed(true);
        ret.setTokenized(true);
        ret.setStored(true);
        ret.freeze();

        return ret;
    }

    private final String name;
    private final IndexableFieldType fieldType;
    protected float boost = 1.0f;
    private final Path value;

      public PathField(String name, Path value) {
          if ((name == null) || (value == null)) throw new IllegalArgumentException("Name and path cannot be null!");
          this.name = name;
          this.fieldType = makeFieldType();
          this.value = value;

      }

    @Override
    public String name() {
        return name;
    }

    @Override
    public IndexableFieldType fieldType() {
        return fieldType;
    }

    @Override
    public float boost() {
        return boost;
    }

    @Override
    public BytesRef binaryValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return value.toString();

         }

    @Override
    public Reader readerValue() {
        return null;
    }

    @Override
    public Number numericValue() {
        return null;
    }

    public Path pathValue() {
        return value;
    }

    @Override
    public TokenStream tokenStream(Analyzer analyzer) throws IOException {
        return analyzer.tokenStream(name(), new StringReader(stringValue()));
    }
}
