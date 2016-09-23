package com.thoughtworks.go.scm.plugin.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DateUtilsTest {
    @Test
    public void parseISO8601() {
        Date date = DateUtils.parseISO8601("1994-11-05T08:15:30-05:00");

        assertThat(date.toString(), is(equalTo("Sat Nov 05 18:45:30 IST 1994")));
    }

    @Test
    public void formatRFC822() {
        Date date = new DateTime(2016, 9, 22, 22, 16, 45, DateTimeZone.UTC).toDate();

        String formatRFC822 = DateUtils.formatRFC822(date);

        assertThat(formatRFC822, is(equalTo("Fri, 23 Sep 2016 03:46:45 +0530")));
    }

}