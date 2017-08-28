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
public class Emission extends Itema3File {
    @XmlElement(name = "EMISSION_ID")
    private String id;
    @XmlElement(name = "EMISSION_NUM")
    public int num;

    @Override
    public String getId() {
        return id;
    }


    public static Emission fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Emission.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Emission) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
