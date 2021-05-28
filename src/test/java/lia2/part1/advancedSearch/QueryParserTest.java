package lia2.part1.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import lia2.part1.advancedSearch.queryParsers.CustomQueryParser;
import lia2.part1.advancedSearch.queryParsers.NumericDateRangeQueryParser;
import lia2.part1.advancedSearch.queryParsers.NumericRangeQueryParser;
import lia2.part1.advancedSearch.queryParsers.OrderedSpanNearQueryParser;
import java.util.Locale;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class QueryParserTest extends AbstractQueryTest {

  @Test
  public void customQueryParser() {
    CustomQueryParser parser = new CustomQueryParser("field", new WhitespaceAnalyzer());

    try {
      parser.parse("a?t");
      fail("Wildcards should fail");
    }
    catch (ParseException ignored) {}

    try {
      parser.parse("xunit~");
      fail("Fuzzy queries should fail");
    }
    catch (ParseException ignored) {}
  }

  @Test
  public void numericRangeQueryTest() throws Exception {
    String expr = "price:[10 TO 20]";
    QueryParser parser = new NumericRangeQueryParser("subject", new WhitespaceAnalyzer());
    Query query = parser.parse(expr);
    assertEquals(expr, query.toString());
  }

  @Test
  public void dateRangeQueryTest() throws Exception {
    String expr = "pubmonth:[01/01/2010 TO 06/01/2010]";
    QueryParser parser = new NumericDateRangeQueryParser("subject", new WhitespaceAnalyzer());

    parser.setDateResolution("pubmonth", Resolution.MONTH);
    parser.setLocale(Locale.US);

    Query query = parser.parse(expr);
    assertEquals("pubmonth:[201001 TO 201006]", query.toString());

    assertTrue(query instanceof TermRangeQuery);
  }

  @Test
  public void phraseQueryTest() throws Exception {
    OrderedSpanNearQueryParser parser = new OrderedSpanNearQueryParser("field",
        new WhitespaceAnalyzer());

    Query query = parser.parse("singleTerm");
    assertTrue(query instanceof TermQuery);

    query = parser.parse("\"a phrase\"");
    assertTrue(query instanceof SpanNearQuery);
  }
}
