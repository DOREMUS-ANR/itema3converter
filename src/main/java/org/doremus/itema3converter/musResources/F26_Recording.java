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
    String sampl = MagSupport.byMag(mag.getId()).get(0).getSampleRate();
    this.resource.addProperty(
      CIDOC.P43_has_dimension,
      model.createResource("http://data.doremus.org/rate/" + sampl)
        .addProperty(RDF.type, CIDOC.E54_Dimension)
        .addProperty(CIDOC.P2_has_type, model.createResource("http://dbpedia.org/resource/Sampling_rate"))
        .addProperty(CIDOC.P90_has_value, sampl, XSDDatatype.XSDfloat)
        .addProperty(CIDOC.P91_has_unit, model.createResource("http://dbpedia.org/datatype/kilohertz")
        )
    );

    // Duration
    if (mag.getDuration() != null)
      this.resource.addProperty(MUS.U53_has_duration, mag.getDuration().toString(), XSDDatatype.XSDdayTimeDuration);
  }

}
