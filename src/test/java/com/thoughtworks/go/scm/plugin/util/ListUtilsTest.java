package com.thoughtworks.go.scm.plugin.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.*;

public class ListUtilsTest {

    private ArrayList<String> collection;

    @Before
    public void setUp() throws Exception {
        collection = new ArrayList<String>() {{
            add("Tom");
            add("Jerry");
            add("Best");
            add("Cartoon");
            add("Show");
        }};
    }

    @Test
    public void isEmpty() {
        assertThat(ListUtils.isEmpty(null), is(equalTo(true)));
        assertThat(ListUtils.isEmpty(new ArrayList()), is(equalTo(true)));
        assertThat(ListUtils.isEmpty(collection), is(equalTo(false)));
    }

    @Test
    public void join() {
        String message = ListUtils.join(collection, ",");

        assertThat(message, is(equalTo("Tom,Jerry,Best,Cartoon,Show")));
    }

    @Test
    public void joinWithCommaAndSpace() {
        String message = ListUtils.join(collection);

        assertThat(message, is(equalTo("Tom, Jerry, Best, Cartoon, Show")));
    }

    @Test
    public void toArray() {
        String[] items = ListUtils.toArray(collection);

        assertThat(items, arrayWithSize(5));
        assertThat(items, arrayContaining("Tom", "Jerry", "Best", "Cartoon", "Show"));
    }
}