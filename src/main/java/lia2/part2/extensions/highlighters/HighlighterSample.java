package lia2.part2.extensions.highlighters;

import java.io.FileWriter;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

public class HighlighterSample {
  private static final String text = "In this section we'll show you how to make the simplest " +
      "programmatic query, searching for a single term, and then " +
      "we'll see how to use QueryParser to accept textual queries. " +
      "In the sections that follow, we’ll take this simple example " +
      "further by detailing all the query types built into Lucene. " +
      "We begin with the simplest search of all: searching for all " +
      "documents that contain a single term.";

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: Highlight <filename-out>");
      System.exit(1);
    }

    String filename = args[0];

    String searchText = "term";
    QueryParser parser = new QueryParser("f", new StandardAnalyzer());

    Query query = parser.parse(searchText);

    SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">",
        "</span>");

    TokenStream tokens = new StandardAnalyzer().tokenStream("f", new StringReader(text));
    QueryScorer scorer = new QueryScorer(query, "f");
    org.apache.lucene.search.highlight.Highlighter highlighter =
        new org.apache.lucene.search.highlight.Highlighter(formatter, scorer);
    highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

    String result = highlighter.getBestFragments(tokens, text, 3, "...");

    FileWriter writer = new FileWriter(filename);
    writer.write("<html>");
    writer.write("<style>\n"
        + ".highlight {\n"
        + "\tbackground: yellow;\n"
        + "}\n"
        + "</style>");
    writer.write("<body>");
    writer.write(result);
    writer.write("</body></html>");
    writer.close();
  }
}
