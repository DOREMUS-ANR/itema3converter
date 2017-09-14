package org.doremus.itema3converter.files;

import org.doremus.itema3converter.DateAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@SuppressWarnings("unused")
@XmlRootElement(name = "DATA_RECORD")
public class MagContenu extends Itema3File {
  private static int timezoneOffset = -1;

  @XmlElement(name = "MAG_CONTENU_ID")
  private String id;
  @XmlElement(name = "ITEM_ID")
  private String itemId;
  @XmlElement(name = "MAG_CONTENU_DATE_ENREG", required = true)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date dateEnreg;
  @XmlElement(name = "MAG_CONTENU_DUREE", required = true)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date duree;
  @XmlElement(name = "MAG_CONTENU_LIEU_ENREG")
  private String lieuEnreg;

  @Override
  public String getId() {
    return id;
  }

  public String getItemId() {
    return itemId;
  }

  public Date getDateEnreg() {
    return dateEnreg;
  }

  public Duration getDuration() {
    if (duree == null) return null;
    return Duration.ofMillis(duree.getTime() + getCurrentTimezoneOffset(duree));
  }


  public String getLieuEnreg() {
    return lieuEnreg;
  }

  public static MagContenu fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(MagContenu.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (MagContenu) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static int getCurrentTimezoneOffset(Date d) {
    if (timezoneOffset == -1) {
      TimeZone tz = TimeZone.getDefault();
      Calendar cal = GregorianCalendar.getInstance(tz);
      timezoneOffset = tz.getOffset(d.getTime());
    }
    return timezoneOffset;
  }

}
