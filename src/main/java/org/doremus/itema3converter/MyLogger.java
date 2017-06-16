package org.doremus.itema3converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class MyLogger extends Logger {
    protected MyLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);

    }

    public static Logger getLogger(String name) {
        Logger l = Logger.getLogger(name);
        l.setUseParentHandlers(false);
        if (l.getHandlers().length == 0) {
            MyFormatter formatter = new MyFormatter();
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(formatter);

            l.addHandler(handler);
        }
        return l;
    }

}

class MyFormatter extends Formatter {

    // Create a DateFormat to format the logger timestamp.
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    private boolean debug = false;

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        if (debug) {
            builder.append(df.format(new Date(record.getMillis()))).append(" - ");
            builder.append("[").append(record.getSourceClassName()).append(".");
            builder.append(record.getSourceMethodName()).append("] - ");
            builder.append("[").append(record.getLevel()).append("] - ");
        }
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
