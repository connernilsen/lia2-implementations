package SearchIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SearchTest {
  private static File dirPointer;
  private static Directory dir;
  private static final String dataDir = "./data";

  @BeforeAll
  public static void setup() throws Exception {
    Path dirPath = Files.createTempDirectory(null);
    dirPointer = new File(dirPath.toUri());
    dir = new MMapDirectory(dirPath);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));

    List<File> files = findFiles(new File(dataDir));
    for (File file : files) {
      Document doc = getDoc(file);
      writer.addDocument(doc);
    }

    writer.close();
  }

  private static Document getDoc(File file) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));
    Document doc = new Document();

    String cat = file.getParent().substring(dataDir.length());
    cat = cat.replace(File.separatorChar, '/');
    String isbn = props.getProperty("isbn");         //2
    String title = props.getProperty("title");       //2
    String author = props.getProperty("author");     //2
    String url = props.getProperty("url");           //2
    String subject = props.getProperty("subject");   //2

    String pubmonth = props.getProperty("pubmonth"); //2

    System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + cat + "\n---------");

    doc.add(new StoredField("isbn", isbn));
    doc.add(new StoredField("category", cat));
    FieldType titleType = new FieldType(TextField.TYPE_STORED);
    titleType.setStoreTermVectors(true);
    titleType.setStoreTermVectorOffsets(true);
    doc.add(new Field("title", title, titleType));
    FieldType title2Type = new FieldType(TextField.TYPE_STORED);
    title2Type.setStoreTermVectors(true);
    title2Type.setStoreTermVectorOffsets(true);
    title2Type.setOmitNorms(true);
    title2Type.setTokenized(false);
    doc.add(new Field("title2", title.toLowerCase(), title2Type));
    String[] authors = author.split(",");
    for (String a : authors) {
      doc.add(new Field("author", author, titleType));
    }

    FieldType urlType = new FieldType(TextField.TYPE_STORED);
    urlType.setOmitNorms(true);
    urlType.setTokenized(false);
    doc.add(new Field("url", url, urlType));
    doc.add(new Field("subject", subject, titleType));
    doc.add(new IntPoint("pubmonth", Integer.parseInt(pubmonth)));
    doc.add(new StoredField("pubmonthValue", pubmonth));

    Date d;
    try {
      d = DateTools.stringToDate(pubmonth);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    doc.add(new IntPoint("pubmonthAsDay", (int) d.getTime() / (1000 * 3600 * 24)));
    doc.add(new IntPoint("pubmonthAsDayValue", (int) d.getTime() / (1000 * 3600 * 24)));

    for (String text : new String[] {title, subject, author, cat}) {
      FieldType textType = new FieldType(titleType);
      textType.setStored(false);
      doc.add(new Field("contents", text, textType));
    }

    return doc;
  }

  private static List<File> findFiles(File bookDir) {
    List<File> files = new ArrayList<>();
    for (File file : Objects.requireNonNull(bookDir.listFiles())) {
      if (file.getName().endsWith(".properties")) {
        files.add(file);
      }
      else if (file.isDirectory()) {
        files.addAll(findFiles(file));
      }
    }
    return files;
  }

  @AfterAll
  public static void teardown() throws IOException {
    dir.close();
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

  @Test
  public void termTest() throws Exception{
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));

    Term t = new Term("subject", "ant");
    Query query = new TermQuery(t);
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);
    t = new Term("subject", "junit");
    docs = searcher.search(new TermQuery(t), 10);
    assertEquals(2, docs.totalHits.value);
  }

  @Test
  public void queryParserTest() throws Exception {
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));

    QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());
    Query query = parser.parse("+JUNIT +ANT -MOCK");
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);
    Document d = searcher.doc(docs.scoreDocs[0].doc);
    assertEquals("Ant in Action", d.get("title"));

  }
}
