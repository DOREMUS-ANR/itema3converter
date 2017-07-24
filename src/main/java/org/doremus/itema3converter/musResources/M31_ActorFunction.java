package org.doremus.itema3converter.musResources;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;
import com.opencsv.bean.customconverter.ConvertSplitOnWhitespace;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.List;

public class M31_ActorFunction extends DoremusResource {
    private static List list = null;

    @CsvBindByName(column = "ID", required = true)
    public int id;
    @CsvBindByName(column = "LIB", required = true)
    private String label;
    @CsvBindByName(column = "CU")
    private String functionID;
    @CsvBindByName(column = "CU_ALT")
    private String functionIDAlt;
    @CsvCustomBindByName(column = "M29 Editing", converter = ConvertGermanToBoolean.class)
    private boolean isAnEditingRole;
    @CsvCustomBindByName(column = "F25 Plan d'exécution", converter = ConvertGermanToBoolean.class)
    private boolean isAPlanningRole;
    @CsvCustomBindByName(column = "F29 Recording Event", converter = ConvertGermanToBoolean.class)
    private boolean isARecordingRole;
    @CsvCustomBindByName(column = "F28 Nouvelle expression", converter = ConvertGermanToBoolean.class)
    private boolean isAnOtherArtisticRole;


    public M31_ActorFunction() {
        this.resource = null;
    }

    private static void init() {
        File csv = new File(ClassLoader.getSystemClassLoader().getResource("ITEMA3_profession.csv").getFile());
        try {
            list = new CsvToBeanBuilder(new FileReader(csv)).withType(M31_ActorFunction.class).build().parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static M31_ActorFunction get(int professionID) {
        if (list == null) init();

        for (Object item : list) {
            M31_ActorFunction p = (M31_ActorFunction) item;
            if (p.id == professionID) return p;
        }
        return null;
    }

    @Override
    public Resource asResource() {
        if (resource == null) initResource();

        return resource;
    }

    private void initResource() {
        try {
            System.out.println(this.className);
            this.uri = ConstructURI.build(this.sourceDb, this.className, this.getFunctionId());

            this.resource = model.createResource(this.uri.toString())
                    .addProperty(RDF.type, MUS.M31_Actor_Function)
                    .addProperty(RDFS.label, this.label, "fr");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String getFunctionId() {
        return functionID.isEmpty() ? functionIDAlt : functionID;
    }

}


