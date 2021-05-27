package lia2.buildSearchIndex;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchIndex {
  public static void main(String[] args) throws IOException, ParseException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Args length != 2");
    }
    String indexDir = args[0];
    String q = args[1];

    search(indexDir, q);
  }

  public static void search(String indexDir, String q)
      throws IOException, ParseException {
    Directory dir = FSDirectory.open(Path.of(indexDir));
    IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    Query query = parser.parse(q);
    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 10);
    long end = System.currentTimeMillis();
    System.err.println("Found " + hits.totalHits + " document(s) (in " +
        (end - start) + " milliseconds) that matched query '" + q + "':");

    for (ScoreDoc scoreDoc : hits.scoreDocs) {
      Document doc = is.doc(scoreDoc.doc);
      System.out.println(doc.get("fullpath"));
    }
  }
}
