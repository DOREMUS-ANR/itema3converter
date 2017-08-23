package org.doremus.itema3converter.musResources;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Invite;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.itema3converter.files.Tessiture;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;

public class M28_IndividualPerformance extends DoremusResource {
  private String ITEMA3_VOC_PREFIX = "http://data.doremus.org/vocabulary/itema3/mop/";

  private final String personneID, moraleID;
  private int professionID, typeMoraleID;
  private final int instrument, tessitura;
  private final String role;


  public M28_IndividualPerformance(Invite record, URI uri) {
    super(uri);
    this.record = record;

    this.personneID = record.personneID;
    this.moraleID = record.moraleID;
    this.professionID = record.professionID;
    this.typeMoraleID = record.typeMoraleID;
    this.instrument = 0;
    this.tessitura = 0;
    this.role = null;

    if (professionID == 10) {
      // considerer qu'il s'agit d'une erreur et remplacer par PROFESSION_ID=2 Animateur, Presentateur
      professionID = 2;
    }

    init();


    this.resource
      .addProperty(CIDOC.P3_has_note, record.activity, "fr")
      .addProperty(CIDOC.P3_has_note, record.note, "fr")
      .addProperty(CIDOC.P3_has_note, record.comment, "fr");
  }

  public M28_IndividualPerformance(OmuPersonne record, URI uri) {
    super(uri);
    this.record = record;

    this.personneID = record.personneID;
    this.moraleID = record.moraleID;
    this.professionID = record.professionID;
    this.typeMoraleID = record.typeMoraleID;
    this.instrument = record.instrumentId;
    this.tessitura = record.tessitureId;
    this.role = record.role;

    init();
  }

  private void init() {
    OntClass resClass = personneID.isEmpty() ? FRBROO.F31_Performance : MUS.M28_Individual_Performance;
    DoremusResource carrier = personneID.isEmpty() ? new F11_Corporate_Body(moraleID) : new E21_Person(personneID);

    M31_ActorFunction af = professionID > 0 ?
      M31_ActorFunction.get(professionID) :
      M31_ActorFunction.getMorale(typeMoraleID);

    this.resource.addProperty(RDF.type, resClass)
      .addProperty(CIDOC.P14_carried_out_by, carrier.asResource());

    if (af != null) {
      this.resource.addProperty(af.getDefaultProperty(), af.asResource());
      model.add(af.getModel());
    }

    if (instrument > 0)
      this.resource.addProperty(MUS.U1_used_medium_of_performance,
        model.createResource(ITEMA3_VOC_PREFIX + instrument));
    else if (tessitura > 0) {
      String tessituraLabel = Tessiture.fromId(tessitura).getLabel()
        .replace(" (voix)", "").trim();
      this.resource.addProperty(MUS.U1_used_medium_of_performance,
        model.createResource().addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
          .addProperty(RDFS.label, tessituraLabel, "fr")
      );
    }

    if (role != null && !role.isEmpty()) {
      this.resource.addProperty(MUS.U27_performed_character,
        model.createResource().addProperty(RDF.type, FRBROO.F38_Character)
          .addProperty(RDFS.label, role)
      );
    }
  }
}
