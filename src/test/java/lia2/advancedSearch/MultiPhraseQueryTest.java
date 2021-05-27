package lia2.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lia2.analyzers.synonym.SynonymAnalyzer;
import lia2.analyzers.synonym.SynonymEngine;
import java.io.IOException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PhraseQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class MultiPhraseQueryTest extends AbstractQueryTest {

  @BeforeAll
  public static void setup() throws Exception {
    setupDir();
    IndexWriter writer = new IndexWriter(testPrefs.dir,
        new IndexWriterConfig(new WhitespaceAnalyzer()));
    Document doc = new Document();
    doc.add(new TextField("field", "the quick brown fox jumped over the lazy dog",
        Store.YES));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new TextField("field", "the fast fox hopped over the hound", Store.YES));
    writer.addDocument(doc);
    writer.close();
    searcher = new IndexSearcher(DirectoryReader.open(testPrefs.dir));
  }

  @Test
  public void basicTest() throws Exception {
    MultiPhraseQuery.Builder query = new MultiPhraseQuery.Builder();
    query.add(new Term[]{new Term("field", "quick"), new Term("field", "fast")});
    query.add(new Term("field", "fox"));
    System.out.println(query);
    TopDocs hits = searcher.search(query.build(), 10);
    assertEquals(1, hits.totalHits.value);
    query.setSlop(1);
    hits = searcher.search(query.build(), 10);
    assertEquals(2, hits.totalHits.value);
  }

  @Test
  public void againstOrTest() throws Exception {
    PhraseQuery.Builder quickFox = new PhraseQuery.Builder();
    quickFox.setSlop(1);
    quickFox.add(new Term("field", "quick"));
    quickFox.add(new Term("field", "fox"));

    PhraseQuery.Builder fastFox = new Builder();
    fastFox.add(new Term("field", "fast"));
    fastFox.add(new Term("field", "fox"));

    BooleanQuery.Builder query = new BooleanQuery.Builder();
    query.add(quickFox.build(), Occur.SHOULD);
    query.add(fastFox.build(), Occur.SHOULD);
    TopDocs hits = searcher.search(query.build(), 10);
    assertEquals(2, hits.totalHits.value);
  }

  @Test
  public void queryParserTest() throws Exception {
    SynonymEngine engine = new SynonymEngine() {
      @Override
      public String[] getSynonyms(String s) throws IOException {
        if (s.equals("quick")) {
          return new String[] {"fast"};
        }
        else {
          return null;
        }
      }
    };

    Query q = new QueryParser("field", new SynonymAnalyzer(engine))
        .parse("\"quick fox\"");
    assertEquals("field:\"(quick fast) fox\"", q.toString());
    assertTrue(q instanceof MultiPhraseQuery);
  }
}
