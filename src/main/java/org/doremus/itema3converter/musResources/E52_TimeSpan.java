package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.Time;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class E52_TimeSpan extends DoremusResource {
  public static final DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public static final String frenchDayRegex = "(1er|[\\d]{1,2})";
  public static final String frenchMonthRegex = "(janvier|février|mars|avril|mai|juin|juillet|ao[ûu]t|septembre|octobre|novembre|décembre)";
  public static final String frenchDateRegex = "(?:le )?(?:" + frenchDayRegex + "? ?" + frenchMonthRegex + "? ?)?(\\d{4})";
  public static final String CENTURY_REGEX = "([\\dXVI]+)[èe](me)?(?: siècle)?";
  public static final Pattern CENTURY_PATTERN = Pattern.compile(CENTURY_REGEX);

  private String label;
  private Literal start, end;

  private Precision quality;


  public Literal getStart() {
    return start;
  }


  public enum Precision {
    CERTAINTY("certain"),
    UNCERTAINTY("uncertain"),
    DECADE("precision at decade"),
    CENTURY("precision at century");

    private final String text;

    Precision(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;

    }

  }

  public E52_TimeSpan(URI uri, Date start, Date end) {
    super(uri);

    label = ISODateFormat.format(start).substring(0, 10);
    if (!end.equals(start)) label += "/" + ISODateFormat.format(end).substring(0, 10);
    this.start = model.createTypedLiteral(ISODateFormat.format(start), XSDDatatype.XSDdate);
    this.end = model.createTypedLiteral(ISODateFormat.format(end), XSDDatatype.XSDdate);

    this.resource = model.createResource(uri.toString())
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDF.type, Time.Interval)
      .addProperty(RDFS.label, label)
      .addProperty(Time.hasBeginning,
        model.createResource()
          .addProperty(RDF.type, Time.Instant)
          .addProperty(Time.inXSDDate, this.start))
      .addProperty(Time.hasEnd, model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, this.end));
  }

  @SuppressWarnings("SpellCheckingInspection")
  private static String frenchMonthToNumber(String month) {
    if (month == null || month.isEmpty()) return null;
    if (month.length() == 2) return month;

    String mm = month.toLowerCase();
    switch (mm) {
      case "janvier":
        return "01";
      case "février":
        return "02";
      case "mars":
        return "03";
      case "avril":
        return "04";
      case "mai":
        return "05";
      case "juin":
        return "06";
      case "juillet":
        return "07";
      case "août":
        return "08";
      case "septembre":
        return "09";
      case "octobre":
        return "10";
      case "novembre":
        return "11";
      case "décembre":
        return "12";
      default:
        return null;
    }
  }

  public static String frenchToISO(String txt) {
    Matcher ma = Pattern.compile(frenchDateRegex).matcher(txt);
    String d = ma.group(1);
    String m = ma.group(2);
    String y = ma.group(3);
    if (m != null) y += "-" + frenchMonthToNumber(m);
    if (d != null) y += "-" + d;
    return y;
  }


  public E52_TimeSpan(URI uri, Literal start, Literal end) {
    super(uri);
    this.start = start;
    this.end = end;

    label = start.getLexicalForm();
    if (end != null && !end.equals(start)) label += "/" + end.getLexicalForm();

    this.resource = model.createResource(uri.toString())
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDF.type, Time.Interval)
      .addProperty(RDFS.label, label);

    this.resource.addProperty(Time.hasBeginning,
      model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, start));

    if (end != null)
      this.resource.addProperty(Time.hasEnd, model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, end));
  }

  public void setQuality(Precision quality) {
    setQualityStart(quality);
    setQualityEnd(quality);
  }

  public void setQualityStart(Precision quality) {
    if (quality == null) return;
    this.quality = quality;

    this.resource.addProperty(CIDOC.P79_beginning_is_qualified_by, quality.toString());
  }

  public void setQualityEnd(Precision quality) {
    if (quality == null) return;
    this.quality = quality;

    this.resource.addProperty(CIDOC.P80_end_is_qualified_by, quality.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;

    E52_TimeSpan ts = (E52_TimeSpan) o;
    return ts.label.equals(this.label);
  }

}
