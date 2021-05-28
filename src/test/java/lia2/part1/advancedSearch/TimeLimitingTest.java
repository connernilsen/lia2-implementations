package lia2.part1.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Counter;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class TimeLimitingTest extends AbstractQueryTest {

  @Test
  public void timeLimitingCollectorTest() throws Exception {
    Query q = new MatchAllDocsQuery();
    long numAllBooks = hitCount(searcher, q);

    TopScoreDocCollector topDocs = TopScoreDocCollector.create(10, Integer.MAX_VALUE);
    Collector collector = new TimeLimitingCollector(topDocs, Counter.newCounter(),
        1000L);

    try {
      searcher.search(q, collector);
      assertEquals(numAllBooks, topDocs.getTotalHits());
    }
    catch (TimeExceededException ex) {
      System.out.println("Too much time taken");
    }
  }
}
