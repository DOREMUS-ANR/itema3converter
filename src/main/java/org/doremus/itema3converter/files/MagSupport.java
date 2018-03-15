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
public class MagSupport extends Itema3File {
  @XmlElement(name = "MAG_SUPPORT_ID")
  private String id;
  @XmlElement(name = "MAG_FREQ_ECHANTILLON_ID")
  private int sampleRateId;
  @XmlElement(name = "MAG_TYPE_SUPPORT_ID")
  private int type;
  @XmlElement(name = "MAG_SUPPORT_NUM_MAGNETO_SIMPLE")
  public String numMagneto;

  @Override
  public String getId() {
    return id;
  }

  public Float getSampleRate() {
    MagFreqEchantillon mfe = MagFreqEchantillon.getMagFreqEchantillon(this.sampleRateId);
    String rate = mfe.getSampleRate();
    if(rate.isEmpty()) return null;
    return Float.parseFloat(rate);
  }

  public MagTypeSupport getType() {
    return MagTypeSupport.getMagTypeSupport(this.type);
  }

  private static MagSupport fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(MagSupport.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (MagSupport) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<MagSupport> byMag(String magId) {
    List<MagSupport> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("MAG_SUPPORT", magId))
      list.add(MagSupport.fromFile(f));
    return list;
  }
}
