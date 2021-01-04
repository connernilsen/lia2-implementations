package extensions.highlighters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.junit.jupiter.api.Test;
import util.AbstractQueryTest;

public class CustomHighlighterTest extends AbstractQueryTest {
  @Test
  public void highlighterTest() throws IOException, InvalidTokenOffsetsException {
    String text = "The quick brown fox jumps over the lazy dog";
    TermQuery query = new TermQuery(new Term("field", "fox"));

    TokenStream tokenStream = new SimpleAnalyzer()
        .tokenStream("field", new StringReader(text));

    QueryScorer scorer = new QueryScorer(query, "field");
    Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
    Highlighter highlighter = new Highlighter(scorer);
    highlighter.setTextFragmenter(fragmenter);
    assertEquals("The quick brown <B>fox</B> jumps over the lazy dog",
        highlighter.getBestFragment(tokenStream, text));
  }

  @Test
  public void hitsTest() throws Exception {
    TermQuery query = new TermQuery(new Term("title", "action"));
    TopDocs hits = searcher.search(query, 10);

    QueryScorer scorer = new QueryScorer(query, "title");

    Highlighter highlighter = new Highlighter(scorer);
    highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

    Analyzer analyzer = new SimpleAnalyzer();

    for (ScoreDoc sd : hits.scoreDocs) {
      Document doc = searcher.doc(sd.doc);
      String title = doc.get("title");

      TokenStream stream = TokenSources.getTokenStream("title",
          searcher.getIndexReader().getTermVectors(sd.doc), "", analyzer, 100);

      String fragment = highlighter.getBestFragment(stream, title);
      System.out.println(fragment);
    }
  }
}
