package org.doremus.itema3converter.files;

import org.doremus.itema3converter.RecordConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class OmuTypeMusicalDoc extends Itema3File {
  private static final List<String> PERIODS = Arrays.asList("82,83,84,85,86,61,62".split(","));
  private final static String GENRE_NAMESPACE = "http://data.doremus.org/vocabulary/itema3/genre/musdoc/";
  private final static String PERIOD_NAMESPACE = "http://data.doremus.org/period/";

  @XmlElement(name = "OMU_ID")
  public String id;
  @XmlElement(name = "TYPE_MUSICAL_DOC_ID")
  private String musicalType;


  @Override
  public String getId() {
    return id;
  }

  private String getMusicalType() {
    if (musicalType.isEmpty()) return null;
    return musicalType.trim();
  }

  public boolean isPeriod() {
    return PERIODS.contains(this.getMusicalType());
  }

  public String getPeriodCentury() {
    if (this.getMusicalType() == null) return null;
    switch (this.getMusicalType()) {
      case "84":
        return "16";
      case "82":
        return "17";
      case "83":
        return "18";
      case "85":
        return "19";
      case "86":
        return "20";
      case "61":
        return "20";
      case "62":
        return "21";
      default:
        return null;
    }
  }

  public String getUri() {
    if (this.getMusicalType() == null) return null;
    if (this.isPeriod())
      return PERIOD_NAMESPACE + getPeriod(this.getMusicalType());

    return GENRE_NAMESPACE + this.getMusicalType();
  }

  public static OmuTypeMusicalDoc fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(OmuTypeMusicalDoc.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (OmuTypeMusicalDoc) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<OmuTypeMusicalDoc> byOmu(String omuId) {
    List<OmuTypeMusicalDoc> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("OMU_TYPE_MUSICAL_DOC", omuId))
      list.add(OmuTypeMusicalDoc.fromFile(f));
    return list;
  }

  private static String getPeriod(String code) {
    switch (code) {
      case "84":
        return "16_century";
      case "82":
        return "17_century";
      case "83":
        return "18_century";
      case "85":
        return "19_century";
      case "86":
        return "20_century_1";
      case "61":
        return "20_century_2";
      case "62":
        return "21_century";
      default:
        return null;
    }
  }
}
