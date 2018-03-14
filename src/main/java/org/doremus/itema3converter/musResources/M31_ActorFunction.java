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
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class M31_ActorFunction extends DoremusResource {
  private List<Integer> MOP_TYPES = Arrays.asList(19, 204, 205, 230, 231, 235, 181, 248);
  private List<Integer> MORALE_MOP_TYPES = Arrays.asList(1, 2, 304);

  private static List<M31_ActorFunction> list = null;

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
    if (professionID == 0) return null;
    if (list == null) init();

    for (M31_ActorFunction item : list)
      if (item.id == professionID) return item;
    return null;
  }

  public static M31_ActorFunction getMorale(int typeMoraleID) {
    if (typeMoraleID == 0) return null;
    if (list == null) init();

    for (M31_ActorFunction item : list)
      if (item.typeMoraleID == typeMoraleID) return item;
    return null;
  }

  public boolean isAFunction() {
    return id > 0 ?
      !MOP_TYPES.contains(id) :
      !MORALE_MOP_TYPES.contains(typeMoraleID);
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
      String appendix = this.typeMoraleID > 0 ? "m" : "";
      this.uri = ConstructURI.build(this.sourceDb, cName, this.getFunctionId() + appendix);
      this.resource = model.createResource(this.uri.toString())
        .addProperty(RDF.type, cClass)
        .addProperty(CIDOC.P1_is_identified_by, this.label, "fr")
        .addProperty(RDFS.label, this.label, "fr");

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public String getFunctionId() {
    if (typeMoraleID > 0) return "m" + typeMoraleID;
    return (functionID == null || functionID.isEmpty()) ?
      functionIDAlt : functionID;
  }

  public boolean isInterprete() {
    return isInterprete;
  }

  public boolean isAPlanningRole() {
    return isAPlanningRole;
  }

  public boolean isARecordingRole() {
    return isARecordingRole;
  }

  public boolean isAnEditingRole() {
    return isAnEditingRole;
  }

  public boolean isWorkAuthor() {
    return isAuthorMusic || isAuthorText || (typeMoraleID > 0 && isAuthor);
  }


  public Property getDefaultProperty() {
    if (this.isAFunction())
      return MUS.U31_had_function;
    if (this.id == 19)
      return MUS.U32_had_responsibility;
    return MUS.U1_used_medium_of_performance;
  }

}


