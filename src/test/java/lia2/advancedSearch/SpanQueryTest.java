package lia2.advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lia2.util.AbstractQueryTest;

public class SpanQueryTest extends AbstractQueryTest {
  private static SpanTermQuery quick;
  private static SpanTermQuery brown;
  private static SpanTermQuery red;
  private static SpanTermQuery fox;
  private static SpanTermQuery lazy;
  private static SpanTermQuery sleepy;
  private static SpanTermQuery dog;
  private static SpanTermQuery cat;
  private static Analyzer analyzer;
  private static IndexReader reader;

  @BeforeAll
  public static void setup() throws Exception {
    setupDir();
    IndexWriter writer = new IndexWriter(testPrefs.dir,
        new IndexWriterConfig(new WhitespaceAnalyzer()));
    Document doc = new Document();
    doc.add(new TextField("f", "the quick brown fox jumps over the lazy dog",
        Store.YES));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new TextField("f", "the quick red fox jumps over the sleepy cat",
        Store.YES));
    writer.addDocument(doc);
    writer.close();
    quick = new SpanTermQuery(new Term("f", "quick"));
    brown = new SpanTermQuery(new Term("f", "brown"));
    red = new SpanTermQuery(new Term("f", "red"));
    fox = new SpanTermQuery(new Term("f", "fox"));
    lazy = new SpanTermQuery(new Term("f", "lazy"));
    sleepy = new SpanTermQuery(new Term("f", "sleepy"));
    dog = new SpanTermQuery(new Term("f", "dog"));
    cat = new SpanTermQuery(new Term("f", "cat"));
    reader = DirectoryReader.open(testPrefs.dir);
    searcher = new IndexSearcher(reader);
  }

  private void assertOnlyBrownFox(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(1, hits.totalHits.value);
    assertEquals(0, hits.scoreDocs[0].doc);
  }

  private void assertBothFoxes(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(2, hits.totalHits.value);
  }

  private void assertNoMatches(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertEquals(0, hits.totalHits.value);
  }

  @Test
  public void spanFirstQueryTest() throws Exception {
    SpanFirstQuery sfq = new SpanFirstQuery(brown, 2);
    assertNoMatches(sfq);
    sfq = new SpanFirstQuery(brown, 3);
    assertOnlyBrownFox(sfq);
  }

  @Test
  public void spanNearQueryTest() throws Exception {
    SpanQuery[] quick_brown_dog = new SpanQuery[]{quick, brown, dog};
    SpanNearQuery snq = new SpanNearQuery(quick_brown_dog, 0, true);
    assertNoMatches(snq);

    snq = new SpanNearQuery(quick_brown_dog, 4, true);
    assertNoMatches(snq);

    snq = new SpanNearQuery(quick_brown_dog, 5, true);
    assertOnlyBrownFox(snq);

    snq = new SpanNearQuery(new SpanQuery[]{lazy, fox}, 3, false);
    assertOnlyBrownFox(snq);
    snq = new SpanNearQuery(new SpanQuery[]{fox, lazy}, 3, false);
    assertOnlyBrownFox(snq);

    PhraseQuery.Builder pq = new PhraseQuery.Builder();
    pq.add(new Term("f", "lazy"));
    pq.add(new Term("f", "fox"));
    pq.setSlop(4);
    assertNoMatches(pq.build());

    pq.setSlop(5);
    assertOnlyBrownFox(pq.build());
  }

  @Test
  public void spanNotQueryTest() throws Exception {
    SpanNearQuery quick_fox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    assertBothFoxes(quick_fox);

    SpanNotQuery quick_fox_dog = new SpanNotQuery(quick_fox, dog);
    assertBothFoxes(quick_fox_dog);

    SpanNotQuery no_quick_red_fox = new SpanNotQuery(quick_fox, red);
    assertOnlyBrownFox(no_quick_red_fox);
  }

  @Test
  public void spanOrQueryTest() throws Exception {
    SpanNearQuery quick_fox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanNearQuery lazy_dog = new SpanNearQuery(new SpanQuery[]{lazy, dog}, 0, true);
    SpanNearQuery sleepy_cat = new SpanNearQuery(new SpanQuery[]{sleepy, cat}, 0, true);

    SpanNearQuery qf_near_ld = new SpanNearQuery(new SpanQuery[]{quick_fox, lazy_dog}, 3, true);
    assertOnlyBrownFox(qf_near_ld);

    SpanNearQuery qf_near_sc = new SpanNearQuery(new SpanQuery[]{quick_fox, sleepy_cat}, 3, true);

    SpanOrQuery or = new SpanOrQuery(qf_near_ld, qf_near_sc);
    assertBothFoxes(or);
  }
}
