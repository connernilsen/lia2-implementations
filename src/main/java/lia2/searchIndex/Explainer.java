package lia2.searchIndex;

import java.nio.file.Path;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Explainer {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Args length != 2");
      System.exit(1);
    }

    String indexDir = args[0];
    String queryExpression = args[1];

    Directory dir = FSDirectory.open(Path.of(indexDir));

    QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());
    Query query = parser.parse(queryExpression);

    System.out.println("Query: " + queryExpression);

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
    TopDocs docs = searcher.search(query, 10);
    for (ScoreDoc match : docs.scoreDocs) {
      Explanation exp = searcher.explain(query, match.doc);
      System.out.println("--------");
      Document doc = searcher.doc(match.doc);
      System.out.println(doc.get("title"));
      System.out.println(exp.toString());
    }
    dir.close();
  }
}
