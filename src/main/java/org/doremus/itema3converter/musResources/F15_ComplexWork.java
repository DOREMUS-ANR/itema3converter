package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F15_ComplexWork extends DoremusResource {

  public F15_ComplexWork(Omu omu) {
    super(omu);
    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work)
      .addProperty(CIDOC.P102_has_title, omu.getTitle());

  }

}
