package lia2.advancedSearch;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import lia2.util.AnalyzerUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;

public class SortingEx {

  public void displayResults(Query query, Sort sort) throws IOException {
    IndexSearcher searcher = AnalyzerUtils.searcher;

    TopDocs results = searcher.search(query, 20, sort, true);
    System.out.println("\nResults for " + query.toString() + " sorted by " + sort);

    System.out.println(StringUtils.rightPad("Title", 30)
        + StringUtils.rightPad("pubmonth", 10)
        + StringUtils.center("id", 4)
        + StringUtils.center("score", 15));

    PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    DecimalFormat scoreFormatter = new DecimalFormat("0.######");
    for (ScoreDoc sd : results.scoreDocs) {
      int docId = sd.doc;
      float score = sd.score;
      Document doc = searcher.doc(docId);

      System.out.println(StringUtils.rightPad(
          StringUtils.abbreviate(doc.get("title"), 29), 30)
      + StringUtils.rightPad(doc.get("pubmonth"), 10)
      + StringUtils.center("" + docId, 4)
      + StringUtils.leftPad(scoreFormatter.format(score), 12));
      out.println("    " + doc.get("category"));
    }
  }

  public static void main(String[] args) throws Exception {
    Query allBooks = new MatchAllDocsQuery();
    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    BooleanQuery.Builder query = new BooleanQuery.Builder();
    query.add(allBooks, Occur.SHOULD);
    query.add(parser.parse("java OR action"), Occur.SHOULD);
    AnalyzerUtils.setupIndex();

    SortingEx ex = new SortingEx();
    try {
      ex.displayResults(query.build(), Sort.RELEVANCE);
      ex.displayResults(query.build(), Sort.INDEXORDER);
      ex.displayResults(query.build(), new Sort(new SortField("category", Type.STRING)));
      ex.displayResults(query.build(), new Sort(new SortField("pubmonth", Type.INT, true)));
      ex.displayResults(query.build(), new Sort(
          new SortField("category", Type.STRING), SortField.FIELD_SCORE,
          new SortField("pubmonth", Type.INT, true)));
      ex.displayResults(query.build(), new Sort(SortField.FIELD_SCORE,
          new SortField("category", Type.STRING)));
    }
    finally {
      AnalyzerUtils.teardown();
    }
  }
}
