package org.doremus.itema3converter.musResources;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.itema3converter.RecordConverter;
import org.doremus.itema3converter.files.*;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

public class F31_Performance extends DoremusResource {

    public F31_Performance(MagContenu mag, Item item) {
        super(mag);

        this.resource.addProperty(RDF.type, FRBROO.F31_Performance);

        // Performance: Date
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

        // Performance: Place
        for (ItemThLieuGeo t : ItemThLieuGeo.byItem(item.getId())) {
            E53_Place p = new E53_Place(t.getLieuGeoId());
            this.resource.addProperty(CIDOC.P7_took_place_at, p.asResource());
        }

        // Performance: Title
        this.resource.addProperty(RDFS.label, item.getLabel());
        this.resource.addProperty(CIDOC.P102_has_title, item.getLabel());

        // Performance: General Topic
        List<ItemIdxFile> generalTopics = ItemIdxNc.byItem(item.getId());
        generalTopics.addAll(ItemIdxRencontre.byItem(item.getId()));
        for (ItemIdxFile t : generalTopics) {
            try {
                String uri = ConstructURI.build("rfi", "theme", t.getIdxId()).toString();

                Resource topic = model.createResource(uri)
                        .addProperty(RDF.type, CIDOC.E1_CRM_Entity)
                        .addProperty(DCTerms.identifier, t.getIdxId())
                        .addProperty(RDFS.label, t.getIdxLabel());
                this.resource.addProperty(CIDOC.P129_is_about, topic);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // Performance: Geo Topic
        // FIXME the property U22 is not yet in the ontology
//        for (ItemIdxGeo t : ItemIdxGeo.byItem(item.getId())) {
//            E53_Place p = new E53_Place(t.getLieuGeoId());
//            this.resource.addProperty(MUS.U22_is_about_place, p.asResource());
//        }


        // Performance: Person
        // TODO

        // Performance: comment
        for (String s : new String[]{item.getDescription(), item.getAnalyseDoc()})
            if (s != null && !s.isEmpty())
                this.resource.addProperty(CIDOC.P3_has_note, s, "fr");

    }

}
