package tika;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXXMLDocument extends DefaultHandler {
  private StringBuilder elementBuffer = new StringBuilder();
  private Map<String, String> attributeMap = new HashMap<>();
  private Document doc;

  public Document getDoc(InputStream is) throws IOException {
    SAXParserFactory fac = SAXParserFactory.newInstance();
    try {
      SAXParser parser = fac.newSAXParser();
      parser.parse(is, this);
    }
    catch (Exception e) {
      throw new IOException("Can't parse XML doc", e);
    }
    return doc;
  }

  public void startDocument() {
    this.doc = new Document();
  }

  public void startElement(String uri, String localName, String qName, Attributes attrs)
      throws SAXException {
    elementBuffer.setLength(0);
    attributeMap.clear();
    int numAttrs = attrs.getLength();
    if (numAttrs > 0) {
      for (int i = 0; i < numAttrs; i++) {
        attributeMap.put(attrs.getQName(i), attrs.getValue(i));
      }
    }
  }

  public void characters(char[] text, int start, int length) {
    elementBuffer.append(text, start, length);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException{
    if (qName.equals("address-book")) {
      return;
    }

    else if (qName.equals("contact")) {
      for (Entry<String, String> attribute : attributeMap.entrySet()) {
        String attrName = attribute.getKey();
        String attrValue = attribute.getValue();
        doc.add(new StringField(attrName, attrValue, Store.YES));
      }
    }

    else {
      doc.add(new StringField(qName, elementBuffer.toString(), Store.YES));
    }
  }

  public static void main(String[] args) throws Exception {
    SAXXMLDocument handler = new SAXXMLDocument();
    Document doc = handler.getDoc(new FileInputStream(args[0]));
    System.out.println(doc);
  }
}
