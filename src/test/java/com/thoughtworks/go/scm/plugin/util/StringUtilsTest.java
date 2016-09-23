package com.thoughtworks.go.scm.plugin.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class StringUtilsTest {
    @Test
    public void isEmpty() {
        assertThat(StringUtils.isEmpty(""), is(equalTo(true)));
        assertThat(StringUtils.isEmpty(null), is(equalTo(true)));
        assertThat(StringUtils.isEmpty("Not empty"), is(equalTo(false)));
    }

}