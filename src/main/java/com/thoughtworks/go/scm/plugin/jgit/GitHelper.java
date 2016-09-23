package com.thoughtworks.go.scm.plugin.jgit;

import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.Revision;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class GitHelper {

    protected GitConfig gitConfig;
    protected File workingDir;

    public GitHelper(GitConfig gitConfig, File workingDir) {
        this.gitConfig = gitConfig;
        this.workingDir = workingDir;
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
        return workingRepositoryUrl().equals(gitConfig.getEffectiveUrl());
    }

    private void setupWorkingDir() {
        FileUtils.deleteQuietly(workingDir);
        try {
            FileUtils.forceMkdir(workingDir);
        } catch (IOException e) {
            new RuntimeException("Could not create directory: " + workingDir.getAbsolutePath());
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

    public abstract List<Revision> getRevisionsSince(String revision);

    public abstract Revision getDetailsForRevision(String sha);

    public Map<String, String> getBranchToRevisionMap() {
        return getBranchToRevisionMap("refs/remotes/origin/");
    }

    public abstract Map<String, String> getBranchToRevisionMap(String pattern);

    public abstract void pull();

    public abstract void fetch(String refSpec);

    public abstract void resetHard(String revision);

    public void fetchAndResetToHead(String refSpec) {
        fetchAndReset("origin/" + gitConfig.getEffectiveBranch(), refSpec);
    }

    public void fetchAndReset(String revision, String refSpec) {
        cleanAllUnversionedFiles();
        if (isSubmoduleEnabled()) {
            removeSubmoduleSectionsFromGitConfig();
        }
        fetch(refSpec);
        gc();
        resetHard(revision);
        if (isSubmoduleEnabled()) {
            checkoutAllModifiedFilesInSubmodules();
            updateSubmoduleWithInit();
        }
        cleanAllUnversionedFiles();
    }

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
        submoduleInit();

        submoduleSync();

        submoduleUpdate();

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
