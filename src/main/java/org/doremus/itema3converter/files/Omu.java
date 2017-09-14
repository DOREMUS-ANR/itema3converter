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
  private static final String workCommentRegex = "(?i)^(Oeuvre (?!découpée)|Pour (?!le)).+";
  private static final String premiereRegex = "(^c|.*C)réation mondiale.+";

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
  @XmlElement(name = "OMU_TIMECODE")
  public int timecode;

  @Override
  public String getId() {
    return id;
  }

  public String getNote(String aClass) {
    note = note.trim();
    if (aClass == null) return note;

    if (note.matches(workCommentRegex))
      return aClass.equals("F22_SelfContainedExpression") ? note : null;
    else
      return aClass.equals("F22_SelfContainedExpression") ? null : note;
  }

  public boolean containsPremiere() {
    return note.matches(premiereRegex);
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
