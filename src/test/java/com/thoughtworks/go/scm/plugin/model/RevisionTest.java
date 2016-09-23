package com.thoughtworks.go.scm.plugin.model;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class RevisionTest {
    Revision revision;
    private Date date;

    @Before
    public void setUp() throws Exception {
        date = DateTime.now().toDate();
        revision = new Revision("revision", date, "comments", "user", "email");
    }

    @Test
    public void createModifiedFile() {
        revision.createModifiedFile("fileName", "added");

        assertThat(revision.getModifiedFiles(), hasSize(1));
        assertThat(revision.getModifiedFiles(), hasItem(new ModifiedFile("fileName", "added")));
    }

    @Test
    public void getRevision() {
        assertThat(revision.getRevision(), is(equalTo("revision")));
    }

    @Test
    public void getTimestamp() {
        assertThat(revision.getTimestamp(), is(date));
    }

    @Test
    public void getComment() {
        assertThat(revision.getComment(), is(equalTo("comments")));
    }

    @Test
    public void getUser() {
        assertThat(revision.getUser(), is(equalTo("user")));
    }

    @Test
    public void getEmailId() {
        assertThat(revision.getEmailId(), is(equalTo("email")));
    }
}