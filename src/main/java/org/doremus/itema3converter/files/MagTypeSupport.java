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
public class MagTypeSupport extends Itema3File {
  @XmlElement(name = "MAG_SUPPORT_ID")
  private String id;
  @XmlElement(name = "MAG_TYPE_SUPPORT_CAR_CODE_TYPE")
  private String code;
  @XmlElement(name = "MAG_TYPE_SUPPORT_LIB")
  public String label;

  @Override
  public String getId() {
    return id;
  }

  public String getCode() {
    return code.isEmpty() ? label.toLowerCase() : code;
  }

  private static MagTypeSupport fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(MagTypeSupport.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (MagTypeSupport) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static MagTypeSupport getMagTypeSupport(int id) {
    return MagTypeSupport.fromFile(RecordConverter.getFile("MAG_TYPE_SUPPORT", id + ""));
  }

}
