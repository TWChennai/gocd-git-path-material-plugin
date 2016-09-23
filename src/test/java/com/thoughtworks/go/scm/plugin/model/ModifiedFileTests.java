package com.thoughtworks.go.scm.plugin.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModifiedFileTests {

    @Test
    public void getFileName() {
        ModifiedFile modifiedFile = new ModifiedFile("file-name", "added");

        assertThat(modifiedFile.getFileName(), is(equalTo("file-name")));
    }

    @Test
    public void getAction() {
        ModifiedFile modifiedFile = new ModifiedFile("file-name", "added");

        assertThat(modifiedFile.getAction(), is(equalTo("added")));
    }

    @Test
    public void toStringShouldReturnFileNameAlongWithAction() {
        ModifiedFile modifiedFile = new ModifiedFile("filename", "added");

        assertThat(modifiedFile.toString(), is(equalTo("ModifiedFile{fileName='filename', action='added'}")));
    }
}
