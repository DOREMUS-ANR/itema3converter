package org.doremus.itema3converter.files;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class TypeMusicalDoc {
  private static List<TypeMusicalDoc> list = null;

  @CsvBindByName(column = "TYPE_MUSICAL_DOC_ID")
  private String id;
  @CsvBindByName(column = "TYPE_MUSICAL_DOC_LIB")
  private String label;
  @CsvCustomBindByName(column = "M19 Categorization", converter = ConvertGermanToBoolean.class)
  private boolean categorization;
  @CsvCustomBindByName(column = "M40 Geographical Context", converter = ConvertGermanToBoolean.class)
  private boolean geoContext;
  @CsvCustomBindByName(column = "PERIODE", converter = ConvertGermanToBoolean.class)
  private boolean period;
  @CsvBindByName(column = "Autre")
  private String otherType;

  public String getType() {
    if (categorization)
      return "categorization";
    if (geoContext)
      return "geocontext";
    if (period)
      return "period";
    if (otherType != null && !otherType.isEmpty())
      return "other";
    return "genre";

    // future:
    // if (VocabularyManager.getVocabulary("genre-itema3-musdoc").getConcept(id) != null)
    //      return "genre";
  }

  public static TypeMusicalDoc get(String id) {
    if (list == null) init();

    return list.stream()
      .filter(x -> x.id.equals(id))
      .findFirst().orElse(null);
  }

  public boolean isPeriod() {
    return period;
  }

  public boolean isGeoContext() {
    return geoContext;
  }

  public String getLabel() {
    return label;
  }


  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    File csv = new File(cl.getResource("ITEMA3_TYPE_MUSICAL.csv").getFile());
    try {
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(TypeMusicalDoc.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


}
