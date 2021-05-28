package lia2.part2.advancedSearch.queryParsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

public class CustomQueryParser extends QueryParser {

  public CustomQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  @Override
  protected final Query getWildcardQuery(String field, String termString)
      throws ParseException {
    throw new ParseException("Wildcards not allowed");
  }

  @Override
  protected Query getFuzzyQuery(String field, String term, float minSimilarity)
      throws ParseException {
    throw new ParseException("Fuzzy queries not allowed");
  }
}
