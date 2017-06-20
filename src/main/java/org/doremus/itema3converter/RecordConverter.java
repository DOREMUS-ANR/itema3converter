package org.doremus.itema3converter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.files.Item;
import org.doremus.itema3converter.files.MagContenu;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.doremus.itema3converter.musResources.DoremusResource;
import org.doremus.itema3converter.musResources.F31_Performance;
import org.doremus.ontology.PROV;

// Convert entirely an entire single ITEM of ITEMA3, from performance to track
public class RecordConverter {
    public static Resource RadioFrance;
    //    public static final DateFormat ISODateTimeFormat = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss");
//    public static final DateFormat ISOTimeFormat = new SimpleDateFormat("hh:mm:ss");
    public static final DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Logger log = MyLogger.getLogger(RecordConverter.class.getName());
    private Resource provEntity, provActivity;

    private Model model;


    public RecordConverter(File mc) throws URISyntaxException {
        log.setLevel(Level.OFF);

        model = ModelFactory.createDefaultModel();
        RadioFrance = model.createResource("http://data.doremus.org/organization/Radio_France");

        MagContenu mag = MagContenu.fromFile(mc);

        System.out.println("MAG_CONTENU " + mag.getId());

        Item item = Item.fromFile(getFile("ITEM", mag.getItemId()));
        assert item != null;
        log.info("ITEM " + item.getId());

        if (item.getStatus() != 4) {
            log.warning("Item with status " + item.getStatus() + " (expected 4). Skipping it");
            return;
        }

        // PROV-O tracing
        provEntity = model.createResource("http://data.doremus.org/source/itema3/" + mag.getItemId())
                .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, RadioFrance);

        provActivity = model.createResource(ConstructURI.build("rf", "prov", mag.getId()).toString())
                .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
                .addProperty(PROV.used, provEntity)
                .addProperty(RDFS.comment, "Reprise et conversion de la notice avec MAG_CONTENU_ID " + mag.getId() +
                        " de la base ITEMA3 de Radio France", "fr")
                .addProperty(RDFS.comment, "Resumption and conversion of the record with MAG_CONTENU_ID " + mag.getId()
                        + " of the dataset ITEMA3 of Radio France", "en")
                .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);


        // start parsing

        F31_Performance f31 = new F31_Performance(mag, item);
        model.add(f31.getModel());

        log.info("\n");
    }

    private void addProvenanceTo(DoremusResource res) {
        res.asResource().addProperty(RDF.type, PROV.Entity)
                .addProperty(PROV.wasAttributedTo, model.createResource("http://data.doremus.org/organization/DOREMUS"))
                .addProperty(PROV.wasDerivedFrom, this.provEntity)
                .addProperty(PROV.wasGeneratedBy, this.provActivity);
    }


    public static File getFile(String type, String id) {
        return new File(Paths.get(Converter.dataFolderPath, type, id + ".xml").toString());
    }

    public static List<File> getFilesStartingWith(String type, String start) {
        List<File> fileList = new ArrayList<>();
        File dir = new File(Paths.get(Converter.dataFolderPath, type).toString());
        for(File file : dir.listFiles()) {
            if(file.getName().startsWith(start))
                fileList.add(file);
        }

        return fileList;
    }

    public Model getModel() {
        return model;
    }
}
