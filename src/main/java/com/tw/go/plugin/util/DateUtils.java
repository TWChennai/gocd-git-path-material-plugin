package com.tw.go.plugin.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();

    public static Date parseISO8601(String date) {
        try {
            DateTime dateTime = formatter.parseDateTime(date);
            return dateTime.toDate();
        } catch (Exception e) {
            //fall through and try and parse other ISO standard formats
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ").parse(date);
        } catch (ParseException e) {
            //fall through and try and parse other ISO standard formats
        }
        try {
            return dateFormatFor("yyyy-MM-dd'T'HH:mm:ss", "UTC").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatRFC822(Date date) {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ").format(date);
    }

    private static SimpleDateFormat dateFormatFor(String simpleDateFormat, String timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormat;
    }
}
