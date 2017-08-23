package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Omu extends Itema3File {
  @XmlElement(name = "OMU_ID")
  private String id;
  @XmlElement(name = "OMU_TITRE")
  private String title;
  @XmlElement(name = "OMU_COMMENTAIRE")
  private String note;
  @XmlElement(name = "OMU_DESCRIPTION")
  public String workNote;
  @XmlElement(name = "OMU_DUREE_MS")
  public int durationMillis;
  @XmlElement(name = "OMU_DATE_COMPOSITION")
  public String compositionDate;


  @Override
  public String getId() {
    return id;
  }

  public String getNote() {
    return note;
  }

  public String getTitle() {
    return title.trim();
  }

  public static Omu fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Omu.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (Omu) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

}
