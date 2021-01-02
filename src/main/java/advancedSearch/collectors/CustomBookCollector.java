package advancedSearch.collectors;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;

public class CustomBookCollector implements Collector {
  private Map<String, String> documents = new HashMap<>();

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext leafReaderContext) throws IOException {
    return new CustomLeafCollector(leafReaderContext);
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE;
  }


  public Map<String, String> getLinks() {
    return this.documents;
  }

  private class CustomLeafCollector implements LeafCollector {
    private Scorable scorer;
    private Map<Integer, String> urls = new HashMap<>();
    private Map<Integer, String> titles = new HashMap<>();

    public CustomLeafCollector(LeafReaderContext ctx) throws IOException {
      BinaryDocValues urlTerms = DocValues.getBinary(ctx.reader(), "url");
      BinaryDocValues titleTerms = DocValues.getBinary(ctx.reader(), "title");
      for (int urlId = urlTerms.nextDoc(); urlId != BinaryDocValues.NO_MORE_DOCS;
          urlId = urlTerms.nextDoc()) {
        String urlTerm = urlTerms.binaryValue().utf8ToString();
        urls.put(urlId, urlTerm);
      }

      for (int titleId = titleTerms.nextDoc(); titleId != BinaryDocValues.NO_MORE_DOCS;
          titleId = titleTerms.nextDoc()) {
        String titleTerm = titleTerms.binaryValue().utf8ToString();
        titles.put(titleId, titleTerm);
      }
    }

    @Override
    public void setScorer(Scorable scorable) throws IOException {
      this.scorer = scorable;
    }

    @Override
    public void collect(int i) throws IOException {
      String url = urls.get(i);
      String title = titles.get(i);

      documents.put(url, title);
      System.out.println(title + ":" + scorer.score());
    }
  }
}
