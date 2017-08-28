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
public class ItemEmission extends Itema3File {
  @XmlElement(name = "ITEM_ID")
  private String id;
  @XmlElement(name = "EMISSION_ID")
  private String emissionId;

  @Override
  public String getId() {
    return id;
  }

  private static ItemEmission fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ItemEmission.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (ItemEmission) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<ItemEmission> byItem(String itemId) {
    List<ItemEmission> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("ITEM_EMISSION", itemId))
      list.add(ItemEmission.fromFile(f));
    return list;
  }

  public Emission getEmission() {
    return Emission.fromFile(RecordConverter.getFile("EMISSION", emissionId));
  }
}
