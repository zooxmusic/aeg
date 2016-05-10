package com.aeg.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by bszucs on 5/4/2016.
 */
public class JodaUtil {

    public static String formatDateTime(DateTime at) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
        return fmt.print(at);

        /*// Format for input
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
        DateTime jodatime = dtf.parseDateTime(at);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
        return dtfOut.toString();*/
    }
}
