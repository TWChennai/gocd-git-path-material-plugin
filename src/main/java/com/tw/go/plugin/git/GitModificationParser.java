package com.tw.go.plugin.git;

import com.tw.go.plugin.model.Revision;
import com.tw.go.plugin.util.DateUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitModificationParser {
    private static final String SPACES = "\\s+";
    private static final String COMMENT_INDENT = "\\s{4}";
    private static final String COMMENT_TEXT = "(.*)";
    private static final String HASH = "(\\w+)";
    private static final String DATE = "(.+)";
    private static final String AUTHOR = "(.+)";
    private static final String MULTIPLE_HASHES = "(.+)";
    private static final Pattern COMMIT_PATTERN = Pattern.compile("^commit" + SPACES + HASH + "$");
    private static final Pattern MERGE_PATTERN = Pattern.compile("^Merge:" + SPACES + MULTIPLE_HASHES + "$");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^Author:" + SPACES + AUTHOR + "$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^Date:" + SPACES + DATE + "$");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^" + COMMENT_INDENT + COMMENT_TEXT + "$");

    private final LinkedList<Revision> revisions = new LinkedList<>();

    public List<Revision> parse(List<String> output) {
        for (String line : output) {
            processLine(line);
        }
        return revisions;
    }

    public void processLine(String line) {
        Matcher matcher = COMMIT_PATTERN.matcher(line);
        if (matcher.matches()) {
            revisions.add(new Revision(matcher.group(1), null, "", "", null, null));
        }
        Matcher mergeMatcher = MERGE_PATTERN.matcher(line);
        if(mergeMatcher.matches()) {
            revisions.getLast().setMergeCommit(true);
        }
        Matcher authorMatcher = AUTHOR_PATTERN.matcher(line);
        if (authorMatcher.matches()) {
            revisions.getLast().setUser(authorMatcher.group(1));
        }
        Matcher dateMatcher = DATE_PATTERN.matcher(line);
        if (dateMatcher.matches()) {
            revisions.getLast().setTimestamp(DateUtils.parseISO8601(dateMatcher.group(1)));
        }
        Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
        if (commentMatcher.matches()) {
            Revision last = revisions.getLast();
            String comment = last.getComment();
            if (!comment.isEmpty()) comment += "\n";
            last.setComment(comment + commentMatcher.group(1));
        }
    }
}
