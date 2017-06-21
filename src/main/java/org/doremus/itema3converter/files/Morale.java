package org.doremus.itema3converter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Morale extends Itema3File {
    @XmlElement(name = "MORALE_ID")
    private String id;
    @XmlElement(name = "TH_STATUT_TERME_ID", required = true)
    private int status;
    @XmlElement(name = "TYPE_MORALE_ID", required = true)
    private int type;
    @XmlElement(name = "MORALE_LIB")
    private String name;
    @XmlElement(name = "MORALE_DATE_DEBUT")
    private String birthDate;
    @XmlElement(name = "MORALE_DATE_FIN")
    private String deathDate;
    @XmlElement(name = "MORALE_COMMENTAIRE")
    private String comment;
    @XmlElement(name = "MORALE_QUALIF")
    private String qualif;

    @Override
    public String getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        if (name.isEmpty()) return null;
        return name;
    }

    public String getBirthDate() {
        return birthDate.isEmpty() ? null : birthDate;
    }

    public String getDeathDate() {
        return deathDate.isEmpty() ? null : deathDate;
    }

    public String getComment() {
        return comment;
    }


    public static Morale fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Morale.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Morale) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
