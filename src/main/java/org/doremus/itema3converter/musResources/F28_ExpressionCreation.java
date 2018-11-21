package org.doremus.itema3converter.musResources;

import io.github.pasqlisena.RomanConverter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.Converter;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;

public class F28_ExpressionCreation extends DoremusResource {

  private E52_TimeSpan timeSpan = null;
  private List<String> composer;
  private String derivation;

  public F28_ExpressionCreation(Omu omu, boolean createAPerformancePlan) {
    super(omu, getId(omu.getId(), createAPerformancePlan));
    this.record = omu;
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    if (createAPerformancePlan) parsePerformancePlan();
    else parseWork();
  }

  private static String getId(String id, boolean createAPerformancePlan) {
    return (createAPerformancePlan ? "p" : "w") + id;
  }

  private void parseWork() {
    Omu omu = (Omu) this.record;
    this.composer = new ArrayList<>();

    // date of the work
    String date = omu.compositionDate.trim();
    if (!date.isEmpty()) {
      try {
        this.timeSpan = toTimeSpan(date);
        this.addProperty(CIDOC.P4_has_time_span, timeSpan);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    // composer
    int ipCount = 0;
    for (OmuPersonne op : OmuPersonne.byOmu(omu.getId())) {
      String activityUri = this.uri + "/activity/" + ++ipCount;
      E7_Activity activity = new E7_Activity(activityUri, op, model);

      M31_ActorFunction af = activity.getAf();
      if (af == null) continue;
      if (af.getDerivation() != null && !af.getDerivation().isEmpty())
        this.derivation = af.getDerivation();
      if (af.isWorkAuthor())
        this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      if (af.isComposer())
        this.composer.add(activity.getCarrier().getUri().toString());
    }

    // period
    for (OmuTypeMusicalDoc ot : OmuTypeMusicalDoc.byOmu(omu.getId())) {
      if (ot.isPeriod() && timeSpan != null &&
        timeSpan.getStart().getLexicalForm().startsWith(ot.getPeriodCentury()))
        this.resource.addProperty(CIDOC.P10_falls_within, model.createResource(ot.getUri()));
    }

    // sponsor radio france
    for (OmuTypeMusicalDoc ot : OmuTypeMusicalDoc.byOmu(omu.getId())) {
      if (ot.hasCode("19")) {
        String activityUri = this.uri + "/activity/" + ++ipCount;
        E7_Activity activity = new E7_Activity(activityUri, Converter.RADIO_FRANCE, "commanditaire", model);
        this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      }
    }
  }

  public String getDerivation() {
    return derivation;
  }


  private E52_TimeSpan toTimeSpan(String date) throws URISyntaxException {
    E52_TimeSpan ts;
    URI url = new URI(this.uri + "/time");
    if (date.matches(E52_TimeSpan.CENTURY_REGEX)) {
      Matcher mt = E52_TimeSpan.CENTURY_PATTERN.matcher(date);
      mt.find();
      String cent = mt.group(1);
      int ct = RomanConverter.isRoman(cent) ? RomanConverter.toNumerical(cent) : Integer.parseInt(cent);
      Literal lit = model.createTypedLiteral(ct + "00", XSDDatatype.XSDgYear);
      ts = new E52_TimeSpan(url, lit, lit);
      ts.setQuality(E52_TimeSpan.Precision.CENTURY);
      return ts;
    }


    List<Literal> literals = new ArrayList<>();
    String[] parts = date.toLowerCase().split("(-| et )", 2);

    List<Boolean> uncertain = new ArrayList<>();

    for (String p : parts) {
      p = p.trim();
      // Move it to ISO format
      List<String> comps = Arrays.asList(p.split("/", 3));
      Collections.reverse(comps);
      String value = String.join("-", comps);

      boolean uct = value.contains("environ") || value.contains("vers") || value.contains("après");
      uncertain.add(uct);
      if (uct)
        value = value.replaceAll("env(\\.|iron)", "")
          .replace("vers", "")
          .replace("apr[èe]s", "")
          .trim();

      if (!value.matches("\\d{4}+.+") && value.matches(E52_TimeSpan.frenchDateRegex))
        value = E52_TimeSpan.frenchToISO(value);

      value = value.replaceAll("a-z", "").trim();
      if (!value.matches("^\\d{4}(-\\d{2}(-\\d{2})?)?")) return null;

      // From length detect Datatype
      XSDDatatype type = comps.size() == 3 ? XSDDatatype.XSDdate :
        comps.size() == 2 ? XSDDatatype.XSDgYearMonth : XSDDatatype.XSDgYear;

      literals.add(model.createTypedLiteral(value, type));
    }

    Literal start = literals.get(0);
    Literal end = literals.get(literals.size() - 1);
    boolean uctStart = uncertain.get(0);
    boolean uctEnd = uncertain.get(uncertain.size() - 1);

    ts = new E52_TimeSpan(url, start, end);
    if (uctStart) ts.setQualityStart(E52_TimeSpan.Precision.UNCERTAINTY);
    if (uctEnd) ts.setQualityStart(E52_TimeSpan.Precision.UNCERTAINTY);
    return ts;
  }

  private void parsePerformancePlan() {
    // People
    int ipCount = 0;
    for (OmuPersonne op : OmuPersonne.byOmu(this.record.getId())) {
      String activityUri = this.uri + "/activity/" + ++ipCount;
      E7_Activity activity = new E7_Activity(activityUri, op, model);
      if (activity.getAf() != null && activity.getAf().isAPlanningRole())
        this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
    }
  }

  public List<String> getComposers() {
    return composer;
  }

  public F28_ExpressionCreation add(F14_IndividualWork f14) {
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, f14.asResource());
    f14.setEvent(this);
    return this;
  }

  public F28_ExpressionCreation add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R17_created, f22.asResource());
    return this;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;

    F28_ExpressionCreation evt = (F28_ExpressionCreation) o;
    for (String c : composer)
      if (!evt.composer.contains(c)) return false;

    if (this.timeSpan == null && evt.timeSpan == null) return true;
    return Objects.equals(this.timeSpan, evt.timeSpan);
  }
}
