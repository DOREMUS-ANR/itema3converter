package org.doremus.itema3converter.files;

import org.doremus.itema3converter.DateAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.util.Date;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Personne extends Itema3File {
    @XmlElement(name = "PERSONNE_ID")
    private String id;
    @XmlElement(name = "TH_STATUT_TERME_ID", required = true)
    private int status;
    @XmlElement(name = "PERSONNE_PRENOM")
    private String name;
    @XmlElement(name = "PERSONNE_NOM")
    private String surname;
    @XmlElement(name = "PERSONNE_PSEUDONYME")
    private String pseudonym;
    @XmlElement(name = "PERSONNE_DATE_NAISSANCE")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date birthDate;
    @XmlElement(name = "PERSONNE_DATE_DECES")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date deathDate;
    @XmlElement(name = "PERSONNE_ANNEE_NAISSANCE")
    private String birthYear;
    @XmlElement(name = "PERSONNE_ANNEE_DECES")
    private String deathYear;
    @XmlElement(name = "PERSONNE_COMMENTAIRE")
    private String comment;
    @XmlElement(name = "GENRE_PERSONNE_ID")
    private int gender;

    @Override
    public String getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        if(name.isEmpty()) return null;
        return name;
    }

    public String getSurname() {
        if (surname.isEmpty()) return null;
        return surname;
    }

    public String getPseudonym() {
        if(pseudonym.isEmpty()) return null;
        return pseudonym;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public String getDeathYear() {
        return deathYear;
    }

    public String getComment() {
        return comment;
    }

    public String getGender() {
        switch (gender) {
            case 1:
                return "male";
            case 2:
                return "female";
            case 3:
                return "child";
            default:
                return null;
        }
    }

    public static Personne fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Personne.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Personne) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
