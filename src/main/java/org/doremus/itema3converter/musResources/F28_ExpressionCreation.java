package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class F28_ExpressionCreation extends DoremusResource {

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

    // date of the work
    String date = omu.compositionDate.trim();
    if (!date.isEmpty())
      this.resource.addProperty(CIDOC.P4_has_time_span, toTimeSpan(date));

    // composer
    int ipCount = 0;
    for (OmuPersonne op : OmuPersonne.byOmu(omu.getId())) {
      String activityUri = this.uri + "/activity/" + ++ipCount;
      E7_Activity activity = new E7_Activity(activityUri, op, model);
      if (activity.getAf() != null && activity.getAf().isWorkAuthor())
        this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
    }
  }

  private Resource toTimeSpan(String date) {
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
    String label = (start == end) ?
      start.getLexicalForm() :
      start.getLexicalForm() + "/" + end.getLexicalForm();


    return model.createResource(this.uri + "/time")
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDFS.label, label)
      .addProperty(CIDOC.P79_beginning_is_qualified_by, start)
      .addProperty(CIDOC.P80_end_is_qualified_by, end);
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

}
