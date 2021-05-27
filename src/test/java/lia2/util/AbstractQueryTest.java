package lia2.util;

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

public abstract class AbstractQueryTest extends AnalyzerUtils {

  @BeforeAll
  public static void setup() throws Exception {
    System.out.println("Setting up for abstract query tests");
    setupIndex();
  }

  @AfterAll
  public static void teardown() throws IOException {
    System.out.println("Tearing down abstract query tests");
    teardown(testPrefs);
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

}
