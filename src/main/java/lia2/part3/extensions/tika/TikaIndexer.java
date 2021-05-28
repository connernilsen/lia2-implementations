package lia2.part3.extensions.tika;

import lia2.part2.buildSearchIndex.CreateIndex;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TikaIndexer extends CreateIndex {

  private static final boolean DEBUG = true;
  static Set<Property> textualMetadataFields = new HashSet<>();

  static {
    textualMetadataFields.add(TikaCoreProperties.TITLE);
    textualMetadataFields.add(TikaCoreProperties.CREATOR);
    textualMetadataFields.add(TikaCoreProperties.COMMENTS);
    textualMetadataFields.add(TikaCoreProperties.KEYWORDS);
    textualMetadataFields.add(TikaCoreProperties.DESCRIPTION);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Needs 2 args");
    }

    TikaConfig config = TikaConfig.getDefaultConfig();
    Parser parser = config.getParser();
    System.out.println("Parser:" + parser.toString());
    System.out.println();

    String indexDir = args[0];
    String dataDir = args[1];
    long start = new Date().getTime();
    TikaIndexer indexer = new TikaIndexer(indexDir);

    int numIndexed = indexer.index(dataDir, null);
    indexer.close();
    long end = new Date().getTime();
    System.out.println("Indexing " + numIndexed + " files took "
    + (end - start) + " milliseconds");
  }

  public TikaIndexer(String indexDir) throws IOException {
    super(indexDir);
  }

  protected Document getDocument(File f) throws Exception {
    Metadata metadata = new Metadata();
    metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
    InputStream is = new FileInputStream(f);
    Parser parser = new AutoDetectParser();
    ContentHandler handler = new BodyContentHandler();
    ParseContext context = new ParseContext();
    context.set(Parser.class, parser);
    try {
      parser.parse(is, handler, metadata, new ParseContext());
    }
    finally {
      is.close();
    }

    Document doc = new Document();
    doc.add(new TextField("contents", handler.toString(), Store.NO));

    if (DEBUG) {
      System.out.println("\tall text: " + handler.toString());
    }

    for (String name : metadata.names()) {
      String value = metadata.get(name);
      if (textualMetadataFields.contains(Property.get(name))) {
        doc.add(new TextField("contents", value, Store.NO));
      }

      FieldType nameType = new FieldType(TextField.TYPE_STORED);
      nameType.setIndexOptions(IndexOptions.NONE);
      doc.add(new Field(name, value, nameType));

      if (DEBUG) {
        System.out.println("\t" + name + ": " + value);
      }
    }
    doc.add(new StringField("filename", f.getCanonicalPath(), Store.YES));
    return doc;
  }
}
