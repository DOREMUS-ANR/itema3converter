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
public class OmuPersonne extends Itema3File {
  @XmlElement(name = "OMU_PERSONNE_ID")
  private String id;
  @XmlElement(name = "OMU_ID")
  public String omuId;
  @XmlElement(name = "PERSONNE_ID")
  public String personneID;
  @XmlElement(name = "MORALE_ID")
  public String moraleID;
  @XmlElement(name = "PROFESSION_ID")
  public int professionID;
  @XmlElement(name = "TYPE_MORALE_ID")
  public int typeMoraleID;
  @XmlElement(name = "INSTRUMENT_ID")
  public int instrumentId;
  @XmlElement(name = "TESSITURE_ID")
  public int tessitureId;
  @XmlElement(name = "OMU_PERSONNE_ROLE")
  public String role;

  @Override
  public String getId() {
    return id;
  }


  public static OmuPersonne fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(OmuPersonne.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (OmuPersonne) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<OmuPersonne> byOmu(String omuId) {
    List<OmuPersonne> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("OMU_PERSONNE", omuId))
      list.add(OmuPersonne.fromFile(f));
    return list;
  }


  public boolean hasInstrumentOrTessiture() {
    return instrumentId != 0 || tessitureId != 0;
  }
}
