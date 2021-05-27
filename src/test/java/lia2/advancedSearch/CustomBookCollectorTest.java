package lia2.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lia2.advancedSearch.collectors.AllDocsCollector;
import lia2.advancedSearch.collectors.CustomBookCollector;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class CustomBookCollectorTest extends AbstractQueryTest {

  @Test
  public void collectorTest() throws Exception {
    TermQuery query = new TermQuery(new Term("contents", "junit"));
    CustomBookCollector collector = new CustomBookCollector();
    searcher.search(query, collector);

    Map<String, String> linkMap = collector.getLinks();
    assertEquals("Ant in Action",
        linkMap.get("http://www.manning.com/loughran"));
  }

  @Test
  public void collectAllDocsTest() throws Exception {
    TermQuery query = new TermQuery(new Term("contents", "junit"));
    AllDocsCollector collector = new AllDocsCollector();
    searcher.search(query, collector);
    List<ScoreDoc> docs = collector.getHits();
    assertEquals(2, docs.size());
  }
}
