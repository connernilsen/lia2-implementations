package analyzers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.AbstractQueryTest;

public class CustomAnalyzerTest extends AbstractQueryTest {
  @BeforeAll
  public static void setup() throws Exception {
    setupDir();
  }

  @Test
  public void koolKatTest() throws Exception {
    Analyzer analyzer = new MetaphoneReplacementAnalyzer();
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(analyzer));
    Document doc = new Document();
    doc.add(new TextField("contents", "cool cat", Store.YES));
    writer.addDocument(doc);
    writer.close();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));

    Query query = new QueryParser("contents", analyzer).parse("kool kat");
    TopDocs hits = searcher.search(query, 1);
    assertEquals(1, hits.totalHits.value);
    int docId = hits.scoreDocs[0].doc;
    doc = searcher.doc(docId);
    assertEquals("cool cat", doc.get("contents"));
  }
}
