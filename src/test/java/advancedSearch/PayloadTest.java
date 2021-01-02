package advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import advancedSearch.payload.BoostingSimilarity;
import advancedSearch.payload.BulletinPayloadsAnalyzer;
import advancedSearch.payload.BulletinPayloadsFilter;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.AbstractQueryTest;

public class PayloadTest extends AbstractQueryTest {
  private static BulletinPayloadsAnalyzer analyzer;
  private static IndexWriter writer;

  @BeforeAll
  public static void setup() throws Exception {
    setupDir();
    analyzer = new BulletinPayloadsAnalyzer(5.0F);
    writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(analyzer));
  }

  @AfterAll
  public static void teardown() throws IOException {
    writer.close();
    teardown(testPrefs);
  }

  private void addDoc(String title, String contents) throws IOException {
    Document doc = new Document();
    doc.add(new StoredField("title", title));
    doc.add(new TextField("contents", contents, Store.NO));

    analyzer.setIsBulletin(contents.startsWith("Bulletin"));
    writer.addDocument(doc);
  }

  @Test
  public void payloadTermQueryTest() throws Throwable {
    this.addDoc("Hurricane Warning", "Bulletin: A hurricane warning was issued"
        + " at 6AM for the outer great banks");
    this.addDoc("Warning label maker", "The warning label maker is a great toy"
        + " for your precocious seven year old's warning needs");
    this.addDoc("Tornado Warning", "Bulletin: There is a tornado warning for "
        + "Worcester county until 6PM today");

    IndexReader r = DirectoryReader.open(writer);
    writer.close();

    IndexSearcher searcher = new IndexSearcher(r);
    searcher.setSimilarity(new BoostingSimilarity());

    Term warning = new Term("contents", "warning");

    Query query1 = new TermQuery(warning);
    System.out.println("\nTermQuery Results:");
    TopDocs hits = searcher.search(query1, 10);
    dumpHits(searcher, hits);

    assertEquals("Warning label maker", searcher.doc(hits.scoreDocs[0].doc).get("title"));

    Query query2 = new PayloadScoreQuery(new SpanTermQuery(warning), new MaxPayloadFunction(),
        PayloadDecoder.FLOAT_DECODER);
    System.out.println("\nTermQuery Results:");
    hits = searcher.search(query2, 10);
    dumpHits(searcher, hits);

//    assertEquals("Warning label maker", searcher.doc(hits.scoreDocs[2].doc).get("title"));
  }
}
