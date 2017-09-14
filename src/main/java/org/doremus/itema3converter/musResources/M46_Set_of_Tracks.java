package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.MagContenu;
import org.doremus.ontology.MUS;

public class M46_Set_of_Tracks extends DoremusResource {
  public M46_Set_of_Tracks(MagContenu mag) {
    super(mag);
    this.resource.addProperty(RDF.type, MUS.M46_Set_of_Tracks);

    // Duration
    if (mag.getDuration() != null)
      this.resource.addProperty(MUS.U53_has_duration, mag.getDuration().toString(), XSDDatatype.XSDdayTimeDuration);
  }
}
