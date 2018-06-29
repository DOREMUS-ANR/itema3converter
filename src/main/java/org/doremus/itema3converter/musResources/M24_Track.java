package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Sequence;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

public class M24_Track extends DoremusResource {
  public int timecode;
  public String title;

  public M24_Track(Sequence sequence, int order) {
    super(sequence);
    this.resource.addProperty(RDF.type, MUS.M24_Track)
      .addProperty(MUS.U227_has_content_type, "performed music", "en");

    // label
    title = sequence.label;
    this.resource
      .addProperty(CIDOC.P102_has_title, sequence.label)
      .addProperty(RDFS.label, sequence.label);

    // order
    this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(order));

    // duration
    this.resource.addProperty(MUS.U53_has_duration, sequence.getDuration().toString(), XSDDatatype.XSDdayTimeDuration);

    timecode = sequence.timecode;
  }

}
