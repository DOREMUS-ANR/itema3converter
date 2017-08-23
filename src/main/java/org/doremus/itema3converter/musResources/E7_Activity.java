package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class E7_Activity {
  private final Model model;
  private final String uri;
  private final DoremusResource carrier;

  private final M31_ActorFunction af;

  public E7_Activity(String uri, OmuPersonne op, Model m) {
    this.model = m;
    this.uri = uri;

    af = op.professionID > 0 ?
      M31_ActorFunction.get(op.professionID) :
      M31_ActorFunction.getMorale(op.typeMoraleID);

    carrier = op.personneID.isEmpty() ?
      new F11_Corporate_Body(op.moraleID) : new E21_Person(op.personneID);
  }

  public Resource asResource() {
    return model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(CIDOC.P14_carried_out_by, carrier.asResource())
      .addProperty(MUS.U31_had_function_of_type, af.asResource());
  }

  public M31_ActorFunction getAf() {
    return af;
  }


}
