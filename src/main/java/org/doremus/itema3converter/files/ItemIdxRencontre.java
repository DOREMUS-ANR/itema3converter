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
public class ItemIdxRencontre extends ItemIdxFile {
    @XmlElement(name = "ITEM_ID")
    private String id;
    @XmlElement(name = "RENCONTRE_ID")
    private String rencontreId;

    @Override
    public String getId() {
        return id;
    }

    public String getIdxId() {
        return rencontreId;
    }

    public String getIdxLabel() {
        Rencontre nc = Rencontre.fromFile(RecordConverter.getFile("RENCONTRE", rencontreId));
        assert nc != null;
        return nc.getLabel();
    }

    private static ItemIdxRencontre fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ItemIdxRencontre.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (ItemIdxRencontre) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemIdxFile> byItem(String itemId) {
        List<ItemIdxFile> list = new ArrayList<>();
        for (File f : RecordConverter.getFilesStartingWith("ITEM_IDX_RENCONTRE", itemId))
            list.add(ItemIdxRencontre.fromFile(f));
        return list;
    }
}
