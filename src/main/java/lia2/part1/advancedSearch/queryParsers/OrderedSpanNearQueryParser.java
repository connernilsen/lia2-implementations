package lia2.part1.advancedSearch.queryParsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

public class OrderedSpanNearQueryParser extends QueryParser {
  public OrderedSpanNearQueryParser(String field, Analyzer a) {
    super(field, a);
  }

  @Override
  protected Query getFieldQuery(String field, String queryText, int slop)
      throws ParseException {
    Query orig = super.getFieldQuery(field, queryText, slop);
    if (!(orig instanceof PhraseQuery)) {
      return orig;
    }

    PhraseQuery pq = (PhraseQuery) orig;
    Term[] terms = pq.getTerms();
    SpanTermQuery[] clauses = new SpanTermQuery[terms.length];
    for (int i = 0; i < terms.length; i++) {
      clauses[i] = new SpanTermQuery(terms[i]);
    }

    return new SpanNearQuery(clauses, slop, true);
  }
}
