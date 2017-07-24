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
public class Invite extends Itema3File {
    @XmlElement(name = "ITEM_ID")
    private String id;
    @XmlElement(name = "PERSONNE_ID")
    public String personneID;
    @XmlElement(name = "MORALE_ID")
    public String moraleID;
    @XmlElement(name = "PROFESSION_ID")
    public int professionID;
    @XmlElement(name = "TYPE_MORALE_ID")
    public int typeMoraleID;
    @XmlElement(name = "INVITE_INTERVENTION")
    public String note;
    @XmlElement(name = "INVITE_ACTIVITE")
    public String activity;
    @XmlElement(name = "INVITE_COMMENTAIRE")
    public String comment;

    @Override
    public String getId() {
        return id;
    }


    private static Invite fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Invite.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Invite) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Invite> byItem(String itemId) {
        List<Invite> list = new ArrayList<>();
        for (File f : RecordConverter.getFilesStartingWith("INVITE", itemId))
            list.add(Invite.fromFile(f));
        return list;
    }
}
