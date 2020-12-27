package BuildSearchIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BuildingIndexTest {
  private final String[] ids = {"1", "2"};
  private final String[] unindexed = {"Netherlands", "Italy"};
  private final String[] unstored = {"Amsterdam has lots of bridges",
      "Venice has lots of canals"};
  private final String[] text = {"Amsterdam", "Venice"};

  private Directory dir;
  private File dirPointer;

  @BeforeEach
  public void setup() throws Exception {
    Path dirPath = Files.createTempDirectory(null);
    this.dirPointer = new File(dirPath.toUri());
    this.dir = new MMapDirectory(dirPath);
    IndexWriter writer = this.getWriter();

    for (int i = 0; i < ids.length; i++) {
      Document doc = new Document();
      doc.add(new StringField("id", ids[i], Store.YES));
      doc.add(new StoredField("country", unindexed[i]));
      doc.add(new TextField("contents", unstored[i], Store.NO));
      doc.add(new StringField("city", text[i], Store.YES));
      writer.addDocument(doc);
    }

    writer.close();
  }

  @AfterEach
  public void teardown() throws IOException {
    if (dirPointer == null || !dirPointer.exists()) {
      return;
    }
    String[] entries;
    if ((entries = dirPointer.list()) != null) {
      for (String s : entries) {
        File curr = new File(dirPointer.getPath(), s);
        curr.delete();
      }
    }
    dirPointer.delete();
  }

  private IndexWriter getWriter() throws IOException {
    return new IndexWriter(this.dir, new IndexWriterConfig(new WhitespaceAnalyzer()));
  }

  private int getHitCount(String fieldName, String searchString) throws IOException {
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
    Term t = new Term(fieldName, searchString);
    Query query = new TermQuery(t);
    return searcher.count(query);
  }

  @Test
  public void indexWriterTest() throws IOException {
    IndexWriter writer = getWriter();
    assertEquals(ids.length, writer.getDocStats().numDocs);
    writer.close();
  }

  @Test
  public void indexReaderTest() throws IOException {
    IndexReader reader = DirectoryReader.open(dir);
    assertEquals(ids.length, reader.maxDoc());
    assertEquals(ids.length, reader.numDocs());
    reader.close();
  }

  @Test
  public void deleteBeforeTest() throws IOException {
    IndexWriter writer = this.getWriter();
    assertEquals(2, writer.getDocStats().numDocs);
    writer.deleteDocuments(new Term("id", "1"));
    writer.commit();
    assertTrue(writer.hasDeletions());
    assertEquals(2, writer.getDocStats().maxDoc);
    assertEquals(1, writer.getDocStats().numDocs);
    writer.close();
  }

  @Test
  public void updateTest() throws IOException {
    assertEquals(1, getHitCount("city", "Amsterdam"));
    IndexWriter writer = this.getWriter();
    Document doc = new Document();
    doc.add(new StringField("id", "1", Store.YES));
    doc.add(new StoredField("country", "Netherlands"));
    doc.add(new TextField("contents", "Den Haag has a lot of museums", Store.NO));
    doc.add(new StringField("city", "Den Haag", Store.YES));
    writer.updateDocument(new Term("id", "1"), doc);
    writer.close();
    assertEquals(0, getHitCount("city", "Amsterdam"));
    assertEquals(1, getHitCount("city", "Den Haag"));
  }
}
