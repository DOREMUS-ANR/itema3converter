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
  private DoremusResource carrier;
  private Resource carrierResource;

  private final M31_ActorFunction af;

  public E7_Activity(String uri, OmuPersonne op, Model model) {
    this.model = model;
    this.uri = uri;

    af = op.professionID > 0 ?
      M31_ActorFunction.get(op.professionID) :
      M31_ActorFunction.getMorale(op.typeMoraleID);

    carrier = op.personneID.isEmpty() ?
      new F11_Corporate_Body(op.moraleID) : new E21_Person(op.personneID);
  }

  public E7_Activity(String uri, Resource actor, String function, Model model) {
    this.model = model;
    this.uri = uri;

    this.af = new M31_ActorFunction(function);
    this.carrierResource = actor;
  }

  public Resource asResource() {
    Resource cr = carrier == null ? carrierResource : carrier.asResource();

    model.add(af.getModel());
    return model.createResource(this.uri)
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(CIDOC.P14_carried_out_by, cr)
      .addProperty(MUS.U31_had_function, af.asResource());
  }

  public M31_ActorFunction getAf() {
    return af;
  }

  public DoremusResource getCarrier() {
    return carrier;
  }
}
