package org.doremus.itema3converter.files;

import org.doremus.itema3converter.RecordConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class Sequence extends Itema3File implements Comparable<Sequence> {
  public enum TYPE {AMBIENT, STANDARD}

  @XmlElement(name = "SEQUENCE_ID")
  private String id;
  @XmlElement(name = "SEQUENCE_TIMECODE")
  public int timecode;
  @XmlElement(name = "SEQUENCE_DUREE")
  private int duration;
  @XmlElement(name = "SEQUENCE_LIB")
  public String label;
  @XmlElement(name = "TYPE_SEQUENCE_ID")
  private int type;

  @Override
  public String getId() {
    return id;
  }

  public TYPE getType() {
    return TYPE.values()[type - 1];
  }

  public Duration getDuration() {
    return Duration.ofMillis(duration);
  }

  private static Sequence fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Sequence.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (Sequence) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static List<Sequence> byMag(String magId) {
    List<Sequence> list = new ArrayList<>();
    for (File f : RecordConverter.getFilesStartingWith("SEQUENCE", magId))
      list.add(Sequence.fromFile(f));

    Collections.sort(list);
    return list;
  }

  @Override
  public int compareTo(Sequence other) {
    return this.timecode - other.timecode;
  }

}
