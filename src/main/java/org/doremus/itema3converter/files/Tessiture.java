package org.doremus.itema3converter.files;

import org.doremus.itema3converter.RecordConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Tessiture extends Itema3File {
    @XmlElement(name = "TESSITURE_ID")
    private String id;
    @XmlElement(name = "TESSITURE_LIB")
    private String label;

    @Override
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static Tessiture fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Tessiture.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Tessiture) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Tessiture fromId(int id) {
        File f = RecordConverter.getFile("TESSITURE", id + "");
        return fromFile(f);
    }

}
