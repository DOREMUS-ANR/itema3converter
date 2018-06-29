package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.MagContenu;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;


public class F25_PerformancePlan extends DoremusResource {

  public F25_PerformancePlan(Omu omu) {
    super(omu);
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    // performance mode
    OmuTypeMusicalDoc.byOmu(omu.getId())
      .stream()
      .filter(OmuTypeMusicalDoc::isPerformanceMode)
      .forEach(ot -> {
        Resource perfMode = model.createResource()
          .addProperty(RDF.type, MUS.M50_Creation_or_Performance_Mode)
          .addProperty(RDFS.label, ot.getLabel());
        this.resource.addProperty(MUS.U90_foresees_creation_or_performance_mode, perfMode);
      });

  }

  public F25_PerformancePlan(MagContenu mag) {
    super(mag);
    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);
  }

  public void add(F25_PerformancePlan subPlan) {
    this.resource.addProperty(FRBROO.R5_has_component, subPlan.asResource());
  }

  public void add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
  }
}
