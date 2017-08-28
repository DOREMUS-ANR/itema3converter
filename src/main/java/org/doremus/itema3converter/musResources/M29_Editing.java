package org.doremus.itema3converter.musResources;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.List;

public class M29_Editing extends DoremusResource {
  private static int countActivity;

  public M29_Editing(MagContenu mag, Item item) {
    super(mag);
    countActivity = 0;

    this.resource.addProperty(RDF.type, MUS.M29_Editing);

    List<ItemEmission> emissions = ItemEmission.byItem(item.getId());
    if (!emissions.isEmpty()) {
      Emission emission = emissions.get(0).getEmission();
      if (emission.num != 1107)
        addActivity(RecordConverter.RadioFrance, "Editeur");
    }

    // Activities
    for (ItemProducteur ip : ItemProducteur.byItem(item.getId())) {
      M31_ActorFunction af = M31_ActorFunction.get(ip.professionId);
      assert af != null;
      if (!af.isAnEditingRole()) continue;
      E21_Person person = new E21_Person(ip.personneId);
      addActivity(person.asResource(), af.asResource());
    }
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
        .addProperty(CIDOC.P14_carried_out_by, actor)
        .addProperty(MUS.U31_had_function_of_type, function)
    );
  }
}
