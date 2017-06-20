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
public class ItemThLieuGeo extends Itema3File {
    @XmlElement(name = "ITEM_ID")
    private String id;
    @XmlElement(name = "LIEU_GEO_ID")
    private String lieuGeoId;

    @Override
    public String getId() {
        return id;
    }

    public String getLieuGeoId() {
        return lieuGeoId;
    }

    private static ItemThLieuGeo fromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ItemThLieuGeo.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (ItemThLieuGeo) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemThLieuGeo> byItem(String itemId) {
        List<ItemThLieuGeo> list = new ArrayList<>();
        for (File f : RecordConverter.getFilesStartingWith("ITEM_TH_LIEU_GEO", itemId))
            list.add(ItemThLieuGeo.fromFile(f));
        return list;
    }
}
