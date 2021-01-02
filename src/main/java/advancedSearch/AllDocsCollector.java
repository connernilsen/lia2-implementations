package advancedSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;

public class AllDocsCollector implements Collector {
  private List<ScoreDoc> docs = new ArrayList<>();

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext leafReaderContext) throws IOException {
    return new CustomAllDocsLeafCollector(leafReaderContext);
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE;
  }

  public void reset() {
    docs.clear();
  }

  public List<ScoreDoc> getHits() {
    return docs;
  }

  private class CustomAllDocsLeafCollector implements LeafCollector {
    private Scorable scorer;
    private final int docBase;

    public CustomAllDocsLeafCollector(LeafReaderContext ctx) {
      this.docBase = ctx.docBase;
    }

    @Override
    public void setScorer(Scorable scorable) throws IOException {
      this.scorer = scorable;
    }

    @Override
    public void collect(int i) throws IOException {
      docs.add(new ScoreDoc(docBase + i, scorer.score()));
    }
  }
}
