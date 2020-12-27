package util;

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
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractQueryTest {
  protected static final String dataDir = "./data";
  protected static TestPrefs testPrefs;
  protected static IndexSearcher searcher;

  @BeforeAll
  public static void setup() throws Exception {
    testPrefs = new TestPrefs();
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(new StandardAnalyzer()));

    List<File> files = findFiles(new File(dataDir));
    for (File file : files) {
      Document doc = getDoc(file);
      writer.addDocument(doc);
    }

    writer.close();
    searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
  }

  private static Document getDoc(File file) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));
    Document doc = new Document();

    String cat = file.getParent().substring(dataDir.length());
    cat = cat.replace(File.separatorChar, '/');
    String isbn = props.getProperty("isbn");
    String title = props.getProperty("title");
    String author = props.getProperty("author");
    String url = props.getProperty("url");
    String subject = props.getProperty("subject");

    String pubmonth = props.getProperty("pubmonth");

    System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + cat + "\n---------");

    doc.add(new StringField("isbn", isbn, Store.YES));
    doc.add(new StringField("category", cat, Store.YES));
    FieldType titleType = new FieldType(TextField.TYPE_STORED);
    titleType.setStoreTermVectors(true);
    titleType.setStoreTermVectorOffsets(true);
    doc.add(new Field("title", title, titleType));
    FieldType title2Type = new FieldType(StringField.TYPE_STORED);
    title2Type.setStoreTermVectors(true);
    title2Type.setStoreTermVectorOffsets(true);
    title2Type.setOmitNorms(true);
    doc.add(new Field("title2", title.toLowerCase(), title2Type));
    String[] authors = author.split(",");
    for (String a : authors) {
      doc.add(new Field("author", a, titleType));
    }

    FieldType urlType = new FieldType(StringField.TYPE_STORED);
    urlType.setOmitNorms(true);
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
    doc.add(new StoredField("pubmonthAsDay", (int) d.getTime() / (1000 * 3600 * 24)));
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
    testPrefs.dir.close();
    if (testPrefs.dirPointer == null || !testPrefs.dirPointer.exists()) {
      return;
    }
    String[] entries;
    if ((entries = testPrefs.dirPointer.list()) != null) {
      for (String s : entries) {
        File curr = new File(testPrefs.dirPointer.getPath(), s);
        curr.delete();
      }
    }
    testPrefs.dirPointer.delete();
  }

  protected static boolean hitsIncludeTitle(IndexSearcher searcher, TopDocs hits, String title)
    throws IOException{
    for (ScoreDoc match : hits.scoreDocs) {
      Document doc = searcher.doc(match.doc);
      if (title.equals(doc.get("title"))) {
        return true;
      }
    }
    return false;
  }

  protected static long hitCount(IndexSearcher searcher, Query query) throws IOException {
    return searcher.search(query, 1).totalHits.value;
  }

//  protected static long hitCount(IndexSearcher searcher, Query query, Filter filter)
//      throws IOException {
//    return searcher.search(query, filter, 1).totalHits.value;
//  }

  protected static void dumpHits(IndexSearcher searcher, TopDocs hits) throws IOException {
    if (hits.totalHits.value == 0L) {
      System.out.println("No hits");
    }

    for (ScoreDoc match : hits.scoreDocs) {
      Document doc = searcher.doc(match.doc);
      System.out.println(match.score + ":" + doc.get("title"));
    }
  }

  protected static boolean slopMatched(String[] phrase, int slop, IndexSearcher testSearcher)
      throws IOException {
    PhraseQuery.Builder query = new PhraseQuery.Builder();
    query.setSlop(slop);
    for (String word : phrase) {
      query.add(new Term("field", word));
    }

    TopDocs matches = testSearcher.search(query.build(), 10);
    return matches.totalHits.value > 0;
  }

  protected TestPrefs setupPhraseQuery() throws IOException {
    TestPrefs prefs = new TestPrefs();
    IndexWriter writer = new IndexWriter(prefs.dir, new IndexWriterConfig(new WhitespaceAnalyzer()));
    Document doc = new Document();
    doc.add(new TextField("field", "the quick brown fox jumped over the lazy dog",
        Store.YES));
    writer.addDocument(doc);
    writer.close();
    prefs.searcher = new IndexSearcher(DirectoryReader.open(prefs.dir));

    return prefs;
  }

  protected TestPrefs indexSingleFieldDocs(Field[] fields) throws Exception {
    TestPrefs prefs = new TestPrefs();
    IndexWriter writer = new IndexWriter(prefs.dir, new IndexWriterConfig(new WhitespaceAnalyzer()));

    for (Field f : fields) {
      Document doc = new Document();
      doc.add(f);
      writer.addDocument(doc);
    }
    writer.close();
    prefs.searcher = new IndexSearcher(DirectoryReader.open(prefs.dir));
    return prefs;
  }

  protected static class TestPrefs {
    public final File dirPointer;
    public final Directory dir;
    IndexSearcher searcher;

    public TestPrefs() throws IOException {
      Path dirPath = Files.createTempDirectory(null);
      dirPointer = new File(dirPath.toUri());
      dir = new MMapDirectory(dirPath);
    }

    public IndexSearcher getSearcher() {
      return searcher;
    }

    public void tearDown() throws IOException {
      dir.close();
    }
  }
}
