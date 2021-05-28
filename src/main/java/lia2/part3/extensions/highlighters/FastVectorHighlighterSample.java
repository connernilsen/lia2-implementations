package lia2.part3.extensions.highlighters;

import java.io.FileWriter;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import lia2.util.AnalyzerUtils;

public class FastVectorHighlighterSample {

  static final String[] DOCS = {
      "the quick brown fox jumps over the lazy dog",
      "the quick gold fox jumped over the lazy black dog",
      "the quick fox jumps over the black dog",
      "the red fox jumped over the lazy dark gray dog"
  };

  static final String QUERY = "quick OR fox OR \"lazy dog\"~1";
  static final String F = "f";
  static Analyzer analyzer = new StandardAnalyzer();

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: FastVectorHighlighterSample <filename>");
      System.exit(1);
    }
    AnalyzerUtils.setupDir();
    makeIndex();
    searchIndex(args[0]);
    AnalyzerUtils.teardown();
  }

  static void makeIndex() throws IOException {
    IndexWriter writer = new IndexWriter(AnalyzerUtils.testPrefs.dir,
        new IndexWriterConfig(analyzer));

    for (String d : DOCS) {
      Document doc = new Document();
      FieldType type = new FieldType(TextField.TYPE_STORED);
      type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
      type.setStoreTermVectors(true);
      type.setStoreTermVectorOffsets(true);
      type.setStoreTermVectorPayloads(true);
      type.setOmitNorms(false);
      type.setStoreTermVectorPositions(true);
      doc.add(new Field(F, d, type));
      writer.addDocument(doc);
    }
    writer.close();
  }

  static void searchIndex(String filename) throws Exception {
    QueryParser parser = new QueryParser(F, analyzer);
    Query query = parser.parse(QUERY);
    FastVectorHighlighter highlighter = getHighlighter();
    FieldQuery fieldQuery = highlighter.getFieldQuery(query);
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(AnalyzerUtils.testPrefs.dir));
    TopDocs docs = searcher.search(query, 10);

    FileWriter writer = new FileWriter(filename);
    writer.write("<html><body>");
    writer.write("<p>QUERY : " + QUERY + "</p>");
    for (ScoreDoc doc : docs.scoreDocs) {
      String snippet = highlighter.getBestFragment(fieldQuery, searcher.getIndexReader(),
          doc.doc, F, 100);

      if (snippet != null) {
        writer.write(doc.doc + " : " + snippet + "<br />");
      }
    }

    writer.write("</body></html>");
    writer.close();
  }

  static FastVectorHighlighter getHighlighter() {
    FragListBuilder fragListBuilder = new SimpleFragListBuilder();
    FragmentsBuilder fragmentsBuilder = new ScoreOrderFragmentsBuilder(
        BaseFragmentsBuilder.COLORED_PRE_TAGS, BaseFragmentsBuilder.COLORED_POST_TAGS);
    return new FastVectorHighlighter(true, true,
        fragListBuilder, fragmentsBuilder);
  }



}
