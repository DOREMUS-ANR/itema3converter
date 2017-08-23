package org.doremus.itema3converter.files;

import org.doremus.itema3converter.RecordConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class OmuTypeMusicalDoc extends Itema3File {
  @XmlElement(name = "OMU_ID")
  public String id;
  @XmlElement(name = "TYPE_MUSICAL_DOC_ID")
  public String musicalType;

  @Override
  public String getId() {
    return id;
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


}
