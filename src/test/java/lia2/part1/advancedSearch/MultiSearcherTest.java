package lia2.part1.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class MultiSearcherTest extends AbstractQueryTest {
  private static TestPrefs aTOmPrefs;
  private static TestPrefs nTOzPrefs;
  private static IndexSearcher searcher;

  @BeforeAll
  public static void setup() throws Exception {
    aTOmPrefs = new TestPrefs();
    nTOzPrefs = new TestPrefs();
    String[] animals = { "aardvark", "beaver", "coati",
        "dog", "elephant", "frog", "gila monster",
        "horse", "iguana", "javelina", "kangaroo",
        "lemur", "moose", "nematode", "orca",
        "python", "quokka", "rat", "scorpion",
        "tarantula", "uromastyx", "vicuna",
        "walrus", "xiphias", "yak", "zebra"};

    Analyzer analyzer = new WhitespaceAnalyzer();
    IndexWriter aTOmWriter = new IndexWriter(aTOmPrefs.dir, new IndexWriterConfig(analyzer));
    IndexWriter nTOzWriter = new IndexWriter(nTOzPrefs.dir, new IndexWriterConfig(analyzer));

    for (int i = animals.length - 1; i >= 0; i--) {
      Document doc = new Document();
      String animal = animals[i];
      doc.add(new StringField("animal", animal, Store.YES));
      if (animal.charAt(0) < 'n') {
        aTOmWriter.addDocument(doc);
      }
      else {
        nTOzWriter.addDocument(doc);
      }
    }
    aTOmWriter.close();
    nTOzWriter.close();
    searcher = new IndexSearcher(new MultiReader(DirectoryReader.open(aTOmPrefs.dir),
        DirectoryReader.open(nTOzPrefs.dir)));
  }

  @AfterAll
  public static void teardown() throws IOException {
    teardown(aTOmPrefs);
    teardown(nTOzPrefs);
  }

  @Test
  public void multiTest() throws Exception {
    TermRangeQuery query = new TermRangeQuery("animal",
        new BytesRef("h"), new BytesRef("t"), true, true);

    TopDocs hits = searcher.search(query, 10);
    assertEquals(12, hits.totalHits.value);
  }
}
