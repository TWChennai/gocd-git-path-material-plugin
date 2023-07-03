package com.thoughtworks.go.scm.plugin.git;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Revision {
    private String revision;
    private Date timestamp;
    private String comment;
    private String user;
    private String emailId;
    private List<ModifiedFile> modifiedFiles;
    private boolean isMergeCommit;

    public Revision(String revision) {
        this.revision = revision;
        this.isMergeCommit = false;
    }

    public Revision(String revision, Date timestamp, String comment, String user, String emailId, List<ModifiedFile> modifiedFiles) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.comment = comment;
        this.user = user;
        this.emailId = emailId;
        this.modifiedFiles = modifiedFiles;
        this.isMergeCommit = false;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<ModifiedFile> getModifiedFiles() {
        return modifiedFiles;
    }

    public void setModifiedFiles(List<ModifiedFile> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    public final ModifiedFile createModifiedFile(String filename, String action) {
        ModifiedFile file = new ModifiedFile(filename, action);
        if (modifiedFiles == null) {
            modifiedFiles = new ArrayList<>();
        }
        modifiedFiles.add(file);
        return file;
    }

    public boolean isMergeCommit() {
        return isMergeCommit;
    }

    public void setMergeCommit(boolean mergeCommit) {
        isMergeCommit = mergeCommit;
    }
}
