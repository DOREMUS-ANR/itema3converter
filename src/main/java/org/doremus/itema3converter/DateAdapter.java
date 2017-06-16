package org.doremus.itema3converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Parse custom dates in the XML
// https://stackoverflow.com/a/17049927/1218213

public class DateAdapter extends XmlAdapter<String, Date> {
    private static final DateFormat f = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    @Override
    public Date unmarshal(String v) throws Exception {
        return f.parse(v);
    }

    @Override
    public String marshal(Date v) throws Exception {
        return f.format(v);
    }
}