package lia2.part1.advancedSearch.payload;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.similarities.BM25Similarity;
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

