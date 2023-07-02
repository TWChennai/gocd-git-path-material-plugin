package com.tw.go.plugin.git;

import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.cmd.Console;
import com.tw.go.plugin.cmd.ConsoleResult;
import com.tw.go.plugin.cmd.InMemoryConsumer;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import com.tw.go.plugin.util.StringUtil;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GitCmdHelper extends GitHelper {
    private static final Pattern GIT_SUBMODULE_STATUS_PATTERN = Pattern.compile("^.[0-9a-fA-F]{40} (.+?)( \\(.+\\))?$");
    private static final Pattern GIT_SUBMODULE_URL_PATTERN = Pattern.compile("^submodule\\.(.+)\\.url (.+)$");
    private static final Pattern GIT_DIFF_TREE_PATTERN = Pattern.compile("^(.{1,3})\\s+(.+)$");

    public GitCmdHelper(GitConfig gitConfig, File workingDir) {
        this(gitConfig, workingDir, new ProcessOutputStreamConsumer(new InMemoryConsumer()), new ProcessOutputStreamConsumer(new InMemoryConsumer()));
    }

    public GitCmdHelper(GitConfig gitConfig, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        super(gitConfig, workingDir, stdOut, stdErr);
    }

    @Override
    public String version() {
        CommandLine gitCmd = Console.createCommand("--version");
        return runAndGetOutput(gitCmd, new File("/")).stdOut().get(0);
    }

    @Override
    public void checkConnection() {
        CommandLine gitCmd = Console.createCommand("ls-remote", gitConfig.getEffectiveUrl());
        runAndGetOutput(gitCmd);
    }

    @Override
    public void cloneRepository() {
        List<String> args = new ArrayList<>(Arrays.asList("clone", String.format("--branch=%s", gitConfig.getEffectiveBranch())));
        if (gitConfig.isNoCheckout())  {
            args.add("--no-checkout");
        }

        gitConfig.getShallowClone()
                .ifPresent(settings -> args.add("--depth=" + settings.getDefaultCommitsDepth()));

        args.add(gitConfig.getEffectiveUrl());
        args.add(workingDir.getAbsolutePath());
        CommandLine gitClone = Console.createCommand(args.toArray(new String[0]));
        runAndGetOutput(gitClone, null, stdOut, stdErr);
    }

    @Override
    public void checkoutRemoteBranchToLocal() {
        CommandLine gitCmd = Console.createCommand("checkout", "-f", gitConfig.getEffectiveBranch());
        runOrBomb(gitCmd);
    }

    @Override
    public String workingRepositoryUrl() {
        CommandLine gitConfig = Console.createCommand("config", "remote.origin.url");
        return runAndGetOutput(gitConfig).stdOut().get(0);
    }

    @Override
    public String getCurrentBranch() {
        CommandLine gitRevParse = Console.createCommand("rev-parse", "--abbrev-ref", "HEAD");
        return runAndGetOutput(gitRevParse).stdOut().get(0);
    }

    @Override
    public int getCommitCount() {
        CommandLine gitCmd = Console.createCommand("rev-list", "HEAD", "--count");
        return Integer.parseInt(runAndGetOutput(gitCmd).stdOut().get(0));
    }

    @Override
    public String currentRevision() {
        CommandLine gitLog = Console.createCommand("log", "-1", "--pretty=format:%H", "--no-decorate", "--no-color");
        return runAndGetOutput(gitLog).stdOut().stream().findFirst().orElse(null);
    }

    @Override
    public List<Revision> getAllRevisions() {
        return gitLog(logArgs());
    }

    @Override
    public Revision getLatestRevision() {
        return getLatestRevision(null);
    }

    @Override
    public Revision getLatestRevision(List<String> subPaths) {
        return gitLog(logArgs(subPaths, "-1"))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Revision> getRevisionsSince(String revision) {
        return getRevisionsSince(revision, null);
    }

    @Override
    public List<Revision> getRevisionsSince(String revision, List<String> subPaths) {
        return gitLog(logArgs(subPaths, String.format("%s..%s", revision, gitConfig.getRemoteBranch())));
    }

    private String[] logArgs(String... revisionLimits) {
        return logArgs(null, revisionLimits);
    }

    private String[] logArgs(List<String> subPaths, String... revisionLimits) {
        String[] logs = Stream.of(
                Stream.of("log", "--date=iso", "--pretty=medium", "--no-decorate", "--no-color"),
                Stream.of(revisionLimits),
                Stream.ofNullable(subPaths).flatMap(paths -> Stream.of("--")),
                Stream.ofNullable(subPaths).flatMap(paths -> subPaths.stream().map(String::trim))
        )
                .flatMap(s -> s)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        return logs;
    }

    @Override
    public Revision getDetailsForRevision(String sha) {
        return gitLog(logArgs("-1", sha))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, String> getBranchToRevisionMap(String pattern) {
        CommandLine gitCmd = Console.createCommand("show-ref");
        List<String> outputLines = runAndGetOutput(gitCmd).stdOut();
        Map<String, String> branchToRevisionMap = new HashMap<>();
        for (String line : outputLines) {
            if (line.contains(pattern)) {
                String[] parts = line.split(" ");
                String branch = parts[1].replace(pattern, "");
                String revision = parts[0];
                if (!branch.equals("HEAD")) {
                    branchToRevisionMap.put(branch, revision);
                }
            }
        }
        return branchToRevisionMap;
    }

    private List<Revision> gitLog(String... args) {
        CommandLine gitLog = Console.createCommand(args);
        List<String> gitLogOutput = runAndGetOutput(gitLog).stdOut();

        List<Revision> revisions = new GitModificationParser().parse(gitLogOutput);
        for (Revision revision : revisions) {
            addModifiedFiles(revision);
        }
        return revisions;
    }

    private void addModifiedFiles(Revision revision) {
        List<String> diffTreeOutput = diffTree(revision.getRevision()).stdOut();

        for (String resultLine : diffTreeOutput) {
            // First line is the node
            if (resultLine.equals(revision.getRevision())) {
                continue;
            }

            Matcher m = matchResultLine(resultLine);
            if (!m.find()) {
                throw new RuntimeException(String.format("Unable to parse git-diff-tree output line: %s%nFrom output:%n %s", resultLine, String.join(System.lineSeparator(), diffTreeOutput)));
            }
            revision.createModifiedFile(m.group(2), parseGitAction(m.group(1).charAt(0)));
        }
    }

    private ConsoleResult diffTree(String node) {
        CommandLine gitCmd = Console.createCommand("diff-tree", "--name-status", "--root", "-r", "-c", node);
        return runAndGetOutput(gitCmd);
    }

    private Matcher matchResultLine(String resultLine) {
        return GIT_DIFF_TREE_PATTERN.matcher(resultLine);
    }

    private String parseGitAction(char action) {
        switch (action) {
            case 'A':
                return "added";
            case 'M':
                return "modified";
            case 'D':
                return "deleted";
            default:
                return "unknown";
        }
    }

    // http://www.kernel.org/pub/software/scm/git/docs/git-log.html
    private String modificationTemplate(String separator) {
        return "%cn <%ce>%n%H%n%ai%n%n%s%n%b%n" + separator;
    }

    @Override
    public void pull() {
        CommandLine gitCommit = Console.createCommand("pull");
        runOrBomb(gitCommit);
    }

    @Override
    public void fetch(String refSpec) {
        stdOut.consumeLine("[GIT] Fetching changes");
        List<String> args = new ArrayList<>(Arrays.asList("fetch", "origin", "--prune", "--recurse-submodules=no"));
        if (!StringUtil.isEmpty(refSpec)) {
            args.add(refSpec);
        }
        runOrBomb(Console.createCommand(args.toArray(new String[0])));
    }

    private void fetchToDepth(int depth) {
        stdOut.consumeLine(String.format("[GIT] Fetching to commit depth %s", depth == Integer.MAX_VALUE ? "[INFINITE]" : depth));
        runOrBomb(Console.createCommand("fetch", "origin", "--depth=" + depth, "--recurse-submodules=no"));
    }

    @Override
    public void resetHard(String revision) {
        gitConfig.getShallowClone().ifPresent(settings -> unshallowIfNecessary(settings.getAdditionalFetchDepth(), revision));

        stdOut.consumeLine("[GIT] Updating working copy to revision " + revision);
        CommandLine gitResetHard = Console.createCommand("reset", "--hard", revision);
        runOrBomb(gitResetHard);
    }

    private void unshallowIfNecessary(int additionalFetchDepth, String revision) {
        if (branchContains(revision)) {
            return;
        }

        stdOut.consumeLine("[GIT] Working copy is shallow clone missing revision " + revision);
        fetchToDepth(additionalFetchDepth);

        if (branchContains(revision)) {
            return;
        }
        stdOut.consumeLine("[GIT] Working copy is shallow clone still missing revision " + revision + ", fetching full repo...");
        fetchToDepth(Integer.MAX_VALUE);
    }

    private boolean branchContains(String revision) {
        try {
            ConsoleResult result = runAndGetOutput(Console.createCommand("branch", "-r", "--contains", revision));
            return result.stdOut().stream().anyMatch(line -> line.contains(gitConfig.getRemoteBranch()));
        } catch (Exception ignore) {
            return false;
        }
    }

    @Override
    protected boolean shouldReset() {
        return !gitConfig.isNoCheckout();
    }

    @Override
    public void cleanAllUnversionedFiles() {
        stdOut.consumeLine("[GIT] Cleaning all unversioned files in working copy");
        if (isSubmoduleEnabled()) {
            for (Map.Entry<String, String> submoduleFolder : submoduleUrls().entrySet()) {
                cleanUnversionedFiles(new File(workingDir, submoduleFolder.getKey()));
            }
        }
        cleanUnversionedFiles(workingDir);
    }

    private void cleanUnversionedFiles(File workingDir) {
        CommandLine gitClean = Console.createCommand("clean", "-dff");
        runAndGetOutput(gitClean, workingDir, stdOut, stdErr);
    }

    @Override
    public void gc() {
        stdOut.consumeLine("[GIT] Performing git gc");
        runOrBomb(Console.createCommand("gc", "--auto"));
    }

    @Override
    public Map<String, String> submoduleUrls() {
        CommandLine gitConfig = Console.createCommand("config", "--get-regexp", "^submodule\\..+\\.url");
        List<String> submoduleList = new ArrayList<>();
        try {
            submoduleList = runAndGetOutput(gitConfig).stdOut();
        } catch (Exception e) {
            // ignore
        }
        Map<String, String> submoduleUrls = new HashMap<>();
        for (String submoduleLine : submoduleList) {
            Matcher m = GIT_SUBMODULE_URL_PATTERN.matcher(submoduleLine);
            if (!m.find()) {
                throw new RuntimeException(String.format("Unable to parse git-config output line: %s%nFrom output:%n%s", submoduleLine, String.join(System.lineSeparator(), submoduleList)));
            }
            submoduleUrls.put(m.group(1), m.group(2));
        }
        return submoduleUrls;
    }

    @Override
    public List<String> submoduleFolders() {
        CommandLine gitCmd = Console.createCommand("submodule", "status");
        return submoduleFolders(runAndGetOutput(gitCmd).stdOut());
    }

    private List<String> submoduleFolders(List<String> submoduleLines) {
        List<String> submoduleFolders = new ArrayList<>();
        for (String submoduleLine : submoduleLines) {
            Matcher m = GIT_SUBMODULE_STATUS_PATTERN.matcher(submoduleLine);
            if (!m.find()) {
                throw new RuntimeException(String.format("Unable to parse git-submodule output line: %s%nFrom output:%n%s", submoduleLine, String.join(System.lineSeparator(), submoduleLines)));
            }
            submoduleFolders.add(m.group(1));
        }
        return submoduleFolders;
    }

    @Override
    public void printSubmoduleStatus() {
        stdOut.consumeLine("[GIT] Git sub-module status");
        CommandLine gitSubModuleStatus = Console.createCommand("submodule", "status");
        runOrBomb(gitSubModuleStatus);
    }

    @Override
    public void checkoutAllModifiedFilesInSubmodules() {
        stdOut.consumeLine("[GIT] Removing modified files in submodules");
        CommandLine gitSubmoduleCheckout = Console.createCommand("submodule", "foreach", "--recursive", "git", "checkout", ".");
        runOrBomb(gitSubmoduleCheckout);
    }

    @Override
    public int getSubModuleCommitCount(String subModuleFolder) {
        CommandLine gitCmd = Console.createCommand("rev-list", "HEAD", "--count");
        return Integer.parseInt(runAndGetOutput(gitCmd, new File(workingDir, subModuleFolder)).stdOut().get(0));
    }

    @Override
    public void submoduleInit() {
        CommandLine gitSubModuleInit = Console.createCommand("submodule", "init");
        runOrBomb(gitSubModuleInit);
    }

    @Override
    public void submoduleSync() {
        CommandLine gitSubModuleSync = Console.createCommand("submodule", "sync");
        runOrBomb(gitSubModuleSync);

        CommandLine gitSubModuleForEachSync = Console.createCommand("submodule", "foreach", "--recursive", "git", "submodule", "sync");
        runOrBomb(gitSubModuleForEachSync);
    }

    @Override
    public void submoduleUpdate() {
        CommandLine gitSubModuleUpdate = Console.createCommand("submodule", "update");
        runOrBomb(gitSubModuleUpdate);
    }

    @Override
    public void init() {
        CommandLine gitCmd = Console.createCommand("init");
        runOrBomb(gitCmd);
    }

    @Override
    public void add(File fileToAdd) {
        CommandLine gitAdd = Console.createCommand("add", fileToAdd.getName());
        runOrBomb(gitAdd);
    }

    @Override
    public void commit(String message) {
        CommandLine gitCommit = Console.createCommand("commit", "-m", message);
        runOrBomb(gitCommit);
    }

    @Override
    public void submoduleAdd(String repoUrl, String submoduleNameToPutInGitSubmodules, String folder) {
        String[] addSubmoduleWithSameNameArgs = new String[]{"submodule", "add", repoUrl, folder};
        runOrBomb(Console.createCommand(addSubmoduleWithSameNameArgs));

        String[] changeSubmoduleNameInGitModules = new String[]{"config", "--file", ".gitmodules", "--rename-section", "submodule." + folder, "submodule." + submoduleNameToPutInGitSubmodules};
        runOrBomb(Console.createCommand(changeSubmoduleNameInGitModules));

        String[] addGitModules = new String[]{"add", ".gitmodules"};
        runOrBomb(Console.createCommand(addGitModules));
    }

    @Override
    public void removeSubmoduleSectionsFromGitConfig() {
        stdOut.consumeLine("[GIT] Cleaning submodule configurations in .git/config");
        for (String submoduleFolder : submoduleUrls().keySet()) {
            configRemoveSection("submodule." + submoduleFolder);
        }
    }

    @Override
    public void submoduleRemove(String folderName) {
        configRemoveSection("submodule." + folderName);

        CommandLine gitConfig = Console.createCommand("config", "-f", ".gitmodules", "--remove-section", "submodule." + folderName);
        runOrBomb(gitConfig);

        CommandLine gitRm = Console.createCommand("rm", "--cached", folderName);
        runOrBomb(gitRm);

        FileUtils.deleteQuietly(new File(workingDir, folderName));
    }

    private void configRemoveSection(String section) {
        CommandLine gitCmd = Console.createCommand("config", "--remove-section", section);
        runOrBomb(gitCmd);
    }

    @Override
    public void changeSubmoduleUrl(String submoduleName, String newUrl) {
        CommandLine gitConfig = Console.createCommand("config", "--file", ".gitmodules", "submodule." + submoduleName + ".url", newUrl);
        runOrBomb(gitConfig);
    }

    @Override
    public void push() {
        CommandLine gitCommit = Console.createCommand("push");
        runOrBomb(gitCommit);
    }

    private void runOrBomb(CommandLine gitCmd) {
        runAndGetOutput(gitCmd, workingDir, stdOut, stdErr);
    }

    private ConsoleResult runAndGetOutput(CommandLine gitCmd) {
        return runAndGetOutput(gitCmd, workingDir);
    }

    private ConsoleResult runAndGetOutput(CommandLine gitCmd, File workingDir) {
        return runAndGetOutput(gitCmd, workingDir, new ProcessOutputStreamConsumer(new InMemoryConsumer()), new ProcessOutputStreamConsumer(new InMemoryConsumer()));
    }

    private ConsoleResult runAndGetOutput(CommandLine gitCmd, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        return Console.runOrBomb(gitCmd, workingDir, stdOut, stdErr);
    }
}
