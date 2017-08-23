package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F14_IndividualWork extends DoremusResource {

  public F14_IndividualWork(Omu omu) {
    super(omu);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

}
