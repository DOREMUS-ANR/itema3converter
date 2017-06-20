package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.Item;
import org.doremus.itema3converter.files.MagContenu;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.util.Date;

public class F31_Performance extends DoremusResource {

    public F31_Performance(MagContenu mag, Item item) {
        super(mag);

        this.resource.addProperty(RDF.type, FRBROO.F31_Performance);

        // TODO ask for start time (if any)
        Date start = mag.getDateEnreg();
        Date end = mag.getDateEnreg();


        Resource timeSpan = model.createResource(this.uri + "/time")
                .addProperty(RDF.type, CIDOC.E52_Time_Span)
                .addProperty(CIDOC.P79_beginning_is_qualified_by, RecordConverter.ISODateFormat.format(start),
                        XSDDatatype.XSDdate)
                .addProperty(CIDOC.P80_end_is_qualified_by, RecordConverter.ISODateFormat.format(end), XSDDatatype
                        .XSDdate);
        this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan);


    }

}
