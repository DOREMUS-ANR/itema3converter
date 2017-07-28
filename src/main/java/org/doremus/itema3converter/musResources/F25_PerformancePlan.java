package org.doremus.itema3converter.musResources;

import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.FRBROO;

public class F25_PerformancePlan extends DoremusResource {

    public F25_PerformancePlan(Omu omu) {
        super(omu);
        this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);


    }

}
