package extensions.spellcheckers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import util.AnalyzerUtils;
import util.AnalyzerUtils.TestPrefs;

public class SpellCheckerExample {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: wordToRespell");
      System.exit(1);
    }

    String wordToRespell = args[0];

    AnalyzerUtils.setupIndex();

    TestPrefs prefs = CreateSpellCheckerIndex.getTestPrefs(AnalyzerUtils.testPrefs.dir,
        "title");


    SpellChecker spell = new SpellChecker(prefs.dir);
    spell.setStringDistance(new LevenshteinDistance());
    String[] suggestions = spell.suggestSimilar(wordToRespell, 5);
    System.out.println(suggestions.length + " suggestions for '" + wordToRespell + "':");
    for (String suggestion : suggestions) {
      System.out.println("\t" + suggestion);
    }

    AnalyzerUtils.teardown();
    AnalyzerUtils.teardown(prefs);
  }

}
