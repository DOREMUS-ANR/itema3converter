package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class AnalyseDoc extends Itema3File {
    @XmlElement(name = "ANALYSE_DOC_ID")
    private String id;
    @XmlElement(name = "ANALYSE_DOC_VALUE")
    private String value;

    @Override
    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static AnalyseDoc fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ItemIdxGeo.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (AnalyseDoc) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
