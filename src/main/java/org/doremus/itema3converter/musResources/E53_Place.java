package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.GeoNames;
import org.doremus.itema3converter.files.LieuGeo;
import org.doremus.ontology.CIDOC;

import java.net.URI;

public class E53_Place extends DoremusResource {
  public E53_Place(LieuGeo record) {
    super(record);

    String label = record.getLabel();
    if (record.getQualif() != null && !record.getQualif().isEmpty())
      label += " (" + record.getQualif() + ")";

    this.resource.addProperty(RDF.type, CIDOC.E53_Place)
      .addProperty(RDFS.label, label)
      .addProperty(model.createProperty(GeoNames.NAME), label)
      .addProperty(CIDOC.P1_is_identified_by, record.getLabel())
      .addProperty(CIDOC.P89_falls_within, new E53_Place(record.getFatherId()).asResource());

  }

  public E53_Place(String identifier) {
    super(identifier);

    int match = GeoNames.get(identifier);
    if (match != -1) {
      this.uri = URI.create(GeoNames.toURI(match));
      this.resource = model.createResource(this.uri.toString());
    }
  }
}
