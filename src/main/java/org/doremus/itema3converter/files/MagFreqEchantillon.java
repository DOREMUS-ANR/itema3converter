package org.doremus.itema3converter.files;

import org.doremus.itema3converter.RecordConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class MagFreqEchantillon extends Itema3File {
  @XmlElement(name = "MAG_FREQ_ECHANTILLON_ID")
  private String id;
  @XmlElement(name = "MAG_FREQ_ECHANTILLON_LIB")
  private String sampleRate;

  @Override
  public String getId() {
    return id;
  }

  public String getSampleRate(){
    return sampleRate;
  }

  private static MagFreqEchantillon fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(MagFreqEchantillon.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (MagFreqEchantillon) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }


  public static MagFreqEchantillon getMagFreqEchantillon(int id ) {
    return MagFreqEchantillon.fromFile(RecordConverter.getFile("MAG_FREQ_ECHANTILLON", id + ""));
  }
}
