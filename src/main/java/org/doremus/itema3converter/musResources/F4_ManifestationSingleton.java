package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.MagSupport;
import org.doremus.itema3converter.files.MagTypeSupport;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F4_ManifestationSingleton extends DoremusResource {
  private static int countActivity;

  public F4_ManifestationSingleton(MagSupport support) {
    super(support);
    countActivity = 0;

    this.resource.addProperty(RDF.type, FRBROO.F4_Manifestation_Singleton);

    // Format (i.e. CD)
    MagTypeSupport supportType = support.getType();
    this.resource.addProperty(CIDOC.P2_has_type,
      model.createResource("http://data.doremus.org/support/" + supportType.getCode())
        .addProperty(RDF.type, CIDOC.E55_Type)
        .addProperty(RDFS.label, supportType.label)
        .addProperty(CIDOC.P1_is_identified_by, supportType.label)
    );

    // N. Magneto
    if (!support.numMagneto.isEmpty()) {
      this.resource.removeAll(DCTerms.identifier);
      this.resource
        .addProperty(DCTerms.identifier, support.numMagneto)
        .addProperty(CIDOC.P1_is_identified_by, support.numMagneto);
    }
  }


}
