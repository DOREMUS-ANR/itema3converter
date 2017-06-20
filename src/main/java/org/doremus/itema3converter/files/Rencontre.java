package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Rencontre extends Itema3File {
    @XmlElement(name = "RENCONTRE_ID")
    private String id;
    @XmlElement(name = "RENCONTRE_LIB_RECH")
    private String label;

    @Override
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static Rencontre fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Rencontre.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Rencontre) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
