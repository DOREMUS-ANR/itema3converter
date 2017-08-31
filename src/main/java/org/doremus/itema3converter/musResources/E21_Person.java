package org.doremus.itema3converter.musResources;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.Converter;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.Personne;
import org.doremus.ontology.CIDOC;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

  public E21_Person(Personne record) throws URISyntaxException {
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

    this.uri = ConstructURI.build("E21_Person", firstName, ln, birthYear);
    addToCache(record.getId(), this.uri.toString());
    initResource();
  }

  private String fixCase(String str) {
    if (str == null) return null;
    if (!StringUtils.isAllUpperCase(str.replaceAll("[^\\w]", ""))) return str;

    return Arrays.stream(str.toLowerCase().split(" "))
      .map(StringUtils::capitalize)
      .collect(Collectors.joining(" "));
  }

  public E21_Person(String id) {
    String uri = cache.get(id);
    if (uri == null || uri.isEmpty()) Converter.parsePerson(id);
    this.resource = model.createResource(uri);
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
    addProperty(RDFS.label, this.getFullName());
    addProperty(CIDOC.P131_is_identified_by, this.getIdentification());

    addProperty(CIDOC.P98i_was_born, formatDate(birthDate, birthYear));
    addProperty(CIDOC.P100i_died_in, formatDate(deathDate, deathYear));


    addNote(this.record.getComment());
    addProperty(FOAF.gender, this.record.getGender(), "en");
    return resource;
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

  private Literal formatDate(Date d, String fallback) {
    String label;
    XSDDatatype type;
    if (d != null) {
      label = RecordConverter.ISODateFormat.format(d);
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


}
