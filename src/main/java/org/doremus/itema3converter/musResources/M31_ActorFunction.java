package org.doremus.itema3converter.musResources;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.itema3converter.ConstructURI;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class M31_ActorFunction extends DoremusResource {
  private List<Integer> MOP_TYPES = Arrays.asList(19, 204, 205, 230, 231, 235, 181, 248);

  private static List list = null;

  @CsvBindByName(column = "ID")
  private int id;
  @CsvBindByName(column = "TYPE_MORALE_ID")
  public int typeMoraleID;
  @CsvBindByName(column = "LIB", required = true)
  private String label;
  @CsvBindByName(column = "CU")
  private String functionID;
  @CsvBindByName(column = "CU_ALT")
  private String functionIDAlt;
  @CsvCustomBindByName(column = "AUTEUR_TEXTE", converter = ConvertGermanToBoolean.class)
  private boolean isAuthorText;
  @CsvCustomBindByName(column = "AUTEUR_MUSIQUE", converter = ConvertGermanToBoolean.class)
  private boolean isAuthorMusic;
  @CsvCustomBindByName(column = "AUTEUR", converter = ConvertGermanToBoolean.class)
  private boolean isAuthor;
  @CsvCustomBindByName(column = "INTERPRETE", converter = ConvertGermanToBoolean.class)
  private boolean isInterprete;
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
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    File csvProfession = new File(cl.getResource("ITEMA3_profession.csv").getFile());
    File csvTypeMorale = new File(cl.getResource("ITEMA3_type_morale.csv").getFile());
    try {
      list = new CsvToBeanBuilder(new FileReader(csvProfession)).withType(M31_ActorFunction.class).build().parse();
      list.addAll(new CsvToBeanBuilder(new FileReader(csvTypeMorale))
        .withType(M31_ActorFunction.class).build().parse());
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

  public static M31_ActorFunction getMorale(int typeMoraleID) {
    if (list == null) init();

    for (Object item : list) {
      M31_ActorFunction p = (M31_ActorFunction) item;
      if (p.id == typeMoraleID) return p;
    }
    return null;
  }

  public boolean isAFunction() {
    return id > 0 ?
      !MOP_TYPES.contains(id) :
      typeMoraleID != 304;
  }

  @Override
  public Resource asResource() {
    if (resource == null) initResource();

    return resource;
  }

  private void initResource() {
    try {
      String cName;
      OntClass cClass;

      if (this.isAFunction()) {
        cName = this.className;
        cClass = MUS.M31_Actor_Function;
      } else if (this.id == 19) {
        cName = "M32_ActorResponsibility";
        cClass = MUS.M32_Actor_Responsibility;
      } else {
        cName = "M14_MediumOfPerformance";
        cClass = MUS.M14_Medium_Of_Performance;
      }
      this.uri = ConstructURI.build(this.sourceDb, cName, this.getFunctionId());
      this.resource = model.createResource(this.uri.toString())
        .addProperty(RDF.type, cClass)
        .addProperty(RDFS.label, this.label, "fr");

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public String getFunctionId() {
    if (typeMoraleID > 0) return "m" + typeMoraleID;
    return functionID.isEmpty() ? functionIDAlt : functionID;
  }

  public boolean isInterprete() {
    return isInterprete;
  }

  public boolean isAPlanningRole() {
    return isAPlanningRole;
  }

  public boolean isWorkAuthor() {
    return isAuthorMusic || isAuthorText || (typeMoraleID > 0 && isAuthor);
  }


  public Property getDefaultProperty() {
    if (this.isAFunction())
      return MUS.U31_had_function_of_type;
    if (this.id == 19)
      return MUS.U32_had_responsibility_of_type;
    return MUS.U1_used_medium_of_performance;
  }
}


