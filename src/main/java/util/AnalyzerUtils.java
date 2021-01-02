package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

public class AnalyzerUtils {
  public static TestPrefs testPrefs;
  public static final String DATA_DIR = "./data";
  public static IndexSearcher searcher;

  public static void displayTokens(Analyzer analyzer, String text) throws IOException {
    displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
  }

  public static void displayTokens(TokenStream stream) throws IOException {
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    while (stream.incrementToken()) {
      System.out.print("[" + String.copyValueOf(term.buffer()) + "] ");
    }
    stream.close();
  }

  public static void displayTokensWithFullDetails(Analyzer analyzer, String text)
      throws IOException {
    TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);
    OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
    TypeAttribute type = stream.addAttribute(TypeAttribute.class);

    int position = 0;
    stream.reset();
    while (stream.incrementToken()) {
      int increment = posIncr.getPositionIncrement();
      if (increment > 0) {
        position = position + increment;
        System.out.println();
        System.out.println(position + ": ");
      }

      System.out.println("[" + String.copyValueOf(term.buffer()) + ":" +
          offset.startOffset() + "->" + offset.endOffset()
          + ":" + type.type() + "]");
      System.out.println();
    }
    stream.close();
  }

  public static void setupIndex() throws Exception {
    setupDir();
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(new StandardAnalyzer()));

    List<File> files = findFiles(new File(DATA_DIR));
    for (File file : files) {
      Document doc = getDoc(file);
      writer.addDocument(doc);
    }

    writer.close();
    searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
  }

  public static void setupDir() throws Exception {
    testPrefs = new TestPrefs();
  }

  private static Document getDoc(File file) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));
    Document doc = new Document();

    String cat = file.getParent().substring(DATA_DIR.length());
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
    doc.add(new SortedDocValuesField("category", new BytesRef(cat)));
    FieldType titleType = new FieldType(TextField.TYPE_STORED);
    titleType.setStoreTermVectors(true);
    titleType.setStoreTermVectorOffsets(true);
    doc.add(new Field("title", title, titleType));
    doc.add(new BinaryDocValuesField("title", new BytesRef(title)));
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
    doc.add(new BinaryDocValuesField("url", new BytesRef(url)));
    doc.add(new Field("subject", subject, titleType));
    doc.add(new IntPoint("pubmonth", Integer.parseInt(pubmonth)));
    doc.add(new NumericDocValuesField("pubmonth", Integer.parseInt(pubmonth)));
    doc.add(new StoredField("pubmonth", pubmonth));

    Date d;
    try {
      d = DateTools.stringToDate(pubmonth);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    doc.add(new StoredField("pubmonthAsDay", (int) d.getTime() / (1000 * 3600 * 24)));
    doc.add(new IntPoint("pubmonthAsDay", (int) d.getTime() / (1000 * 3600 * 24)));

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

  public static void teardown() throws IOException {
    teardown(testPrefs);
  }

  public static void teardown(TestPrefs prefs) throws IOException {
    prefs.dir.close();
    if (prefs.dirPointer == null || !prefs.dirPointer.exists()) {
      return;
    }
    String[] entries;
    if ((entries = prefs.dirPointer.list()) != null) {
      for (String s : entries) {
        File curr = new File(prefs.dirPointer.getPath(), s);
        curr.delete();
      }
    }
    prefs.dirPointer.delete();
  }

  public static class TestPrefs {
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
      AnalyzerUtils.teardown(this);
    }
  }
}
