package advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.AbstractQueryTest;

public class DistanceSortTest extends AbstractQueryTest {
  private static final Query query = new TermQuery(new Term("type", "restaurant"));

  @BeforeAll
  public static void setup() throws Exception {
    setupDir();
    IndexWriter writer = new IndexWriter(testPrefs.dir,
        new IndexWriterConfig(new WhitespaceAnalyzer()));
    addPoint(writer, "El Charro", "restaurant", 1, 2);
    addPoint(writer, "Cafe Poca Cosa", "restaurant", 5, 9);
    addPoint(writer, "Los Betos", "restaurant", 9, 6);
    addPoint(writer, "Nico's Taco Shop", "restaurant", 3, 8);
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
  }

  private static void addPoint(IndexWriter writer, String name, String type, int x, int y)
      throws IOException {
    Document doc = new Document();
    doc.add(new StringField("name", name, Store.YES));
    doc.add(new StringField("type", type, Store.YES));
    doc.add(new StringField("location", x + "," + y, Store.YES));
    doc.add(new IntPoint("loc", x, y));
    doc.add(new StringField("locX", "" + x, Store.YES));
    doc.add(new StringField("locY", "" + y, Store.YES));
    writer.addDocument(doc);
  }

  @Test
  public void nearestRestaruantToHomeTest() throws Exception {
    Sort sort = new Sort(new SortField("location", new DistanceComparatorSource(0, 0)));

    TopDocs hits = searcher.search(query, 10, sort);
    assertEquals("El Charro", searcher.doc(hits.scoreDocs[0].doc).get("name"));
    assertEquals("Los Betos", searcher.doc(hits.scoreDocs[3].doc).get("name"));
  }
}
