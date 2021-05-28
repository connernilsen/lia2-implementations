package lia2.part1.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class MultiFieldParseTest extends AbstractQueryTest {

  @Test
  public void defaultOperatorTest() throws Exception {
    Query query = new MultiFieldQueryParser(new String[]{"title", "subject"},
        new SimpleAnalyzer()).parse("development");

    TopDocs hits = searcher.search(query, 10);
    System.out.println(query);
    assertTrue(hitsIncludeTitle(searcher, hits, "Ant in Action"));
    assertTrue(hitsIncludeTitle(searcher, hits, "Extreme Programming Explained"));
  }

  @Test
  public void specifiedOperatorTest() throws Exception {
    Query query = MultiFieldQueryParser.parse("lucene",
        new String[]{"title", "subject"}, new Occur[]{Occur.MUST, Occur.MUST},
        new SimpleAnalyzer());

    TopDocs hits = searcher.search(query, 10);
    System.out.println(query);
    assertTrue(hitsIncludeTitle(searcher, hits, "Lucene in Action, Second Edition"));
    assertEquals(1, hits.scoreDocs.length);
  }
}
