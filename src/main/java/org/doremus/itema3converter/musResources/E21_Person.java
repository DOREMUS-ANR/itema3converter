package org.doremus.itema3converter.musResources;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.isnimatcher.ISNIRecord;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.Converter;
import org.doremus.itema3converter.ISNIWrapper;
import org.doremus.itema3converter.Utils;
import org.doremus.itema3converter.files.Personne;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.Schema;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class E21_Person extends DoremusResource {
  private static HashMap<String, String> cache;
  private Personne record;

  private String firstName, lastName, pseudo;
  private Date birthDate, deathDate;
  private String birthYear, deathYear;


  public E21_Person(String id) {
    String uriCache = cache.get(id);
    if (uriCache == null || uriCache.isEmpty()) {
      Converter.parsePerson(id);
      uriCache = cache.get(id);
    }

    try {
      uri = new URI(uriCache);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(uri.toString());
  }

  public E21_Person(Personne record) throws URISyntaxException, RuntimeException {
    this.record = record;
    this.firstName = record.getName();
    this.lastName = fixCase(record.getSurname());
    this.pseudo = fixCase(record.getPseudonym());
    this.birthDate = record.getBirthDate();
    this.deathDate = record.getDeathDate();
    this.birthYear = toYear(record.getBirthDate(), record.getBirthYear());
    this.deathYear = toYear(record.getDeathDate(), record.getDeathYear());

    String ln = lastName;
    if (lastName == null || lastName.isEmpty()) ln = pseudo;
    if (ln.isEmpty()) throw new RuntimeException("Person without surname neither pseudo :" + record.getId());

    this.uri = ConstructURI.build("E21_Person", firstName, ln, birthYear);
    initResource();
    interlink();

    addToCache(record.getId(), this.uri.toString());
  }

  public static boolean isInCache(String id) {
    return cache.containsKey(id);
  }

  private String fixCase(String str) {
    if (str == null) return null;
    if (!StringUtils.isAllUpperCase(str.replaceAll("[^\\w]", ""))) return str;

    return Arrays.stream(str.toLowerCase().split(" "))
      .map(StringUtils::capitalize)
      .collect(Collectors.joining(" "));
  }

  private String toYear(Date d, String s) {
    if (s != null && s.isEmpty())
      s = null;
    if (s == null) {
      if (d == null) return null;
      LocalDate localDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      s = localDate.getYear() + "";
    }
    return s;
  }

  public String getFullName() {
    if (lastName == null) return pseudo;

    String fullName = lastName;
    if (firstName != null) fullName = firstName + " " + lastName;
    return fullName;
  }

  public String getIdentification() {
    String identification = lastName;
    if (firstName != null) identification += ", " + firstName;
    if (birthYear != null) {
      identification += " (" + birthYear;
      if (deathYear != null) identification += "-" + deathYear;
      identification += ")";
    }
    return identification;
  }

  private Resource initResource() {
    this.resource = model.createResource(this.uri.toString());
    resource.addProperty(RDF.type, CIDOC.E21_Person);

    addProperty(FOAF.firstName, firstName);
    addProperty(FOAF.surname, lastName);
    addProperty(FOAF.name, this.getFullName());
    addProperty(FOAF.name, pseudo);

    addProperty(RDFS.label, this.getFullName());
    addProperty(CIDOC.P131_is_identified_by, this.getIdentification());
    addProperty(DC.identifier, record.getId());

    try {
      addDate(formatDate(birthDate, birthYear), false);
      addDate(formatDate(deathDate, deathYear), true);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    addNote(this.record.getComment());
    addProperty(FOAF.gender, this.record.getGender(), "en");
    return resource;
  }

  private void addDate(Literal date, boolean isDeath) throws URISyntaxException {
    if (date == null) return;

    String url = this.uri + (isDeath ? "/death" : "/birth");

    E52_TimeSpan ts = new E52_TimeSpan(new URI(url + "/interval"), date, date);
    Property schemaProp = isDeath ? Schema.deathDate : Schema.birthDate;

    this.resource.addProperty(isDeath ? CIDOC.P100i_died_in : CIDOC.P98i_was_born,
      model.createResource(url)
        .addProperty(RDF.type, isDeath ? CIDOC.E69_Death : CIDOC.E67_Birth)
        .addProperty(CIDOC.P4_has_time_span, ts.asResource())
    ).addProperty(schemaProp, ts.getStart());
    model.add(ts.getModel());
  }


  public void addProperty(Property property, Literal object) {
    if (property == null || object == null) return;
    resource.addProperty(property, object);
  }

  public void addProperty(Property property, String object, String lang) {
    if (property == null || object == null || object.isEmpty()) return;

    if (lang != null)
      resource.addProperty(property, model.createLiteral(object, lang));
    else
      resource.addProperty(property, object);
  }

  public void addProperty(Property property, String object) {
    addProperty(property, object, null);
  }

  public void addPropertyResource(Property property, String uri) {
    if (property == null || uri == null) return;
    resource.addProperty(property, model.createResource(uri));
  }

  private Literal formatDate(Date d, String fallback) {
    String label;
    XSDDatatype type;
    if (d != null) {
      label = E52_TimeSpan.ISODateFormat.format(d);
      type = XSDDatatype.XSDdate;
    } else if (fallback != null && !fallback.isEmpty()) {
      label = fallback;
      type = XSDDateType.XSDgYear;
    } else return null;

    return model.createTypedLiteral(label, type);
  }

  public static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("person.properties");
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
      properties.store(new FileOutputStream("person.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean interlink() {
    // 1. search in doremus by name/date
    Resource match = getPersonFromDoremus();
    if (match != null) {
      this.setUri(match.getURI());
      return true;
    }

    // 2. search in isni by name/date
    ISNIRecord isniMatch;
    try {
      isniMatch = ISNIWrapper.search(this.getFullName(), this.birthYear);
    } catch (IOException e) {
      return false;
    }
    if (isniMatch == null) return false;

    // 3. search in doremus by isni
    match = getPersonFromDoremus(isniMatch.uri);
    if (match != null) {
      this.setUri(match.getURI());
      return true;
    }

    // 4. add isni info
    this.isniEnrich(isniMatch);
    return false;
  }

  private static final String NAME_SPARQL = "PREFIX ecrm: <" + CIDOC.getURI() + ">\n" +
    "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
    "PREFIX schema: <" + Schema.getURI() + ">\n" +
    "SELECT DISTINCT ?s " +
    "FROM <http://data.doremus.org/bnf> " +
    "WHERE { ?s a ecrm:E21_Person; foaf:name ?name. }";
  private static final String NAME_DATE_SPARQL = "PREFIX ecrm: <" + CIDOC.getURI() + ">\n" +
    "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
    "PREFIX schema: <" + Schema.getURI() + ">\n" +
    "SELECT DISTINCT ?s " +
    "FROM <http://data.doremus.org/bnf> " +
    "WHERE { ?s a ecrm:E21_Person; foaf:name ?name. " +
    "?s schema:birthDate ?date. FILTER regex(str(?date), ?birthDate) }";

  public static Resource getFromDoremus(String name, String birthDate) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    pss.setCommandText(birthDate != null ? NAME_DATE_SPARQL : NAME_SPARQL);
    pss.setLiteral("name", name);
    if (birthDate != null) pss.setLiteral("birthDate", birthDate);

    return (Resource) Utils.queryDoremus(pss, "s");
  }

  private static final String ISNI_SPARQL = "PREFIX owl: <" + OWL.getURI() + ">\n" +
    "SELECT DISTINCT * WHERE { ?s owl:sameAs ?isni }";

  private Resource getPersonFromDoremus() {
    return getFromDoremus(this.getFullName(), this.birthYear);
  }


  private Resource getPersonFromDoremus(String isni) {
    ParameterizedSparqlString pss = new ParameterizedSparqlString();
    pss.setCommandText(ISNI_SPARQL);
    pss.setIri("isni", isni);
    return (Resource) Utils.queryDoremus(pss, "s");
  }

  public void isniEnrich(ISNIRecord isni) {
    this.addPropertyResource(OWL.sameAs, isni.uri);
    this.addPropertyResource(OWL.sameAs, isni.getViafURI());
    this.addPropertyResource(OWL.sameAs, isni.getMusicBrainzUri());
    this.addPropertyResource(OWL.sameAs, isni.getMuziekwebURI());
    this.addPropertyResource(OWL.sameAs, isni.getWikidataURI());

    String wp = isni.getWikipediaUri();
    String dp = isni.getDBpediaUri();

    if (wp == null) {
      wp = isni.getWikipediaUri("fr");
      dp = isni.getDBpediaUri("fr");
    }
    this.addPropertyResource(OWL.sameAs, dp);
    this.addPropertyResource(FOAF.isPrimaryTopicOf, wp);
  }

}
