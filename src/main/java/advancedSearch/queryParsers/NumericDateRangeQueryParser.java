package advancedSearch.queryParsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.IntRange;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

public class NumericDateRangeQueryParser extends QueryParser {

  public NumericDateRangeQueryParser(String field, Analyzer a) {
    super(field, a);
  }

  public Query getRangeQuery(String field, String part1, String part2, boolean inclusive)
      throws ParseException {
    TermRangeQuery query = (TermRangeQuery) super.getRangeQuery(field, part1, part2,
        inclusive, inclusive);

    if ("pubmonth".equals(field)) {
      return IntRange.newContainsQuery("pubmonth",
          new int[]{Integer.parseInt(query.getLowerTerm().utf8ToString())},
          new int[]{Integer.parseInt(query.getUpperTerm().utf8ToString())});
    }
    else {
      return query;
    }
  }
}
