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
public class ItemProducteur extends Itema3File {
  @XmlElement(name = "ITEM_PRODUCTEUR_ID")
  private String id;
  @XmlElement(name = "PERSONNE_ID")
  public String personneId;
  @XmlElement(name = "PROFESSION_ID")
  public int professionId;

  @Override
  public String getId() {
    return id;
  }

  private static ItemProducteur fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ItemProducteur.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (ItemProducteur) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<ItemProducteur> byItem(String itemId) {
    List<ItemProducteur> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("ITEM_PRODUCTEUR", itemId))
      list.add(ItemProducteur.fromFile(f));
    return list;
  }

}
