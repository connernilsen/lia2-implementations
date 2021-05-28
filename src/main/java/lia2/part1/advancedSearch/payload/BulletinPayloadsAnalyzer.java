package lia2.part1.advancedSearch.payload;

import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;

public class BulletinPayloadsAnalyzer extends Analyzer {
  private final float boost;
  private boolean isBulletin;

  public BulletinPayloadsAnalyzer(float boost) {
    this.boost = boost;
  }

  public void setIsBulletin(boolean isBulletin) {
    this.isBulletin = isBulletin;
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer tokenizer = new LetterTokenizer();
    tokenizer.setReader(new StringReader(s));
    return new TokenStreamComponents(tokenizer,
        new BulletinPayloadsFilter(tokenizer, this.boost, this.isBulletin));
  }
}
