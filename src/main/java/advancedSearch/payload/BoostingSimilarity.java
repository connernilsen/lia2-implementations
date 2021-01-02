package advancedSearch.payload;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.util.BytesRef;

public class BoostingSimilarity extends BM25Similarity {

  @Override
  protected float scorePayload(int doc, int start, int end, BytesRef payload) {
    if (payload != null) {
      return PayloadHelper.decodeFloat(payload.bytes, start);
    }
    else {
      return 1.0F;
    }
  }
}

