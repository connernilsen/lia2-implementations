package lia2.part1.analyzers.metaphone;

import java.io.IOException;
import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class MetaphoneReplacementFilter extends TokenFilter {
  public static final String METAPHONE = "metaphone";
  private Metaphone metaphoner = new Metaphone();
  private CharTermAttribute termAttr;
  private TypeAttribute typeAttr;

  public MetaphoneReplacementFilter(TokenStream stream) {
    super(stream);

    termAttr = addAttribute(CharTermAttribute.class);
    typeAttr = addAttribute(TypeAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }

    String token = new String(termAttr.buffer(), 0, termAttr.length());

    String encoded = metaphoner.encode(token);
    clearAttributes();
    termAttr.append(encoded);
    typeAttr.setType(METAPHONE);
    return true;
  }
}
