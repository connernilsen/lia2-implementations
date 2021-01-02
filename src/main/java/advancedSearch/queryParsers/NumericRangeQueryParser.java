package advancedSearch.queryParsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoubleRange;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

public class NumericRangeQueryParser extends QueryParser {

  public NumericRangeQueryParser(String field, Analyzer a) {
    super(field, a);
  }

  public Query getRangeQuery(String field, String part1, String part2, boolean inclusive)
      throws ParseException {
    TermRangeQuery query = (TermRangeQuery)
        super.getRangeQuery(field, part1, part2, inclusive, inclusive);

    if ("price".equals(field)) {
      return DoubleRange.newContainsQuery("price",
          new double[]{Double.parseDouble(query.getLowerTerm().utf8ToString())},
          new double[]{Double.parseDouble(query.getUpperTerm().utf8ToString())});
    }
    else {
      return query;
    }
  }
}
