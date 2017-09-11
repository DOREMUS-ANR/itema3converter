package org.doremus.itema3converter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
import org.doremus.itema3converter.files.LieuGeo;
import org.doremus.itema3converter.files.Morale;
import org.doremus.itema3converter.files.Personne;
import org.doremus.itema3converter.musResources.E21_Person;
import org.doremus.itema3converter.musResources.E53_Place;
import org.doremus.itema3converter.musResources.F11_Corporate_Body;
import org.doremus.ontology.*;
import org.doremus.vocabulary.VocabularyManager;
import org.geonames.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
  static Logger log = MyLogger.getLogger(Converter.class.getName());
  public static final String UTF8_BOM = "\uFEFF";
  public final static String SCHEMA = "http://schema.org/";

  public static Properties properties;
  static String dataFolderPath;
  private static String inputFolderPath, outputFolderPath;

  public static void main(String[] args) throws IOException, XMLStreamException, TransformerException {
    // INIT
    System.out.println("\n\n******* Running ITEMA3 Converter *********");

    loadProperties();
    System.out.println(properties);

    GeoNames.loadCache();
    E21_Person.loadCache();
    VocabularyManager.init();

    System.out.println("\n\n");
    // end INIT

    inputFolderPath = properties.getProperty("src");
    outputFolderPath = properties.getProperty("out");
    File inputFolder = new File(inputFolderPath);

    dataFolderPath = Paths.get(inputFolderPath, "data").toString();
    File dataFolder = new File(dataFolderPath);

    if (!dataFolder.exists()) {
      log.info("Pre-processing data. It will be needed only the first time");
      for (File f : inputFolder.listFiles())
//      if (f.getName().equals("SEQUENCE.xml"))
        fileToFolder(f);
      log.info("End Pre-processing\n\n");
    }

    new File(outputFolderPath + "/item").mkdirs();

    if (properties.getProperty("places").equals("true")) {
      GeoNames.setUser(properties.getProperty("geonames_user"));
      String geonamesFolder = Paths.get(outputFolderPath, "place", "geonames").toString();
      new File(geonamesFolder).mkdirs();
      GeoNames.setDestFolder(geonamesFolder);

      File plFolder = new File(Paths.get(dataFolderPath, "LIEU_GEO").toString());

      new File(outputFolderPath + "/place/p").mkdirs();
      for (File p : plFolder.listFiles()) {
        parsePlace(p, outputFolderPath + "/place/p");
      }
    }

    if (properties.getProperty("persons").equals("true")) {
      File persFolder = new File(Paths.get(dataFolderPath, "PERSONNE").toString());
      new File(outputFolderPath + "/person").mkdirs();
      for (File p : persFolder.listFiles()) {
        parsePerson(p, outputFolderPath + "/person");
      }

    }

    if (properties.getProperty("organizations").equals("true")) {
      File organizFolder = new File(Paths.get(dataFolderPath, "MORALE").toString());
      new File(outputFolderPath + "/organization").mkdirs();
      for (File p : organizFolder.listFiles()) {
        parseOrganization(p, outputFolderPath + "/organization");
      }

    }

    // MAG_CONTENU is the first folder to parse
    File mcFolder = new File(Paths.get(dataFolderPath, "MAG_CONTENU").toString());
    int i = 0;
    for (File mc : mcFolder.listFiles()) {
//      if (!mc.getName().equals("1196142.xml")) continue;
      if(++i == 10) return;
      parseRecord(mc, outputFolderPath + "/item");
    }
  }

  public static void parsePerson(String id) {
    File f = new File(Paths.get(dataFolderPath, "PERSONNE", id + ".xml").toString());
    parsePerson(f, outputFolderPath + "/person", true);
  }

  private static void parsePerson(File p, String outputFolder) {
    parsePerson(p, outputFolder, false);
  }

  private static void parsePerson(File p, String outputFolder, boolean force) {
    Personne ps = Personne.fromFile(p);
    if (!force && ps.getStatus() != 1) return;
    try {
      E21_Person person = new E21_Person(ps);
      log.info("Person : " + ps.getId() + " " + person.getFullName());

      writeTtl(person.getModel(), Paths.get(outputFolder, p.getName().replaceFirst(".xml", ".ttl")).toString());
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  private static void parseOrganization(File p, String outputFolder) {
    Morale mr = Morale.fromFile(p);
    if (mr.getStatus() != 1) return;
    try {
      F11_Corporate_Body cb = new F11_Corporate_Body(mr);
      log.info("Corporate : " + mr.getId() + " " + cb.getName());

      Model m = cb.getModel();
      writeTtl(m, Paths.get(outputFolder, p.getName().replaceFirst(".xml", ".ttl")).toString());
    } catch (NullPointerException e) {
      log.severe("Corporate without name: " + mr.getId());
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  private static void parsePlace(File p, String outputFolder) {
    Model m = ModelFactory.createDefaultModel();

    LieuGeo lg = LieuGeo.fromFile(p);
    assert lg != null;

    if (lg.getQualif() != null && lg.getQualif().contains("PEUPLE"))
      return;
    log.info("Place: " + lg.getId() + " : " + lg.getLabel());

    Toponym tp = GeoNames.query(lg);
    if (tp != null) {
      // simply download the file
      String uri = "http://sws.geonames.org/" + tp.getGeoNameId() + "/";
      log.info("> " + uri + " : " + tp.getName());
      GeoNames.downloadRdf(tp.getGeoNameId());

      // add some additional info
      Resource place = m.createResource(uri).addProperty(RDF.type, CIDOC.E53_Place);
      if (lg.getFatherId() != null)
        place.addProperty(CIDOC.P89_falls_within, new E53_Place(lg.getFatherId()).asResource());
      if (tp.getName() != null)
        place.addProperty(RDFS.label, tp.getName());
    } else {
      // model it as a Place
      E53_Place pl = new E53_Place(lg);
      m.add(pl.getModel());
    }
    try {
      writeTtl(m, Paths.get(outputFolder, p.getName().replaceFirst(".xml", ".ttl")).toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void parseRecord(File mc, String outputFolder) {
    try {
      RecordConverter r = new RecordConverter(mc);
      Model m = r.getModel();

      if (m == null) return;

      String newFileName = mc.getName().replaceFirst(".xml", ".ttl");

      VocabularyManager.string2uri(m);
      writeTtl(m, Paths.get(outputFolder, newFileName).toString());
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  private static void writeTtl(Model m, String filename) throws IOException {
    m.setNsPrefix("mus", MUS.getURI());
    m.setNsPrefix("ecrm", CIDOC.getURI());
    m.setNsPrefix("efrbroo", FRBROO.getURI());
    m.setNsPrefix("xsd", XSD.getURI());
    m.setNsPrefix("dcterms", DCTerms.getURI());
    m.setNsPrefix("owl", OWL.getURI());
    m.setNsPrefix("foaf", FOAF.getURI());
    m.setNsPrefix("rdfs", RDFS.getURI());
    m.setNsPrefix("prov", PROV.getURI());
    m.setNsPrefix("time", Time.getURI());
    m.setNsPrefix("schema", SCHEMA);


    // Write the output file
    FileWriter out = new FileWriter(filename);
    // m.write(System.out, "TURTLE");
    m.write(out, "TURTLE");
    out.close();
  }

  private static void fileToFolder(File f) throws XMLStreamException, IOException, TransformerException {
    // Separate the long files in a folder with a file for each record
    if (!f.getName().endsWith(".xml")) return;
    removeUTF8BOM(f);

    Pattern item_id = Pattern.compile("<ITEM_ID>(.+)</ITEM_ID>");
    Pattern mag_id = Pattern.compile("<MAG_CONTENU_ID>(.+)</MAG_CONTENU_ID>");
    Pattern omu_id = Pattern.compile("<OMU_ID>(.+)</OMU_ID>");

    String fileName = f.getName().replaceFirst("\\.xml", "");
    System.out.println(fileName);

    new File(Paths.get(dataFolderPath, fileName).toString()).mkdirs();

    XMLInputFactory xif = XMLInputFactory.newInstance();
    XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(f));
    xsr.next(); // Skip Doctype
    xsr.nextTag(); // Advance to statements element

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    QName recordName = QName.valueOf("DATA_RECORD");

    String idMain = fileName.replaceFirst("TH_VA_", "");
    if (idMain.equals("NC")) idMain = "NOM_COMMUN"; // known abbreviation
    Pattern p = Pattern.compile("<" + idMain + "_ID>(.+)<\\/" + idMain + "_ID>");

    while (xsr.hasNext()) {
      if (xsr.next() != XMLStreamConstants.START_ELEMENT || !xsr.getName().equals(recordName)) continue;
      StringWriter stringWriter = new StringWriter();

      t.transform(new StAXSource(xsr), new StreamResult(stringWriter));
      String recordString = stringWriter.toString();
//            System.out.println(recordString);
      Matcher m = p.matcher(recordString);
      String id;
      if (m.find()) {
        id = m.group(1);

        // The INVITE file is in facts a JOIN table between ITEM and PERSONS
        // idem for ITEM_PRODUCTEUR
        if (fileName.equals("INVITE") || fileName.equals("ITEM_PRODUCTEUR")) {
          Matcher mi = item_id.matcher(recordString);
          mi.find();
          id = mi.group(1) + "_" + id;
        }
        // idem for MAG_SUPPORT and SEQUENCE
        if (fileName.equals("MAG_SUPPORT") ||  fileName.equals("SEQUENCE")) {
          Matcher mm = mag_id.matcher(recordString);
          mm.find();
          id = mm.group(1) + "_" + id;
        }
        // OMU_PERSONNE has its own id but it is more convenient model it as a JOIN table
        if (fileName.equals("OMU_PERSONNE")) {
          Matcher mo = omu_id.matcher(recordString);
          mo.find();
          id = mo.group(1) + "_" + id;
        }
      } else {
        // it is a join table
        String splitter = "_";
        for (String code : new String[]{"_IDX_", "_TH_"})
          if (fileName.contains(code)) splitter = code;

        String[] parts = fileName
          .replaceFirst("TH_LIEN_", "")
          .split(splitter, 2);

        for (int i = 0; i < parts.length; i++) {
          // System.out.println("> " + parts[i]);
          if (parts[i].equals("NC")) parts[i] = "NOM_COMMUN"; // known abbreviation
        }

        // workaround for these exceptions
        if (fileName.equals("OMU_PERSONNE_STATION"))
          parts = new String[]{"OMU_PERSONNE", "STATION"};
        if (fileName.startsWith("TH_DOMAINE"))
          parts = new String[]{"TH_DOMAINE", fileName.replace("TH_DOMAINE_", "")};

        Pattern p1 = Pattern.compile("<" + parts[0] + "_ID>(.+)<\\/" + parts[0] + "_ID>");
        Pattern p2 = Pattern.compile("<" + parts[1] + "_ID>(.+)<\\/" + parts[1] + "_ID>");

        Matcher m1 = p1.matcher(recordString);
        Matcher m2 = p2.matcher(recordString);
        m1.find();
        m2.find();

        id = m1.group(1) + "_" + m2.group(1);
      }

      Files.write(Paths.get(dataFolderPath, fileName, id + ".xml"), recordString.getBytes(),
        StandardOpenOption.CREATE);
    }
    xsr.close();
  }


  private static void removeUTF8BOM(File f) throws IOException {
    // workaround: the person file is too big for this
    if (f.getName().equals("PERSONNE.xml")) return;
    // remove UTF8 BOM
    // https://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
    boolean firstLine = true;
    FileInputStream fis = new FileInputStream(f);
    BufferedReader r = new BufferedReader(new InputStreamReader(fis, "UTF8"));

    StringBuilder sb = new StringBuilder();
    for (String s; (s = r.readLine()) != null; ) {
      if (firstLine) {
        if (s.startsWith(UTF8_BOM)) s = s.substring(1);
        firstLine = false;
      } else sb.append("\n");
      sb.append(s);
    }
    r.close();
    Files.delete(f.toPath());

    String output = sb.toString().replaceAll("&#(31|28|29);", " ")
      .replaceAll("&#30;", "f");

    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8"));
    w.write(output);
    w.close();
  }

  private static void loadProperties() {
    properties = new Properties();
    String filename = "config.properties";

    try {
      InputStream input = new FileInputStream(filename);
      properties.load(input);
      input.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

}


