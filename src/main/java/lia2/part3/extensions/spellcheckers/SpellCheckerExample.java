package lia2.part3.extensions.spellcheckers;

import lia2.util.AnalyzerUtils;
import lia2.util.AnalyzerUtils.TestPrefs;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;

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
