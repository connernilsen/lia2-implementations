package lia2.extensions;

import java.nio.file.Path;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import lia2.util.AnalyzerUtils;

public class BooksMoreLikeThis {
  public static void main(String[] args) throws Throwable {
    AnalyzerUtils.setupIndex();
    IndexReader reader = DirectoryReader.open(AnalyzerUtils.testPrefs.dir);
    IndexSearcher searcher = new IndexSearcher(reader);

    int numDocs = reader.maxDoc();
    MoreLikeThis mlt = new MoreLikeThis(reader);
    mlt.setFieldNames(new String[]{"title", "author"});
    mlt.setMinTermFreq(1);
    mlt.setMinDocFreq(1);

    for (int docId = 0; docId < numDocs; docId++) {
      System.out.println();
      Document doc = reader.document(docId);
      System.out.println(doc.get("title"));

      Query query = mlt.like(docId);
      System.out.println("\tquery = " + query);

      TopDocs similarDocs = searcher.search(query, 10);
      if (similarDocs.totalHits.value == 0) {
        System.out.println("\tNone like this");
      }

      for (int i = 0; i < similarDocs.scoreDocs.length; i++) {
        if (similarDocs.scoreDocs[i].doc != docId) {
          doc = reader.document(similarDocs.scoreDocs[i].doc);
          System.out.println("\t-> " + doc.get("title"));
        }
      }
    }
    reader.close();
    AnalyzerUtils.teardown();
  }
}
