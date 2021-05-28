package lia2.part1.analyzers.stemming;

import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;

public class PositionalPorterStopAnalyzer extends Analyzer {
  private final CharArraySet stopWords;

  public PositionalPorterStopAnalyzer() {
    this(ClassicAnalyzer.STOP_WORDS_SET);
  }

  public PositionalPorterStopAnalyzer(CharArraySet stopWords) {
    this.stopWords = stopWords;
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer tokenizer = new LetterTokenizer();
    tokenizer.setReader(new StringReader(s));
    return new TokenStreamComponents(tokenizer,
        new PorterStemFilter(
            new StopFilter(
                new LowerCaseFilter(tokenizer), stopWords)));
  }
}
