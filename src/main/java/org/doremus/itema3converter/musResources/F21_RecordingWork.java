package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Item;
import org.doremus.ontology.FRBROO;

public class F21_RecordingWork extends DoremusResource {

  public F21_RecordingWork(Item item) {
    super(item);
    this.resource.addProperty(RDF.type, FRBROO.F21_Recording_Work);
  }

}
