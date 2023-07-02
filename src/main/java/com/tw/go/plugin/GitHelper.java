package com.tw.go.plugin;

import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class GitHelper {
    protected final GitConfig gitConfig;
    protected final File workingDir;
    protected final ProcessOutputStreamConsumer stdOut;
    protected final ProcessOutputStreamConsumer stdErr;

    public GitHelper(GitConfig gitConfig, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        this.gitConfig = gitConfig;
        this.workingDir = workingDir;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public abstract String version();

    public abstract void checkConnection();

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

    public abstract void cloneRepository();

    public abstract void checkoutRemoteBranchToLocal();

    public abstract String workingRepositoryUrl();

    public abstract String getCurrentBranch();

    public abstract int getCommitCount();

    public abstract String currentRevision();

    public abstract List<Revision> getAllRevisions();

    public abstract Revision getLatestRevision();

    public abstract Revision getLatestRevision(List<String> subPaths);

    public abstract List<Revision> getRevisionsSince(String revision);

    public abstract List<Revision> getRevisionsSince(String revision, List<String> subPaths);

    public abstract Revision getDetailsForRevision(String sha);

    public Map<String, String> getBranchToRevisionMap() {
        return getBranchToRevisionMap("refs/remotes/origin/");
    }

    public abstract Map<String, String> getBranchToRevisionMap(String pattern);

    public abstract void pull();

    public abstract void fetch(String refSpec);

    public abstract void resetHard(String revision);

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

    protected abstract boolean shouldReset();

    public abstract void cleanAllUnversionedFiles();

    public abstract void gc();

    public boolean isSubmoduleEnabled() {
        return new File(workingDir, ".gitmodules").exists();
    }

    public abstract Map<String, String> submoduleUrls();

    public abstract List<String> submoduleFolders();

    public abstract void printSubmoduleStatus();

    public abstract void checkoutAllModifiedFilesInSubmodules();

    public abstract int getSubModuleCommitCount(String subModuleFolder);

    public void updateSubmoduleWithInit() {
        stdOut.consumeLine("[GIT] Updating git sub-modules");

        submoduleInit();

        submoduleSync();

        submoduleUpdate();

        stdOut.consumeLine("[GIT] Cleaning unversioned files and sub-modules");
        printSubmoduleStatus();
    }

    public abstract void submoduleInit();

    public abstract void submoduleSync();

    public abstract void submoduleUpdate();

    public abstract void init();

    public abstract void add(File fileToAdd);

    public abstract void commit(String message);

    public abstract void commitOnDate(String message, Date commitDate);

    public abstract void submoduleAdd(String repoUrl, String submoduleNameToPutInGitSubmodules, String folder);

    public abstract void removeSubmoduleSectionsFromGitConfig();

    public abstract void submoduleRemove(String folderName);

    public abstract void changeSubmoduleUrl(String submoduleName, String newUrl);

    public abstract void push();
}
