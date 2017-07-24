package org.doremus.itema3converter.musResources;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.itema3converter.files.Invite;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;

public class M28_IndividualPerformance extends DoremusResource {

    public M28_IndividualPerformance(Invite record, URI uri) {
        super(uri);

        this.record = record;

        OntClass resClass = (record.personneID.isEmpty()) ? FRBROO.F31_Performance : MUS.M28_Individual_Performance;
        DoremusResource carrier = (record.personneID.isEmpty()) ?
                new F11_Corporate_Body(record.moraleID) :
                new E21_Person(record.personneID);

        Resource function = null;
        // TODO can we map some function with MoP vocabularies ?
        if (record.professionID > 0) {
            if (record.professionID == 10) {
                // considérer qu'il s'agit d'une erreur et remplacer par PROFESSION_ID=2 Animateur, Présentateur
                record.professionID = 2;
            }
            M31_ActorFunction f = M31_ActorFunction.get(record.professionID);
            function = f.asResource();
            model.add(f.getModel());
        }
        // TODO can we map some TYPE_MORALE with MoP vocabularies ?


        this.resource.addProperty(RDF.type, resClass)
                .addProperty(CIDOC.P14_carried_out_by, carrier.asResource())
                .addProperty(MUS.U31_had_function_of_type, function)
                .addProperty(CIDOC.P3_has_note, record.activity)
                .addProperty(CIDOC.P3_has_note, record.note)
                .addProperty(CIDOC.P3_has_note, record.comment)
        ;

    }
}
