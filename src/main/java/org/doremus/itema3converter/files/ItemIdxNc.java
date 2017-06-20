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
public class ItemIdxNc extends ItemIdxFile {
    @XmlElement(name = "ITEM_ID")
    private String id;
    @XmlElement(name = "NOM_COMMUN_ID")
    private String ncId;

    @Override
    public String getId() {
        return id;
    }

    public String getIdxId() {
        return ncId;
    }

    public String getIdxLabel() {
        NomCommun nc = NomCommun.fromFile(RecordConverter.getFile("NOM_COMMUN", ncId));
        assert nc != null;
        return nc.getLabel();
    }

    private static ItemIdxNc fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ItemIdxNc.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (ItemIdxNc) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemIdxFile> byItem(String itemId) {
        List<ItemIdxFile> list = new ArrayList<>();
        for (File f : RecordConverter.getFilesStartingWith("ITEM_IDX_NC", itemId))
            list.add(ItemIdxNc.fromFile(f));
        return list;
    }
}
