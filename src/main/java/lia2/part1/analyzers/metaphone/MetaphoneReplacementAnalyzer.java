package lia2.part1.analyzers.metaphone;

import java.io.IOException;
import java.io.StringReader;
import lia2.util.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;

public class MetaphoneReplacementAnalyzer extends Analyzer {

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer tokenizer = new LetterTokenizer();
    tokenizer.setReader(new StringReader(s));
    return new TokenStreamComponents(tokenizer, new MetaphoneReplacementFilter(tokenizer));
  }

  public static void main(String[] args) throws IOException {
    MetaphoneReplacementAnalyzer analyzer = new MetaphoneReplacementAnalyzer();
    AnalyzerUtils.displayTokens(analyzer,
        "The quick brown fox jumped over the lazy dog");
    System.out.println();
    AnalyzerUtils.displayTokens(analyzer,
        "Tha quik brown phox jumpd ovvar the lazi dag");
  }
}
