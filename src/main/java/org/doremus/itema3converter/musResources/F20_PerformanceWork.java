package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.FRBROO;

public class F20_PerformanceWork extends DoremusResource {

    public F20_PerformanceWork(Omu omu) {
      super(omu);
        this.resource.addProperty(RDF.type, FRBROO.F20_Performance_Work);


    }

}
