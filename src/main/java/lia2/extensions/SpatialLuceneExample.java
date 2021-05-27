package lia2.extensions;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lia2.util.AnalyzerUtils;
import lia2.util.AnalyzerUtils.TestPrefs;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class SpatialLuceneExample implements Closeable {

  private static final String POINT = "point";
  private static final String LAT = "lat";
  private static final String LON = "lon";

  private final TestPrefs prefs;
  private final IndexWriter writer;

  public SpatialLuceneExample() throws IOException {
    this.prefs = new AnalyzerUtils.TestPrefs();
    this.writer = new IndexWriter(this.prefs.dir,
        new IndexWriterConfig(new WhitespaceAnalyzer()));
  }

  @Override
  public void close() throws IOException {
    this.prefs.tearDown();
  }

  public static void main(String[] args) throws IOException {
    SpatialLuceneExample spatial = new SpatialLuceneExample();
    spatial.addData();
    spatial.findNear("Restaurant", 39.8725000, -77.3829000, 8);
    spatial.close();
  }

  private void addData() throws IOException {
    addLocation("McCormick & Schmick's Seafood Restaurant",
        39.9579000, -77.3572000);
    addLocation("Jimmy's Old Town Tavern", 39.9690000, -77.3862000);
    addLocation("Ned Devine's", 39.9510000, -77.4107000);
    addLocation("Old Brogue Irish Pub", 39.9955000, -77.2884000);
    addLocation("Alf Laylah Wa Laylah", 39.8956000, -77.4258000);
    addLocation("Sully's Restaurant & Supper", 39.9003000, -
        77.4467000);
    addLocation("TGIFriday", 39.8725000, -77.3829000);
    addLocation("Potomac Swing Dance Club", 39.9027000, -77.2639000);
    addLocation("White Tiger Restaurant", 39.9027000, -77.2638000);
    addLocation("Jammin' Java", 39.9039000, -77.2622000);
    addLocation("Potomac Swing Dance Club", 39.9027000, -77.2639000);
    addLocation("WiseAcres Comedy Club", 39.9248000, -77.2344000);
    addLocation("Glen Echo Spanish Ballroom", 39.9691000, -77.1400000);
    addLocation("Whitlow's on Wilson", 39.8889000, -77.0926000);
    addLocation("Iota Club and Cafe", 39.8890000, -77.0923000);
    addLocation("Hilton Washington Embassy Row", 39.9103000,
        -77.0451000);
    addLocation("HorseFeathers, Bar & Grill", 39.01220000000001,
        -77.3942);
    this.writer.close();
  }

  private void addLocation(String name, double lat, double lon) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("name", name, Store.YES));
    doc.add(new LatLonPoint(POINT, lat, lon));
    doc.add(new LatLonDocValuesField(POINT, lat, lon));
    doc.add(new StoredField(LAT, lat));
    doc.add(new StoredField(LON, lon));

    doc.add(new TextField("metafile", "doc", Store.YES));
    writer.addDocument(doc);
  }

  public void findNear(String what, double lat, double lon, double radius)
      throws IOException {
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(this.prefs.dir));
    Query tq;
    if (what == null) {
      tq = new TermQuery(new Term("metafile", "doc"));
    }
    else {
      tq = new TermQuery(new Term("name", what));
    }

    BooleanQuery.Builder bq = new Builder();
    bq.add(tq, Occur.MUST);
    Query dq = LatLonPoint.newDistanceQuery(POINT, lat, lon, radius * 1609.34);
    bq.add(dq, Occur.MUST);
    SortField sort = LatLonDocValuesField.newDistanceSort(POINT, lat, lon);
    TopDocs hits = searcher.search(bq.build(), 10, new Sort(sort));

    long numResults = hits.totalHits.value;
    System.out.println("# Results: " + numResults);
    System.out.println("Found:");
    for (ScoreDoc sd : hits.scoreDocs) {
      Document d = searcher.doc(sd.doc);
      String name = d.get("name");
      float distance = 0;
      System.out.printf(name + ": %2f Miles\n", distance);
      System.out.println("\t\t(" + d.get(LAT) + "," + d.get(LON) + ")");
    }
  }
}
