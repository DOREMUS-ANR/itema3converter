package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.MUS;

public class M44_PerformedWork extends DoremusResource {
    public M44_PerformedWork(Omu omu) {
        super(omu);

        this.resource.addProperty(RDF.type, MUS.M44_Performed_Work);
    }
}
