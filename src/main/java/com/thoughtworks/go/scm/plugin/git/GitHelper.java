package com.thoughtworks.go.scm.plugin.git;

import com.thoughtworks.go.scm.plugin.git.cmd.Console;
import com.thoughtworks.go.scm.plugin.git.cmd.ConsoleResult;
import com.thoughtworks.go.scm.plugin.git.cmd.InMemoryConsumer;
import com.thoughtworks.go.scm.plugin.git.cmd.ProcessOutputStreamConsumer;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class GitHelper {
    public static final String GIT_SUBMODULE_ALLOW_FILE_PROTOCOL = "toggle.git.submodule.allow.file.protocol";
    private static final Pattern GIT_SUBMODULE_STATUS_PATTERN = Pattern.compile("^.[0-9a-fA-F]{40} (.+?)( \\(.+\\))?$");
    private static final Pattern GIT_SUBMODULE_URL_PATTERN = Pattern.compile("^submodule\\.(.+)\\.url (.+)$");
    private static final Pattern GIT_DIFF_TREE_PATTERN = Pattern.compile("^(.{1,3})\\s+(.+)$");

    private final GitConfig gitConfig;
    private final File workingDir;
    private final ProcessOutputStreamConsumer stdOut;
    private final ProcessOutputStreamConsumer stdErr;


    public GitHelper(GitConfig gitConfig, File workingDir) {
        this(gitConfig, workingDir, new ProcessOutputStreamConsumer(new InMemoryConsumer()), new ProcessOutputStreamConsumer(new InMemoryConsumer()));
    }

    public GitHelper(GitConfig gitConfig, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        this.gitConfig = gitConfig;
        this.workingDir = workingDir;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public String version() {
        CommandLine gitCmd = Console.createCommand("--version");
        return runAndGetOutput(gitCmd, new File("/")).stdOut().get(0);
    }

    public void checkConnection() {
        CommandLine gitCmd = Console.createCommand("ls-remote", gitConfig.getEffectiveUrl());
        runAndGetOutput(gitCmd);
    }

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

    public void checkoutRemoteBranchToLocal() {
        CommandLine gitCmd = Console.createCommand("checkout", "-f", gitConfig.getEffectiveBranch());
        runOrBomb(gitCmd);
    }

    public String workingRepositoryUrl() {
        CommandLine gitConfig = Console.createCommand("config", "remote.origin.url");
        return runAndGetOutput(gitConfig).stdOut().get(0);
    }

    public String getCurrentBranch() {
        CommandLine gitRevParse = Console.createCommand("rev-parse", "--abbrev-ref", "HEAD");
        return runAndGetOutput(gitRevParse).stdOut().get(0);
    }

    public int getCommitCount() {
        CommandLine gitCmd = Console.createCommand("rev-list", "HEAD", "--count");
        return Integer.parseInt(runAndGetOutput(gitCmd).stdOut().get(0));
    }

    public String currentRevision() {
        CommandLine gitLog = Console.createCommand("log", "-1", "--pretty=format:%H", "--no-decorate", "--no-color");
        return runAndGetOutput(gitLog).stdOut().stream().findFirst().orElse(null);
    }

    public List<Revision> getAllRevisions() {
        return gitLog(logArgs());
    }

    public Revision getLatestRevision() {
        return getLatestRevision(null);
    }

    public Revision getLatestRevision(List<String> subPaths) {
        return gitLog(logArgs(subPaths, "-1"))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<Revision> getRevisionsSince(String revision) {
        return getRevisionsSince(revision, null);
    }

    public List<Revision> getRevisionsSince(String revision, List<String> subPaths) {
        return gitLog(logArgs(subPaths, String.format("%s..%s", revision, gitConfig.getRemoteBranch())));
    }

    private String[] logArgs(String... revisionLimits) {
        return logArgs(null, revisionLimits);
    }

    private String[] logArgs(List<String> subPaths, String... revisionLimits) {
        String[] logs = of(
                of("log", "--date=iso", "--pretty=medium", "--no-decorate", "--no-color"),
                of(revisionLimits),
                Stream.ofNullable(subPaths).flatMap(paths -> of("--")),
                Stream.ofNullable(subPaths).flatMap(paths -> subPaths.stream().map(String::trim))
        )
                .flatMap(s -> s)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        return logs;
    }

    public Revision getDetailsForRevision(String sha) {
        return gitLog(logArgs("-1", sha))
                .stream()
                .findFirst()
                .orElse(null);
    }

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

    public void pull() {
        CommandLine gitCommit = Console.createCommand("pull");
        runOrBomb(gitCommit);
    }

    public void fetch(String refSpec) {
        stdOut.consumeLine("[GIT] Fetching changes");
        List<String> args = new ArrayList<>(Arrays.asList("fetch", "origin", "--prune", "--recurse-submodules=no"));
        if (!StringUtils.isBlank(refSpec)) {
            args.add(refSpec);
        }
        runOrBomb(Console.createCommand(args.toArray(new String[0])));
    }

    private void fetchToDepth(int depth) {
        stdOut.consumeLine(String.format("[GIT] Fetching to commit depth %s", depth == Integer.MAX_VALUE ? "[INFINITE]" : depth));
        runOrBomb(Console.createCommand("fetch", "origin", "--depth=" + depth, "--recurse-submodules=no"));
    }

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

    protected boolean shouldReset() {
        return !gitConfig.isNoCheckout();
    }

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

    public void gc() {
        stdOut.consumeLine("[GIT] Performing git gc");
        runOrBomb(Console.createCommand("gc", "--auto"));
    }

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

    public void printSubmoduleStatus() {
        stdOut.consumeLine("[GIT] Git sub-module status");
        CommandLine gitSubModuleStatus = Console.createCommand("submodule", "status");
        runOrBomb(gitSubModuleStatus);
    }

    public void checkoutAllModifiedFilesInSubmodules() {
        stdOut.consumeLine("[GIT] Removing modified files in submodules");
        CommandLine gitSubmoduleCheckout = Console.createCommand("submodule", "foreach", "--recursive", "git", "checkout", ".");
        runOrBomb(gitSubmoduleCheckout);
    }

    public int getSubModuleCommitCount(String subModuleFolder) {
        CommandLine gitCmd = Console.createCommand("rev-list", "HEAD", "--count");
        return Integer.parseInt(runAndGetOutput(gitCmd, new File(workingDir, subModuleFolder)).stdOut().get(0));
    }

    public void submoduleInit() {
        CommandLine gitSubModuleInit = Console.createCommand("submodule", "init");
        runOrBomb(gitSubModuleInit);
    }

    public void submoduleSync() {
        CommandLine gitSubModuleSync = Console.createCommand("submodule", "sync");
        runOrBomb(gitSubModuleSync);

        CommandLine gitSubModuleForEachSync = Console.createCommand("submodule", "foreach", "--recursive", "git", "submodule", "sync");
        runOrBomb(gitSubModuleForEachSync);
    }

    public void submoduleUpdate() {
        CommandLine gitSubModuleUpdate = Console.createCommand(concat(gitSubmoduleConfigArgs().stream(), of("submodule", "update")).toArray(String[]::new));
        runOrBomb(gitSubModuleUpdate);
    }

    public void init() {
        CommandLine gitCmd = Console.createCommand("init");
        runOrBomb(gitCmd);
    }

    public void add(File fileToAdd) {
        CommandLine gitAdd = Console.createCommand("add", fileToAdd.getName());
        runOrBomb(gitAdd);
    }

    public void commit(String message) {
        CommandLine gitCommit = Console.createCommand("commit", "-m", message);
        runOrBomb(gitCommit);
    }

    public void submoduleAdd(String repoUrl, String submoduleNameToPutInGitSubmodules, String folder) {

        String[] addSubmoduleWithSameNameArgs = concat(gitSubmoduleConfigArgs().stream(), of("submodule", "add", repoUrl, folder)).toArray(String[]::new);
        runOrBomb(Console.createCommand(addSubmoduleWithSameNameArgs));

        String[] changeSubmoduleNameInGitModules = new String[]{"config", "--file", ".gitmodules", "--rename-section", "submodule." + folder, "submodule." + submoduleNameToPutInGitSubmodules};
        runOrBomb(Console.createCommand(changeSubmoduleNameInGitModules));

        String[] addGitModules = new String[]{"add", ".gitmodules"};
        runOrBomb(Console.createCommand(addGitModules));
    }

    private List<String> gitSubmoduleConfigArgs() {
        if ("Y".equalsIgnoreCase(System.getProperty(GIT_SUBMODULE_ALLOW_FILE_PROTOCOL))) {
            return List.of("-c", "protocol.file.allow=always");
        } else {
            return Collections.emptyList();
        }
    }

    public void removeSubmoduleSectionsFromGitConfig() {
        stdOut.consumeLine("[GIT] Cleaning submodule configurations in .git/config");
        for (String submoduleFolder : submoduleUrls().keySet()) {
            configRemoveSection("submodule." + submoduleFolder);
        }
    }

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

    public void changeSubmoduleUrl(String submoduleName, String newUrl) {
        CommandLine gitConfig = Console.createCommand("config", "--file", ".gitmodules", "submodule." + submoduleName + ".url", newUrl);
        runOrBomb(gitConfig);
    }

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
        return Console.runOrBomb(gitCmd, workingDir, stdOut, stdErr, gitConfig == null ? List.of() : gitConfig.redactables());
    }

    public void cloneOrFetch() {
        cloneOrFetch(null);
    }

    public void cloneOrFetch(String refSpec) {
        if (!isGitRepository() || !isSameRepository()) {
            setupWorkingDir();
            cloneRepository();
        }

        fetchAndResetToHead(refSpec);
    }

    private boolean isGitRepository() {
        File dotGit = new File(workingDir, ".git");
        return workingDir.exists() && dotGit.exists() && dotGit.isDirectory();
    }

    public boolean isSameRepository() {
        try {
            return workingRepositoryUrl().equals(gitConfig.getEffectiveUrl());
        } catch (Exception e) {
            return false;
        }
    }

    private void setupWorkingDir() {
        FileUtils.deleteQuietly(workingDir);
        try {
            FileUtils.forceMkdir(workingDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory: " + workingDir.getAbsolutePath());
        }
    }

    public Map<String, String> getBranchToRevisionMap() {
        return getBranchToRevisionMap("refs/remotes/origin/");
    }

    public void fetchAndResetToHead(String refSpec) {
        fetchAndReset(refSpec, gitConfig.getRemoteBranch());
    }

    public void fetchAndReset(String refSpec, String revision) {
        fetch(refSpec);
        gc();

        if (shouldReset()) {
            stdOut.consumeLine(String.format("[GIT] Reset working directory %s", workingDir));
            cleanAllUnversionedFiles();
            if (isSubmoduleEnabled()) {
                removeSubmoduleSectionsFromGitConfig();
            }
            resetHard(revision);
            if (isSubmoduleEnabled()) {
                checkoutAllModifiedFilesInSubmodules();
                updateSubmoduleWithInit();
            }
            cleanAllUnversionedFiles();
        }
    }

    public boolean isSubmoduleEnabled() {
        return new File(workingDir, ".gitmodules").exists();
    }

    public void updateSubmoduleWithInit() {
        stdOut.consumeLine("[GIT] Updating git sub-modules");

        submoduleInit();

        submoduleSync();

        submoduleUpdate();

        stdOut.consumeLine("[GIT] Cleaning unversioned files and sub-modules");
        printSubmoduleStatus();
    }
}
