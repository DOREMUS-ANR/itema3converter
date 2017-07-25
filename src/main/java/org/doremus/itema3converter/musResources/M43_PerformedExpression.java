package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.doremus.itema3converter.files.Omu;
import org.doremus.ontology.MUS;

import java.time.Duration;

public class M43_PerformedExpression extends DoremusResource {
    public M43_PerformedExpression(Omu omu) {
        super(omu);
        this.resource.addProperty(RDF.type, MUS.M43_Performed_Expression);

        // Duration
        Duration duration = Duration.ofMillis(omu.durationMillis);
        this.resource.addProperty(MUS.U53_has_duration,
                model.createTypedLiteral(duration.toString(), XSDDatatype.XSDdayTimeDuration));

        // Title
        this.resource.addProperty(MUS.U70_has_title, omu.getTitle())
                .addProperty(RDFS.label, omu.getTitle());
    }
}
