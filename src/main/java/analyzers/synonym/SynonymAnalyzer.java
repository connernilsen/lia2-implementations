package analyzers.synonym;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import util.AnalyzerUtils;

public class SynonymAnalyzer extends Analyzer {
  private final SynonymEngine engine;

  public SynonymAnalyzer(SynonymEngine engine) {
    super();
    this.engine = engine;
  }

  public static void main(String[] args) throws IOException {
    Analyzer analyzer = new SynonymAnalyzer(new TestSynonymEngine());
    AnalyzerUtils.displayTokens(analyzer,
        "The quick brown fox jumps over the lazy dog");
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer tokenizer = new StandardTokenizer();
    tokenizer.setReader(new StringReader(s));
    return new TokenStreamComponents(tokenizer,
        new SynonymFilter(
            new StopFilter(new LowerCaseFilter(tokenizer), ClassicAnalyzer.STOP_WORDS_SET),
            engine));
  }

  public static class TestSynonymEngine implements SynonymEngine {
    private static final HashMap<String, String[]> map = new HashMap<>();

    static {
      map.put("quick", new String[]{"fast", "speedy"});
      map.put("jumps", new String[]{"leaps", "hops"});
      map.put("over", new String[]{"above"});
      map.put("lazy", new String[]{"apathetic", "sluggish"});
      map.put("dog", new String[]{"canine", "pooch"});
    }

    @Override
    public String[] getSynonyms(String s) {
      return map.get(s);
    }
  }

}
