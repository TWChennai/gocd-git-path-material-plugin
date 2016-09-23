package com.thoughtworks.go.scm.plugin.model;

import com.thoughtworks.go.scm.plugin.util.ListUtils;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class Revision {
    private String revision;
    private Date timestamp;
    private String comment;
    private String user;
    private String emailId;
    private List<ModifiedFile> modifiedFiles;

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Revision(String revision, Date timestamp, String comment, String user, String emailId, List<ModifiedFile> modifiedFiles) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.comment = comment;
        this.user = user;
        this.emailId = emailId;
        this.modifiedFiles = modifiedFiles;
    }

    public Revision(String revision, Date date, String comments, String user, String email) {
        this(revision, date, comments, user, email, new ArrayList<ModifiedFile>());
    }

    public final ModifiedFile createModifiedFile(String filename, String action) {
        ModifiedFile file = new ModifiedFile(filename, action);
        modifiedFiles.add(file);
        return file;
    }

    public Map<String, Object> getRevisionMap() {
        Map<String, Object> response = new HashMap<>();
        response.put("revision", this.getRevision());
        response.put("timestamp", new SimpleDateFormat(DATE_PATTERN).format(this.getTimestamp()));
        response.put("user", this.getUser());
        response.put("revisionComment", this.getComment());
        List<Map> modifiedFilesMapList = new ArrayList<>();

        if (!ListUtils.isEmpty(this.getModifiedFiles())) {
            for (ModifiedFile modifiedFile : this.getModifiedFiles()) {
                Map<String, String> modifiedFileMap = new HashMap<>();
                modifiedFileMap.put("fileName", modifiedFile.getFileName());
                modifiedFileMap.put("action", modifiedFile.getAction());
                modifiedFilesMapList.add(modifiedFileMap);
            }
        }
        response.put("modifiedFiles", modifiedFilesMapList);
        return response;
    }
}
