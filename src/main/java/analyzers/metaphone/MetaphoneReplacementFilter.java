package analyzers.metaphone;

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

    String encoded = metaphoner.encode(String.copyValueOf(termAttr.buffer()));
    overwrite(termAttr.buffer(), encoded);
    typeAttr.setType(METAPHONE);
    return true;
  }

  public static void overwrite(char[] buff, String value) {
    for (int i = 0; i < buff.length; i++) {
      if (i < value.length()) {
        buff[i] = value.charAt(i);
      }
      else {
        buff[i] = '\u0000';
      }
    }
  }
}
