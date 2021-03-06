package lia2.part3.extensions.spellcheckers;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import lia2.util.AnalyzerUtils.TestPrefs;

public class CreateSpellCheckerIndex {
  public static TestPrefs getTestPrefs(Directory dir, String indexField) throws Exception {
    TestPrefs prefs = new TestPrefs();

    System.out.println("Building SpellChecker index...");
    SpellChecker spell = new SpellChecker(prefs.dir);
    long start = System.currentTimeMillis();

    try (IndexReader reader = DirectoryReader.open(dir)) {
      spell.indexDictionary(new LuceneDictionary(reader, indexField),
          new IndexWriterConfig(), true);
    }

    long endTime = System.currentTimeMillis();
    System.out.println("\ttook " + (endTime - start) + " milliseconds");
    return prefs;
  }
}
