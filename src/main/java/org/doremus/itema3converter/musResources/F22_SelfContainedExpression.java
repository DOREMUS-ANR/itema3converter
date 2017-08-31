package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Omu;
import org.doremus.itema3converter.files.OmuPersonne;
import org.doremus.itema3converter.files.OmuTypeMusicalDoc;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F22_SelfContainedExpression extends DoremusResource {
  private final static String GENRE_NAMESPACE = "http://data.doremus.org/vocabulary/itema3/genre/musdoc/";

  public F22_SelfContainedExpression(Omu omu) {
    super(omu);

    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression)
      .addProperty(CIDOC.P102_has_title, omu.getTitle())
      .addProperty(RDFS.label, omu.getTitle());
    // TODO the title can also contain Catalogue, Opus and Key

    // genre
    for (OmuTypeMusicalDoc ot : OmuTypeMusicalDoc.byOmu(omu.getId()))
      this.resource.addProperty(MUS.U12_has_genre, model.createResource(GENRE_NAMESPACE + ot.musicalType));

    // note
    addNote(omu.workNote);
  }

}
