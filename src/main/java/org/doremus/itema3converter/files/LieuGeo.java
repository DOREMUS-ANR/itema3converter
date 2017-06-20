package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class LieuGeo extends Itema3File {
    @XmlElement(name = "LIEU_GEO_ID")
    private String id;
    @XmlElement(name = "LIEU_GEO_PERE_ID")
    private String fatherId;
    @XmlElement(name = "LIEU_GEO_LIB_RECH")
    private String label;
    @XmlElement(name = "LIEU_GEO_QUALIF")
    private String qualif;

    @Override
    public String getId() {
        return id;
    }

    public String getFatherId() {
        return fatherId;
    }

    public String getLabel() {
        return label;
    }

    public String getQualif() {
        return qualif;
    }

    public static LieuGeo fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LieuGeo.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (LieuGeo) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
