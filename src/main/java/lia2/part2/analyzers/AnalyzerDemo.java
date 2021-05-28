package lia2.part2.analyzers;

import java.io.IOException;
import lia2.util.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerDemo {
  private static final String[] examples = {
      "The quick brown fox jumped over the lazy dog",
      "XY&Z Corp - xyz@example.com"
  };

  private static final Analyzer[] analyzers = new Analyzer[] {
      new WhitespaceAnalyzer(),
      new SimpleAnalyzer(),
      new StopAnalyzer(ClassicAnalyzer.STOP_WORDS_SET),
      new StandardAnalyzer()
  };

  public static void main(String[] args) throws IOException {
    String[] strings = examples;

    if (args.length > 0) {
      strings = args;
    }

    for (String text : strings) {
      analyze(text);
    }
  }

  private static void analyze(String text) throws IOException {
    System.out.println("Analyzing \"" + text + "\"");

    for (Analyzer an : analyzers) {
      String name = an.getClass().getSimpleName();
      System.out.println("\t" + name + ":");
      System.out.print("\t\t");
      AnalyzerUtils.displayTokens(an, text);
      System.out.println();
    }

    System.out.println();
    AnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(), "The quick brown fox...");
  }
}
