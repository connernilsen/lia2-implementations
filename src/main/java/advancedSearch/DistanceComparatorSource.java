package advancedSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.LeafFieldComparator;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.util.BytesRef;

public class DistanceComparatorSource extends FieldComparatorSource {
  private final int x;
  private final int y;

  public DistanceComparatorSource(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public FieldComparator<Float> newComparator(String fieldName, int numHits,
      int sortPos, boolean reversed) {
    return new DistanceScoreDocLookupComparator(fieldName, numHits);
  }

  @Override
  public String toString() {
    return "Distance from (" + x + "," + y + ")";
  }

  private class DistanceScoreDocLookupComparator extends FieldComparator<Float> {
    private long[] xDoc = new long[4];
    private long[] yDoc = new long[4];
    private final float[] values;
    private int bottom;
    private float top;
    final String fieldName;

    public DistanceScoreDocLookupComparator(String fieldName, int numHits) {
      super();
      this.values = new float[numHits];
      this.fieldName = fieldName;
    }

    @Override
    public int compare(int slot1, int slot2) {
      return Float.compare(values[slot1], values[slot2]);
    }

    @Override
    public void setTopValue(Float top) {
      this.top = top;
    }

    @Override
    public Float value(int i) {
      return values[i];
    }

    @Override
    public LeafFieldComparator getLeafComparator(LeafReaderContext leafReaderContext)
        throws IOException {
      TermsEnum terms = leafReaderContext.reader().terms("location").iterator();
      for (BytesRef termVal = terms.next(); termVal != null; termVal = terms.next()) {
        String[] term = termVal.utf8ToString().split(",");
        try {
          // :( this is disgusting but I'm really struggling to figure out clean access to stored
          // fields without document id
          int pos = (int) terms.termState().getClass()
              .getField(terms.termState().getClass().getFields()[5].getName())
              .get(terms.termState());
          xDoc[pos] = Long.parseLong(term[0]);
          yDoc[pos] = Long.parseLong(term[1]);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
      return new ScoreDocLeafComparator();
    }

    private class ScoreDocLeafComparator implements LeafFieldComparator {

      @Override
      public void setBottom(int bottom) throws IOException {
        DistanceScoreDocLookupComparator.this.bottom = bottom;
      }

      @Override
      public int compareBottom(int i) throws IOException {
        float docDist = getDistance(i);
        return Float.compare(values[bottom], docDist);
      }

      @Override
      public int compareTop(int i) throws IOException {
        float docDist = getDistance(i);
        return Float.compare(top, docDist);
      }

      @Override
      public void copy(int slot, int doc) throws IOException {
        DistanceScoreDocLookupComparator.this.values[slot] = getDistance(doc);
      }

      @Override
      public void setScorer(Scorable scorable) throws IOException { }

      private float getDistance(int doc) {
        long deltax = xDoc[doc] - x;
        long deltay = yDoc[doc] - y;
        return (float) Math.sqrt(deltax * deltax + deltay * deltay);
      }
    }
  }
}
