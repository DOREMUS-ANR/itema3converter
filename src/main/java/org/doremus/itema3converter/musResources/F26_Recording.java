package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.MagContenu;
import org.doremus.itema3converter.files.MagSupport;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F26_Recording extends DoremusResource {

  public F26_Recording(MagContenu mag) {
    super(mag);
    this.resource.addProperty(RDF.type, FRBROO.F26_Recording);

    // Sampling rate
    Float sampl = MagSupport.byMag(mag.getId()).get(0).getSampleRate();
    if (sampl != null) {
      Float hz = sampl * 1000;
      this.resource.addProperty(
        model.createProperty("http://dbpedia.org/ontology/frequency"), hz.toString(), XSDDatatype.XSDdouble);
    }

    // Duration
    if (mag.getDuration() != null)
      this.resource.addProperty(MUS.U53_has_duration, mag.getDuration().toString(), XSDDatatype.XSDdayTimeDuration);
  }

}
