package advancedSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.AbstractQueryTest;
import util.AnalyzerUtils;

public class SimilarityTest extends AbstractQueryTest {
  TreeMap<String, Map<String, Long>> categoryMap;

  @BeforeEach
  public void setupMap() throws Exception {
    categoryMap = new TreeMap<>();
    buildCategoryVectors();
  }

  @Test
  public void categorizationTest() throws Exception {
    assertEquals("/technology/computers/programming/methodology",
        getCategory("extreme agile methodology"));
    assertEquals("/education/pedagogy",
        getCategory("montessori education philosophy"));
  }

  private void buildCategoryVectors() throws Exception {
    IndexReader reader = DirectoryReader.open(AnalyzerUtils.testPrefs.dir);
    int maxDoc = reader.maxDoc();

    for (int i = 0; i < maxDoc; i++) {
      Document doc = reader.document(i);
      String category = doc.get("category");

      Map<String, Long> vectorMap = categoryMap
          .computeIfAbsent(category, k -> new TreeMap<>());

      Terms term = reader.getTermVector(i, "subject");
      addTermFreqToMap(vectorMap, term);
    }
  }

  private void addTermFreqToMap(Map<String, Long> vectorMap, Terms termVec)
      throws IOException {
    TermsEnum iter = termVec.iterator();
    List<String> terms = new ArrayList<>();
    List<Long> freqs = new ArrayList<>();
    while (iter.next() != null) {
      terms.add(iter.term().utf8ToString());
      freqs.add(iter.totalTermFreq());
    }

    for (int i = 0; i < terms.size(); i++) {
      String term = terms.get(i);
      if (vectorMap.containsKey(term)) {
        Long value = vectorMap.get(term);
        vectorMap.put(term, value + freqs.get(i));
      }
      else {
        vectorMap.put(term, freqs.get(i));
      }
    }
  }

  private String getCategory(String subject) {
    String[] words = subject.split(" ");

    Iterator<String> categoryIterator = categoryMap.keySet().iterator();
    double bestAngle = Double.MAX_VALUE;
    String bestCategory = null;

    while (categoryIterator.hasNext()) {
      String category = categoryIterator.next();
      double angle = computeAngle(words, category);

      if (angle < bestAngle) {
        bestAngle = angle;
        bestCategory = category;
      }
    }
    return bestCategory;
  }

  private double computeAngle(String[] words, String category) {
    Map<String, Long> vecMap = categoryMap.get(category);

    int dotProd = 0;
    int sumOfSquares = 0;
    for (String word : words) {
      long catWordFreq = 0;

      if (vecMap.containsKey(word)) {
        catWordFreq = vecMap.get(word);
      }

      dotProd += catWordFreq;
      sumOfSquares += catWordFreq * catWordFreq;
    }

    double denom;
    if (sumOfSquares == words.length) {
      denom = sumOfSquares;
    }
    else {
      denom = Math.sqrt(sumOfSquares) * Math.sqrt(words.length);
    }

    double ratio = dotProd / denom;
    return Math.acos(ratio);
  }
}
