package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Item extends Itema3File {
    @XmlElement(name = "ITEM_ID")
    private String id;

    @XmlElement(name = "STATUT_DOC_ID")
    private int status;

    @Override
    public String getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public static Item fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Item.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Item) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
