package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F14_IndividualWork extends DoremusResource {

  public F14_IndividualWork(Omu omu) {
    super(omu);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work)
      .addProperty(MUS.U94_has_work_type, "musical work");
    parseRecord();
  }

  public F14_IndividualWork(Omu omu, String identifier) {
    super(omu, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work)
      .addProperty(MUS.U94_has_work_type, "musical work");
    parseRecord();
  }

  private void parseRecord() {
    // derivation type
    OmuTypeMusicalDoc.byOmu(this.record.getId())
      .stream()
      .filter(OmuTypeMusicalDoc::isDerivation)
      .forEach(ot -> this.resource.addProperty(MUS.U47_has_derivation_type, ot.getLabel()));
  }

  public void setDerivation(String derivation) {
    this.resource.addProperty(MUS.U47_has_derivation_type, derivation);
  }

  public F14_IndividualWork add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R9_is_realised_in, f22.asResource());
    return this;
  }

  public F14_IndividualWork add(F14_IndividualWork child) {
    this.resource.addProperty(CIDOC.P148_has_component, child.asResource());
    return this;
  }
}
