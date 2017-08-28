package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F29_RecordingEvent extends DoremusResource {
  private static int countActivity;

  public F29_RecordingEvent(Item item) {
    super(item);
    countActivity = 0;

    this.resource.addProperty(RDF.type, FRBROO.F29_Recording_Event);

    Emission emission = ItemEmission.byItem(item.getId()).get(0).getEmission();
    if (emission.num != 1107)
      addActivity(RecordConverter.RadioFrance, "Enregistrement");

    // Activities
    for (ItemProducteur ip : ItemProducteur.byItem(item.getId())) {
      M31_ActorFunction af = M31_ActorFunction.get(ip.professionId);
      assert af != null;
      if (!af.isARecordingRole()) continue;
      E21_Person person = new E21_Person(ip.personneId);
      addActivity(person.asResource(), af.asResource());
    }
  }

  public void setTimeSpan(Resource timeSpan) {
    this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan);
  }

  public void setPlace(E53_Place place) {
    this.resource.addProperty(CIDOC.P7_took_place_at, place.asResource());
  }

  private void addActivity(Resource actor, String function) {
    int futureCount = countActivity + 1;
    addActivity(actor, model.createResource(this.uri + "/" + futureCount + "/function")
      .addProperty(RDF.type, MUS.M31_Actor_Function)
      .addProperty(RDFS.label, function, "fr"));
  }

  private void addActivity(Resource actor, Resource function) {
    String a_uri = this.uri + "/" + ++countActivity;
    this.resource.addProperty(CIDOC.P9_consists_of,
      model.createResource(a_uri)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(CIDOC.P14_carried_out_by, RecordConverter.RadioFrance)
        .addProperty(MUS.U31_had_function_of_type, function)
    );

  }


}
