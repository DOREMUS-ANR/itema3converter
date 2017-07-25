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
public class ItemOmu extends Itema3File {
    @XmlElement(name = "ITEM_ID")
    private String id;
    @XmlElement(name = "OMU_ID")
    private String omuId;
    @XmlElement(name = "ITEM_OMU_ORDRE")
    public int omuOrderNum;

    @Override
    public String getId() {
        return id;
    }

    private static ItemOmu fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ItemOmu.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (ItemOmu) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemOmu> byItem(String itemId) {
        List<ItemOmu> list = new ArrayList<>();
        for (File f : RecordConverter.getFilesStartingWith("ITEM_OMU", itemId))
            list.add(ItemOmu.fromFile(f));
        return list;
    }

    public Omu getOmu() {
        return Omu.fromFile(RecordConverter.getFile("OMU", omuId));
    }
}
