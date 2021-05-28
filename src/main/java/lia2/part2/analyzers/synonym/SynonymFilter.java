package lia2.part2.analyzers.synonym;

import java.io.IOException;
import java.util.Stack;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
  public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
  private Stack<String> synonymStack;
  private SynonymEngine engine;
  private AttributeSource.State current;
  private final CharTermAttribute termAtt;
  private final PositionIncrementAttribute posIncrAtt;

  public SynonymFilter(TokenStream in, SynonymEngine engine) {
    super(in);
    this.engine = engine;
    this.synonymStack = new Stack<>();
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (synonymStack.size() > 0) {
      String syn = synonymStack.pop();
      restoreState(current);
      clearAttributes();
      termAtt.append(syn);
      posIncrAtt.setPositionIncrement(0);
      return true;
    }

    clearAttributes();
    if (!input.incrementToken()) {
      return false;
    }
    if (this.addAliasesToStack()) {
      current = captureState();
    }
    return true;
  }

  private boolean addAliasesToStack() throws IOException {
    String token = new String(termAtt.buffer(), 0, termAtt.length());
    String[] synonyms = engine.getSynonyms(token);
    if (synonyms == null) {
      return false;
    }

    for (String syn : synonyms) {
      this.synonymStack.push(syn);
    }
    return true;
  }
}
