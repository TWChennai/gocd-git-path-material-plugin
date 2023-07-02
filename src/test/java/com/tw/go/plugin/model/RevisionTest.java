package com.tw.go.plugin.model;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class RevisionTest {
    private Revision revision;
    private Date date;

    @BeforeEach
    public void setUp() {
        date = DateTime.now().toDate();
        revision = new Revision("revision", date, "comments", "user", "email", null);
    }

    @Test
    public void createModifiedFile() {
        revision.createModifiedFile("fileName", "added");

        assertThat(revision.getModifiedFiles()).hasSize(1);
        assertThat(revision.getModifiedFiles()).contains(new ModifiedFile("fileName", "added"));
    }

    @Test
    public void getRevision() {
        assertThat(revision.getRevision()).isEqualTo("revision");
    }

    @Test
    public void getTimestamp() {
        assertThat(revision.getTimestamp()).isEqualTo(date);
    }

    @Test
    public void getComment() {
        assertThat(revision.getComment()).isEqualTo("comments");
    }

    @Test
    public void getUser() {
        assertThat(revision.getUser()).isEqualTo("user");
    }

    @Test
    public void getEmailId() {
        assertThat(revision.getEmailId()).isEqualTo("email");
    }
}