package advancedSearch.payload;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.util.BytesRef;

public class BulletinPayloadsFilter extends TokenFilter {
  private final CharTermAttribute termAtt;
  private final PayloadAttribute payloadAtt;
  private final boolean isBulletin;
  private final BytesRef boostPayload;

  public BulletinPayloadsFilter(TokenStream in, float warningBoost, boolean isBulletin) {
    super(in);
    this.payloadAtt = addAttribute(PayloadAttribute.class);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.boostPayload = new BytesRef(PayloadHelper.encodeFloat(warningBoost));
    this.isBulletin = isBulletin;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (isBulletin && String.copyValueOf(termAtt.buffer()).substring(0, termAtt.length())
          .equals("warning")) {
        payloadAtt.setPayload(boostPayload);
      }
      else {
        payloadAtt.setPayload(null);
      }
      return true;
    }
    return false;
  }
}
