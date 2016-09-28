package com.thoughtworks.go.scm.plugin.util;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DateUtilsTest {
    @Test
    public void parseISO8601() {
        Date actual = DateUtils.parseISO8601("1994-11-05T08:15:30-05:00");
        GregorianCalendar gregorianCalendar = new GregorianCalendar(1994, 10, 5, 13, 15, 30);
        gregorianCalendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        Date expected = gregorianCalendar.getTime();
        assertThat(actual, is(equalTo(expected)));
    }
}