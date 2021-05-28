package lia2.part2.advancedSearch;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import lia2.util.AnalyzerUtils;

public class SimilarBooks {
  private final IndexReader reader;
  private final IndexSearcher searcher;

  public SimilarBooks(IndexReader reader) {
    this.reader = reader;
    this.searcher = new IndexSearcher(reader);
  }

  public static void main(String[] args) throws Exception {
    AnalyzerUtils.setupIndex();
    IndexReader reader = DirectoryReader.open(AnalyzerUtils.testPrefs.dir);
    int numDocs = reader.maxDoc();

    SimilarBooks sb = new SimilarBooks(reader);

    for (int i = 0; i < numDocs; i++) {
      System.out.println();
      Document doc = reader.document(i);
      System.out.println(doc.get("title"));

      Document[] docs = sb.docsLike(i, 10);

      if (docs.length == 0) {
        System.out.println("\tNone like this");
      }

      for (Document simDoc : docs) {
        System.out.println("\t-> " + simDoc.get("title"));
      }
    }

    reader.close();
    AnalyzerUtils.teardown();
  }

  public Document[] docsLike(int id, int max) throws IOException {
    Document doc = reader.document(id);

    String[] authors = doc.getValues("author");
    BooleanQuery.Builder authorQuery = new Builder();
    for (String author : authors) {
      authorQuery.add(new BoostQuery(new TermQuery(new Term("author", author)), 2f),
          Occur.SHOULD);
    }

    Terms vec = reader.getTermVector(id, "subject");

    BooleanQuery.Builder subjectQuery = new Builder();
    TermsEnum iter = vec.iterator();
    for (BytesRef ref = iter.next(); ref != null; ref = iter.next()) {
      TermQuery tq = new TermQuery(new Term("subject", ref));
      subjectQuery.add(tq, Occur.SHOULD);
    }

    BooleanQuery.Builder likeThisQuery = new Builder();
    likeThisQuery.add(authorQuery.build(), Occur.SHOULD);
    likeThisQuery.add(subjectQuery.build(), Occur.SHOULD);
    likeThisQuery.add(new TermQuery(new Term("isbn", doc.get("isbn"))), Occur.MUST_NOT);

    TopDocs hits = searcher.search(likeThisQuery.build(), 10);
    int size = max;
    if (max > hits.scoreDocs.length) {
      size = hits.scoreDocs.length;
    }

    Document[] docs = new Document[size];
    for (int i = 0; i < size; i++) {
      docs[i] = reader.document(hits.scoreDocs[i].doc);
    }

    return docs;
  }
}
