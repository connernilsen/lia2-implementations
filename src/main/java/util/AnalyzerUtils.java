package util;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AnalyzerUtils {
  public static void displayTokens(Analyzer analyzer, String text) throws IOException {
    displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
  }

  public static void displayTokens(TokenStream stream) throws IOException {
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    while (stream.incrementToken()) {
      System.out.print("[" + String.copyValueOf(term.buffer()) + "] ");
    }
    stream.close();
  }

  public static void displayTokensWithFullDetails(Analyzer analyzer, String text)
      throws IOException {
    TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);
    OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
    TypeAttribute type = stream.addAttribute(TypeAttribute.class);

    int position = 0;
    stream.reset();
    while (stream.incrementToken()) {
      int increment = posIncr.getPositionIncrement();
      if (increment > 0) {
        position = position + increment;
        System.out.println();
        System.out.println(position + ": ");
      }

      System.out.println("[" + String.copyValueOf(term.buffer()) + ":" +
          offset.startOffset() + "->" + offset.endOffset()
          + ":" + type.type() + "]");
      System.out.println();
    }
    stream.close();
  }
}
