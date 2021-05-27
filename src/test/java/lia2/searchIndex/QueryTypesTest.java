package lia2.searchIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lia2.util.AbstractQueryTest;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

public class QueryTypesTest extends AbstractQueryTest {
  @Test
  public void keywordTest() throws Exception {
    Term t = new Term("isbn", "9781935182023");
    Query query = new TermQuery(t);
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);
  }

  @Test
  public void termRangeQueryTest() throws Exception {
    TermRangeQuery query = new TermRangeQuery("title2", new BytesRef("d"),
        new BytesRef("j"), true, true);
    TopDocs matches = searcher.search(query, 100);
    assertEquals(3, matches.totalHits.value);
  }

  @Test
  public void inclusiveNumericTest() throws Exception {
    Query query = IntPoint.newRangeQuery("pubmonth", 200605, 200609);
    TopDocs matches = searcher.search(query, 10);
    assertEquals(1, matches.totalHits.value);
  }

  @Test
  public void exclusiveNumericTest() throws Exception {
    Query query = IntPoint.newRangeQuery("pubmonth", Math.addExact(200605, 1),
        Math.addExact(200609, -1));
    TopDocs matches = searcher.search(query, 10);
    assertEquals(0, matches.totalHits.value);
  }

  @Test
  public void prefixTest() throws Exception {
    Term term = new Term("category", "/technology/computers/programming");
    PrefixQuery query = new PrefixQuery(term);
    TopDocs matches = searcher.search(query, 10);
    long programmingAndBelow = matches.totalHits.value;
    matches = searcher.search(new TermQuery(term),  10);
    long justProgramming = matches.totalHits.value;
    assertTrue(programmingAndBelow > justProgramming);
  }

  @Test
  public void andTest() throws Exception {
    TermQuery searchingBooks = new TermQuery(new Term("subject", "search"));
    Query books2010 = IntPoint.newRangeQuery("pubmonth", 201001, 201012);
    BooleanQuery.Builder searchigBooks2010 = new BooleanQuery.Builder();
    searchigBooks2010.add(searchingBooks, Occur.MUST);
    searchigBooks2010.add(books2010, Occur.MUST);

    TopDocs matches = searcher.search(searchigBooks2010.build(), 10);
    assertTrue(hitsIncludeTitle(searcher, matches, "Lucene in Action, Second Edition"));
  }

  @Test
  public void orTest() throws Exception {
    TermQuery methodologyBooks = new TermQuery(
        new Term("category", "/technology/computers/programming/methodology"));
    TermQuery easternPhiloBooks = new TermQuery(
        new Term("category", "/philosophy/eastern"));

    BooleanQuery.Builder enlightenmentBooks = new BooleanQuery.Builder();
    enlightenmentBooks.add(methodologyBooks, Occur.SHOULD);
    enlightenmentBooks.add(easternPhiloBooks, Occur.SHOULD);

    TopDocs matches = searcher.search(enlightenmentBooks.build(), 10);
    System.out.println("or = " + enlightenmentBooks);
    assertTrue(hitsIncludeTitle(searcher, matches, "Extreme Programming Explained"));
    assertTrue(hitsIncludeTitle(searcher, matches, "Tao Te Ching \u9053\u5FB7\uu7D93"));
  }

  @Test
  public void slopComparisonTest() throws Exception {
    TestPrefs prefs = setupPhraseQuery();
    String[] phrase = new String[]{"quick", "fox"};
    assertFalse(slopMatched(phrase, 0, prefs.getSearcher()));
    assertTrue(slopMatched(phrase, 1, prefs.getSearcher()));
    prefs.tearDown();
  }

  @Test
  public void slopComparisonReverseTest() throws Exception {
    TestPrefs prefs = setupPhraseQuery();
    String[] phrase = new String[]{"fox", "quick"};
    assertFalse(slopMatched(phrase, 2, prefs.getSearcher()));
    assertTrue(slopMatched(phrase, 3, prefs.getSearcher()));
    prefs.tearDown();
  }

  @Test
  public void slopMultipleTest() throws Exception {
    TestPrefs prefs = setupPhraseQuery();
    assertFalse(slopMatched(new String[]{"quick", "jumped", "lazy"}, 3, prefs.getSearcher()));
    assertTrue(slopMatched(new String[]{"quick", "jumped", "lazy"}, 4, prefs.getSearcher()));
    assertFalse(slopMatched(new String[]{"lazy", "jumped", "quick"}, 7, prefs.getSearcher()));
    assertTrue(slopMatched(new String[]{"lazy", "jumped", "quick"}, 8, prefs.getSearcher()));
    prefs.tearDown();
  }

  @Test
  public void wildcardTest() throws Exception {
    Field[] fields = new Field[]{
        new TextField("contents", "wild", Store.YES),
        new TextField("contents", "child", Store.YES),
        new TextField("contents", "mild", Store.YES),
        new TextField("contents", "mildew", Store.YES)
    };
    TestPrefs prefs = indexSingleFieldDocs(fields);
    Query query = new WildcardQuery(new Term("contents", "?ild*"));
    TopDocs matches = prefs.getSearcher().search(query, 10);
    assertEquals(3, matches.totalHits.value);
    assertEquals(matches.scoreDocs[0].score, matches.scoreDocs[1].score, 0.0);
    assertEquals(matches.scoreDocs[1].score, matches.scoreDocs[2].score, 0.0);
    prefs.tearDown();
  }

  @Test
  public void fuzzyTest() throws Exception {
    Field[] fields = new Field[]{
        new TextField("contents", "fuzzy", Store.YES),
        new TextField("contents", "wuzzy", Store.YES)
    };
    TestPrefs prefs = indexSingleFieldDocs(fields);
    Query query = new FuzzyQuery(new Term("contents", "wuzza"));
    TopDocs matches = prefs.getSearcher().search(query, 10);
    assertEquals(2, matches.totalHits.value);
    assertTrue(matches.scoreDocs[0].score != matches.scoreDocs[1].score);
    Document doc = prefs.getSearcher().doc(matches.scoreDocs[0].doc);
    assertEquals("wuzzy", doc.get("contents"));
    prefs.tearDown();
  }

  @Test
  public void termRangeQueryConversionTest() throws Exception {
    Query query = new QueryParser("subject", new StandardAnalyzer())
        .parse("title2:[Q TO V]");

    assertTrue(query instanceof TermRangeQuery);
    TopDocs matches = searcher.search(query, 10);
    assertTrue(hitsIncludeTitle(searcher, matches, "Tapestry in Action"));

    query = new QueryParser("subject", new StandardAnalyzer())
        .parse("title2:{Q TO \"Tapestry in Action\"}");
    matches = searcher.search(query, 10);
    assertFalse(hitsIncludeTitle(searcher, matches, "Tapestry in Action"));
  }

  @Test
  public void lowercasingTest() throws Exception {
    Query q = new QueryParser("field", new StandardAnalyzer()).parse("PrefixQuery*");
    assertEquals("prefixquery*", q.toString("field"));
    QueryParser qp = new QueryParser("field", new StandardAnalyzer());
  }

  @Test
  public void phraseQueryConversionTest() throws Exception {
    Query q = new QueryParser("field", new StandardAnalyzer())
        .parse("\"This is Some Phrase*\"");
    assertEquals("\"this is some phrase\"", q.toString("field"));
    q = new QueryParser("field", new StandardAnalyzer()).parse("\"term\"");
    assertTrue(q instanceof TermQuery);
  }

  @Test
  public void slopConversionTest() throws Exception {
    Query q = new QueryParser("field", new StandardAnalyzer()).parse("\"exact phrase\"");
    assertEquals("\"exact phrase\"", q.toString("field"));
    QueryParser qp = new QueryParser("field", new StandardAnalyzer());
    qp.setPhraseSlop(5);
    q = qp.parse("\"sloppy phrase\"");
    assertEquals("\"sloppy phrase\"~5", q.toString("field"));
  }

  @Test
  public void groupingTest() throws Exception {
    Query query = new QueryParser("subject", new StandardAnalyzer())
        .parse("(agile OR extreme) AND methodology");
    TopDocs matches = searcher.search(query, 10);

    assertTrue(hitsIncludeTitle(searcher, matches, "Extreme Programming Explained"));
    assertTrue(hitsIncludeTitle(searcher, matches, "The Pragmatic Programmer"));
  }
}
