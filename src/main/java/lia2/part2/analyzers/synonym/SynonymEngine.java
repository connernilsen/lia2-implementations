package lia2.part2.analyzers.synonym;

import java.io.IOException;

public interface SynonymEngine {
  String[] getSynonyms(String s) throws IOException;
}
