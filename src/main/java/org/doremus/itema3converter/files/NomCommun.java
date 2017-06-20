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
public class NomCommun extends Itema3File {
    @XmlElement(name = "NOM_COMMUN_ID")
    private String id;
    @XmlElement(name = "NOM_COMMUN_LIB_RECH")
    private String label;

    @Override
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static NomCommun fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NomCommun.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (NomCommun) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
