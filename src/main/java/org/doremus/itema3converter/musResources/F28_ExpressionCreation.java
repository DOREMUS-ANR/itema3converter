package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.Converter;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
        this.model.add(timeSpan.model);
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
    String[] parts = date.split("-", 2);
    List<Literal> literals = new ArrayList<>();

    for (String p : parts) {
      p = p.trim();
      // Move it to ISO format
      List<String> comps = Arrays.asList(p.split("/", 3));
      Collections.reverse(comps);
      String value = String.join("-", comps);

      // From length detect Datatype
      XSDDatatype type = comps.size() == 3 ? XSDDatatype.XSDdate :
        comps.size() == 2 ? XSDDatatype.XSDgYearMonth : XSDDatatype.XSDgYear;

      literals.add(model.createTypedLiteral(value, type));
    }

    Literal start = literals.get(0);
    Literal end = literals.get(literals.size() - 1);

    return new E52_TimeSpan(new URI(this.uri + "/time"), start, end);
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
    return this;
  }

  public F28_ExpressionCreation add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R17_created, f22.asResource());
    return this;
  }
}
