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
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);

    // derivation type
    OmuTypeMusicalDoc.byOmu(omu.getId())
      .stream()
      .filter(OmuTypeMusicalDoc::isDerivation)
      .forEach(ot -> this.resource.addProperty(MUS.U47_has_derivation_type, ot.getLabel()));
  }

  public void setDerivation(String derivation) {
    this.resource.addProperty(MUS.U47_has_derivation_type, derivation);
  }
}
