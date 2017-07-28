package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F28_ExpressionCreation extends DoremusResource {

  public F28_ExpressionCreation(Omu omu, boolean createAPerformancePlan) {
    super(omu, getId(omu.getId(), createAPerformancePlan));
    this.record = omu;
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    if (createAPerformancePlan) parsePerformancePlan();
  }

  private static String getId(String id, boolean createAPerformancePlan) {
    return (createAPerformancePlan ? "p" : "w") + id;
  }

  private void parsePerformancePlan() {
    // Performers
    int ipCount = 0;
    for (OmuPersonne op : OmuPersonne.byOmu(record.getId())) {
      if (op.professionID != 207 && op.professionID != 21 && op.typeMoraleID != 45)
        continue;

      try {
        M28_IndividualPerformance ip = new M28_IndividualPerformance(op, new URI(this.uri + "/" + ++ipCount));
        model.add(ip.getModel());
        this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

  }

}
