package lia2.buildSearchIndex;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateIndex {
  private final IndexWriter writer;

  public CreateIndex(String indexDir) throws IOException {
    Directory dir = FSDirectory.open(Path.of(indexDir));
    writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
  }

  public void close() throws IOException {
    writer.close();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Args length != 2");
    }

    String indexDir = args[0];
    String dataDir = args[1];

    long start = System.currentTimeMillis();
    CreateIndex indexer = new CreateIndex(indexDir);
    int numIndexed;
    try {
      numIndexed = indexer.index(dataDir, new TextFilesFilter());
    }
    finally {
      indexer.close();
    }

    long end = System.currentTimeMillis();
    System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
  }

  public int index(String dataDir, FileFilter filter) throws Exception {
    File[] files = new File(dataDir).listFiles();
    if (files == null) {
      return 0;
    }

    for (File f : files) {
      if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() &&
          (filter == null || filter.accept(f))) {
        this.indexFile(f);
      }
    }
    return this.writer.getDocStats().numDocs;
  }

  private static class TextFilesFilter implements FileFilter {
    public boolean accept(File path) {
      return path.getName().toLowerCase().endsWith(".txt");
    }
  }

  protected Document getDocument(File f) throws Exception {
    Document doc = new Document();
    doc.add(new TextField("contents", new FileReader(f)));
    doc.add(new StringField("filename", f.getName(), Store.YES));
    doc.add(new StringField("fullpath", f.getCanonicalPath(), Store.YES));
    return doc;
  }

  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = this.getDocument(f);
    writer.addDocument(doc);
  }
}
