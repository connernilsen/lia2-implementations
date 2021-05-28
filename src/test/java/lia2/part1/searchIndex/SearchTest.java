package lia2.part1.searchIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class SearchTest extends AbstractQueryTest {
  @Test
  public void termTest() throws Exception{
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
    QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());
    Query query = parser.parse("+JUNIT +ANT -MOCK");
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);
    Document d = searcher.doc(docs.scoreDocs[0].doc);
    assertEquals("Ant in Action", d.get("title"));
    query = parser.parse("mock OR junit");
    docs = searcher.search(query, 10);
    assertEquals(2, docs.totalHits.value);
  }

  @Test
  public void runExplainer() throws Exception {
    Explainer.main(new String[]{testPrefs.dirPointer.getPath(), "junit"});
  }
}
