package lia2.analyzers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lia2.analyzers.metaphone.MetaphoneReplacementAnalyzer;
import lia2.analyzers.metaphone.MetaphoneReplacementFilter;
import lia2.analyzers.synonym.SynonymAnalyzer;
import lia2.analyzers.synonym.SynonymAnalyzer.TestSynonymEngine;
import lia2.analyzers.synonym.SynonymEngine;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class AnalyzerTest extends AbstractQueryTest {
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

  @Test
  public void jumpsTest() throws Exception {
    TokenStream stream = new SynonymAnalyzer(new TestSynonymEngine())
        .tokenStream("contents", new StringReader("jumps"));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

    stream.reset();
    int i = 0;
    String[] expected = new String[]{"jumps", "hops", "leaps"};
    while (stream.incrementToken()) {
      assertEquals(expected[i], String.copyValueOf(term.buffer()).substring(0, term.length()));

      int expectedPos;
      if (i == 0) {
        expectedPos = 1;
      }
      else {
        expectedPos = 0;
      }
      assertEquals(expectedPos, posIncr.getPositionIncrement());
      i++;
      MetaphoneReplacementFilter.overwrite(term.buffer(), "");
      term.setEmpty();
    }
    assertEquals(3, i);
    stream.close();
  }

  @Test
  public void termQueryTest() throws Exception {
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(new SimpleAnalyzer()));
    Document doc = new Document();
    doc.add(new StringField("partnum", "Q36", Store.YES));
    doc.add(new TextField("description", "Illidium Space Modulator", Store.YES));
    writer.addDocument(doc);
    writer.close();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
    Query query = new TermQuery(new Term("partnum", "Q36"));
    assertEquals(1, hitCount(searcher, query));
  }

  @Test
  public void basicQueryParserTest() throws Exception {
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(new SimpleAnalyzer()));
    Document doc = new Document();
    doc.add(new StringField("partnum", "Q36", Store.NO));
    doc.add(new TextField("description", "Illidium Space Modulator", Store.YES));
    writer.addDocument(doc);
    writer.close();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
    Query query = new QueryParser("description", new SimpleAnalyzer())
        .parse("partnum:Q36 AND SPACE");
    assertEquals("+partnum:q +space", query.toString("description"));
    assertEquals(0, hitCount(searcher, query));
  }

  @Test
  public void perFieldAnalyzerTest() throws Exception {
    IndexWriter writer = new IndexWriter(testPrefs.dir, new IndexWriterConfig(new SimpleAnalyzer()));
    Document doc = new Document();
    doc.add(new StringField("partnum", "Q36", Store.NO));
    doc.add(new TextField("description", "Illidium Space Modulator", Store.YES));
    writer.addDocument(doc);
    writer.close();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));

    Map<String, Analyzer> map = new HashMap<>();
    map.put("partnum", new KeywordAnalyzer());
    PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(), map);
    Query query = new QueryParser("description", wrapper)
        .parse("partnum:Q36 AND SPACE");
    assertEquals("+partnum:Q36 +space", query.toString("description"));
    assertEquals(1, hitCount(searcher, query));
  }
}
