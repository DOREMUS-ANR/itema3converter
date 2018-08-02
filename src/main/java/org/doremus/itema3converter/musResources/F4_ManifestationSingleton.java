package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.MagSupport;
import org.doremus.itema3converter.files.MagTypeSupport;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F4_ManifestationSingleton extends DoremusResource {

  public F4_ManifestationSingleton(MagSupport support) {
    super(support);
    this.resource.addProperty(RDF.type, FRBROO.F4_Manifestation_Singleton);

    // Format (i.e. CD)
    MagTypeSupport supportType = support.getType();
    this.resource.addProperty(MUS.U207_has_carrier_type,
      model.createResource("http://data.doremus.org/support/" + supportType.getCode())
        .addProperty(RDF.type, MUS.M169_Carrier_Type)
        .addProperty(RDFS.label, supportType.label)
        .addProperty(CIDOC.P1_is_identified_by, supportType.label)
    );

    // N. Magneto
    if (!support.numMagneto.isEmpty()) {
      this.resource.removeAll(DC.identifier);
      this.resource
        .addProperty(DC.identifier, support.numMagneto)
        .addProperty(CIDOC.P1_is_identified_by, support.numMagneto);
    }
  }
}
