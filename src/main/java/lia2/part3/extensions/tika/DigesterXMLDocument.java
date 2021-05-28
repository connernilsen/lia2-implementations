package lia2.part3.extensions.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester3.Digester;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.xml.sax.SAXException;

public class DigesterXMLDocument {
  private Digester dig;
  private static Document doc;

  public DigesterXMLDocument() {
    this.dig = new Digester();
    dig.setValidating(false);

    dig.addObjectCreate("address-book", this.getClass());
    dig.addObjectCreate("address-book/contact", Contact.class);
    dig.addSetProperties("address-book/contact", "type", "type");

    dig.addCallMethod("address-book/contact/name", "setName", 0);
    dig.addCallMethod("address-book/contact/address", "setAddress", 0);
    dig.addCallMethod("address-book/contact/city", "setCity", 0);
    dig.addCallMethod("address-book/contact/province", "setProvince", 0);
    dig.addCallMethod("address-book/contact/postalcode", "setPostalcode", 0);
    dig.addCallMethod("address-book/contact/country", "setCountry", 0);
    dig.addCallMethod("address-book/contact/telephone", "setTelephone", 0);

    dig.addSetNext("address-book/contact", "populateDocument");
  }

  public static void main(String[] args) throws Exception {
    DigesterXMLDocument handler = new DigesterXMLDocument();
    Document doc = handler.getDoc(new FileInputStream(new File(args[0])));
    System.out.println(doc);
  }

  public synchronized Document getDoc(InputStream is) throws IOException {
    try {
      dig.parse(is);
    }
    catch (IOException | SAXException e) {
      throw new IOException("Cannot parse XML document", e);
    }

    return doc;
  }

  public void populateDocument(Contact contact) {
    doc = new Document();
    doc.add(new StringField("type", contact.getType(), Store.YES));
    doc.add(new StringField("name", contact.getName(), Store.YES));
    doc.add(new StringField("address", contact.getAddress(), Store.YES));
    doc.add(new StringField("city", contact.getCity(), Store.YES));
    doc.add(new StringField("province", contact.getProvince(), Store.YES));
    doc.add(new StringField("postalcode", contact.getPostalcode(), Store.YES));
    doc.add(new StringField("country", contact.getCountry(), Store.YES));
    doc.add(new StringField("telephone", contact.getTelephone(), Store.YES));
  }

  public static class Contact {
    private String type;
    private String name;
    private String address;
    private String city;
    private String province;
    private String postalcode;
    private String country;
    private String telephone;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public String getCity() {
      return city;
    }

    public void setCity(String city) {
      this.city = city;
    }

    public String getProvince() {
      return province;
    }

    public void setProvince(String province) {
      this.province = province;
    }

    public String getPostalcode() {
      return postalcode;
    }

    public void setPostalcode(String postalcode) {
      this.postalcode = postalcode;
    }

    public String getCountry() {
      return country;
    }

    public void setCountry(String country) {
      this.country = country;
    }

    public String getTelephone() {
      return telephone;
    }

    public void setTelephone(String telephone) {
      this.telephone = telephone;
    }
  }
}
