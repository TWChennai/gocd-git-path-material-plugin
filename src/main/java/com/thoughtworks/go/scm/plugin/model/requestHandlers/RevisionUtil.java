package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.tw.go.plugin.model.Revision;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class RevisionUtil {
    private static final DateTimeFormatter ISO8601_FORMAT = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

    private RevisionUtil() {}

    public static Map<String, Object> toMap(Revision revision) {
        return Map.of(
                "revision", revision.getRevision(),
                "timestamp", ISO8601_FORMAT.format(revision.getTimestamp().toInstant()),
                "user", revision.getUser(),
                "revisionComment", revision.getComment(),
                "modifiedFiles",
                Optional.ofNullable(revision.getModifiedFiles())
                        .map(files -> files
                                .stream()
                                .map(file -> Map.of(
                                        "fileName", file.getFileName(),
                                        "action", file.getAction()))
                                .collect(Collectors.toList()))
                        .orElseGet(Collections::emptyList));
    }
}
