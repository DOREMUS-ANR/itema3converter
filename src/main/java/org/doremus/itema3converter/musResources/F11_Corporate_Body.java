package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.Converter;
import org.doremus.itema3converter.Utils;
import org.doremus.itema3converter.files.Morale;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class F11_Corporate_Body extends DoremusResource {
  private static HashMap<String, String> cache = null;

  private String name;
  private String birthDate, deathDate;
  private String comment;

  public F11_Corporate_Body(Morale record) throws NullPointerException {
    super(record.getId());
    if (cache == null) loadCache();

    this.record = record;

    this.name = Utils.fixCase(record.getName());
    if (this.name == null) throw new NullPointerException("The name of a Corporate Body cannot be null");

    this.birthDate = record.getBirthDate();
    this.deathDate = record.getDeathDate();
    this.comment = record.getComment();

    String uriCache = cache.get(identifier);
    if (uriCache == null || uriCache.isEmpty()) {
      Resource r = getFromDoremus(this.name);
      if (r != null) {
        this.resource = r;
        setUri(r.getURI());
      }
    } else setUri(uriCache);

    addToCache(record.getId(), this.uri.toString());
    initResource();
  }

  public F11_Corporate_Body(String id) {
    if (cache == null) loadCache();
    String uriCache = cache.get(id);
    if (uriCache == null || uriCache.isEmpty()) {
      Converter.parseOrganization(id);
      uriCache = cache.get(id);
    }

    try {
      uri = new URI(uriCache);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(uri.toString());

//    super(id);
//    if (!Converter.organizationExists(id))
//      Converter.parseOrganization(id);
  }

  public String getName() {
    return name;
  }

  private Resource initResource() {
    resource.addProperty(RDF.type, FRBROO.F11_Corporate_Body)
      .addProperty(RDFS.label, this.name)
      .addProperty(CIDOC.P131_is_identified_by, this.name);

    if (birthDate != null) {
      this.resource.addProperty(CIDOC.P95i_was_formed_by,
        model.createResource(this.uri + "/formation")
          .addProperty(RDF.type, CIDOC.E66_Formation)
          .addProperty(CIDOC.P4_has_time_span, toTimeSpan(birthDate, this.uri + "/formation/interval").asResource())
      );
    }
    if (deathDate != null) {
      this.resource.addProperty(CIDOC.P99i_was_dissolved_by,
        model.createResource(this.uri + "/dissolution")
          .addProperty(RDF.type, CIDOC.E68_Dissolution)
          .addProperty(CIDOC.P4_has_time_span, toTimeSpan(deathDate, this.uri + "/dissolution/interval").asResource())
      );
    }

    // TODO add type with this?
    //  a) F11 Corporate Body P14i performed F51 Pursuit P2 has type E55 Type
    //  b) F11 Corporate Body P14i performed F51 Pursuit R59 had typical subject E1 CRM Entity

    if (!this.name.equals(comment)) addNote(comment);

    return resource;
  }

  private E52_TimeSpan toTimeSpan(String date, String uri) {
    date = date
      .replace("Janvier ", "01/")
      .replace("Mai ", "05/")
      .replace("Septembre ", "09/")
      .trim();

    E52_TimeSpan ts;

    Literal d = null, dEnd = null;
    E52_TimeSpan.Precision precision = null;
    String note = null;
    if (date.matches("\\d{4}")) { // 1937
      d = model.createTypedLiteral(date, XSDDatatype.XSDgYear);
      dEnd = d;
    } else if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {// 05/12/2000
      String[] parts = date.split("/");
      Collections.reverse(Arrays.asList(parts));
      d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDdate);
      dEnd = d;
    } else if (date.matches("\\d{2}/\\d{4}")) {// 01/1945
      String[] parts = date.split("/");
      Collections.reverse(Arrays.asList(parts));
      d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDgMonth);
      dEnd = d;
    } else if (date.matches("\\d{8}")) { // 19951123
      String[] parts = new String[]{
        date.substring(0, 4),
        date.substring(4, 6),
        date.substring(6)};
      d = model.createTypedLiteral(String.join("-", parts), XSDDatatype.XSDdate);
      dEnd = d;
    } else if (date.startsWith("ann\u00e9es ")) {
      note = date;
      String year = date.replace("ann\u00e9es ", "");
      String startYear = null, endYear = null;
      if (year.length() == 2) {
        startYear = 19 + year;
        endYear = "19" + year.charAt(0) + "9";
      } else if (year.length() == 3) {
        startYear = year + 0;
        endYear = year + 9;
      }
      if (startYear != null) {
        d = model.createTypedLiteral(startYear, XSDDatatype.XSDgYear);
        dEnd = model.createTypedLiteral(endYear, XSDDatatype.XSDgYear);
        precision = E52_TimeSpan.Precision.DECADE;
      }
    } else if (date.contains("-") || date.contains("/")) {
      note = date;
      String[] parts = (date + " ").split("[-/]");
      String startYear = parts[0].trim(),
        endYear = parts[1].trim();

      if (startYear.length() == 3) {
        startYear += "0";
        precision = E52_TimeSpan.Precision.DECADE;
      }
      if (startYear.length() != 0) d = model.createTypedLiteral(startYear, XSDDatatype.XSDgYear);

      if (endYear.length() == 3) {
        endYear += "9";
        precision = E52_TimeSpan.Precision.DECADE;
      }
      if (endYear.length() != 0) dEnd = model.createTypedLiteral(endYear, XSDDatatype.XSDgYear);
    } else
      note = date;

    addNote(note);

    if (d == null) return null;

    try {
      ts = new E52_TimeSpan(new URI(uri), d, dEnd);
      ts.setQuality(precision);
      model.add(ts.getModel());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
    return ts;
  }

  private static final String NAME_SPARQL = "PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
    "PREFIX efrbroo: <" + FRBROO.getURI() + ">\n" +
    "SELECT DISTINCT ?s " +
    "WHERE { ?s a efrbroo:F11_Corporate_Body; rdfs:label ?o. " +
    "FILTER (lcase(str(?o)) = ?name) }";

  public static Resource getFromDoremus(String name) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    pss.setCommandText(NAME_SPARQL);
    pss.setLiteral("name", name.toLowerCase());

    return (Resource) Utils.queryDoremus(pss, "s");
  }

  public static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("organization.properties");
      Properties properties = new Properties();
      properties.load(fis);

      for (String key : properties.stringPropertyNames()) {
        cache.put(key, properties.get(key).toString());
      }
    } catch (IOException e) {
      System.out.println("No 'person.properties' file found. I will create it.");
    }

  }

  private static void addToCache(String key, String value) {
    cache.put(key, value);
    saveCache();
  }

  private static void saveCache() {
    Properties properties = new Properties();

    for (Map.Entry<String, String> entry : cache.entrySet()) {
      properties.put(entry.getKey(), entry.getValue() + "");
    }

    try {
      properties.store(new FileOutputStream("organization.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
