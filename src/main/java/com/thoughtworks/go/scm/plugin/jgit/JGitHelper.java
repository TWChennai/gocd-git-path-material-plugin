package com.thoughtworks.go.scm.plugin.jgit;

import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.ModifiedFile;
import com.thoughtworks.go.scm.plugin.model.Revision;
import com.thoughtworks.go.scm.plugin.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class JGitHelper extends GitHelper {

    public JGitHelper(GitConfig gitConfig, File workingDir) {
        super(gitConfig, workingDir);
    }

    public static JGitHelper create(GitConfig gitConfig, String destinationFolder) {
        File workingDirectory = destinationFolder == null ? null : new File(destinationFolder);
        return new JGitHelper(gitConfig, workingDirectory);
    }

    @Override
    public String version() {
        return "3.6.2.201501210735-r";
    }

    @Override
    public void checkConnection() {
        try {
            LsRemoteCommand lsRemote = Git.lsRemoteRepository().setHeads(true).setRemote(gitConfig.getUrl());
            setCredentials(lsRemote);
            lsRemote.call();
        } catch (Exception e) {
            throw new RuntimeException("check connection (ls-remote) failed", e);
        }
    }

    @Override
    public void cloneRepository() {
        CloneCommand clone = Git.cloneRepository().setURI(gitConfig.getUrl()).setDirectory(workingDir).setBranch(gitConfig.getEffectiveBranch());
        if (gitConfig.isRecursiveSubModuleUpdate()) {
            clone.setCloneSubmodules(true);
        }
        setCredentials(clone);
        try {
            clone.call();
        } catch (Exception e) {
            throw new RuntimeException("clone failed", e);
        }
    }

    @Override
    public void checkoutRemoteBranchToLocal() {
    }

    @Override
    public String workingRepositoryUrl() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            return repository.getConfig().getString("remote", "origin", "url");
        } catch (Exception e) {
            throw new RuntimeException("clean failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public String getCurrentBranch() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            return repository.getBranch();
        } catch (Exception e) {
            throw new RuntimeException("current branch failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public int getCommitCount() {
        Repository repository = null;
        int count = 0;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            LogCommand logCmd = git.log();
            Iterable<RevCommit> log = logCmd.call();
            for (RevCommit commit : log) {
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("commit count failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
        return count;
    }

    @Override
    public String currentRevision() {
        return getLatestRevision().getRevision();
    }

    @Override
    public List<Revision> getAllRevisions() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            LogCommand logCmd = git.log();
            Iterable<RevCommit> log = logCmd.call();
            List<Revision> revisionObjs = new ArrayList<Revision>();
            for (RevCommit commit : log) {
                Revision revisionObj = getRevisionObj(repository, commit);
                revisionObjs.add(revisionObj);
            }
            return revisionObjs;
        } catch (Exception e) {
            throw new RuntimeException("get all revisions failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public Revision getLatestRevision() {
        return getLatestRevision(null);
    }

    @Override
    public Revision getLatestRevision(String subDirectoryPath) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            LogCommand logCmd = git.log().setMaxCount(1);
            if (subDirectoryPath != null) logCmd.addPath(subDirectoryPath);
            Iterable<RevCommit> log = logCmd.call();
            Iterator<RevCommit> iterator = log.iterator();
            if (iterator.hasNext()) {
                return getRevisionObj(repository, iterator.next());
            }
        } catch (Exception e) {
            throw new RuntimeException("get latest revision failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
        return null;
    }

    @Override
    public List<Revision> getRevisionsSince(String previousRevision) {
        return getRevisionsSince(previousRevision, null);
    }

    @Override
    public List<Revision> getRevisionsSince(String previousRevision, String subDirectoryPath) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            LogCommand logCmd = git.log();
            if (subDirectoryPath != null) logCmd.addPath(subDirectoryPath);
            Iterable<RevCommit> log = logCmd.call();
            List<RevCommit> newCommits = new ArrayList<RevCommit>();
            for (RevCommit commit : log) {
                if (commit.getName().equals(previousRevision)) {
                    break;
                }
                newCommits.add(commit);
            }

            List<Revision> revisionObjs = new ArrayList<Revision>();
            if (!newCommits.isEmpty()) {
                for (RevCommit newCommit : newCommits) {
                    Revision revisionObj = getRevisionObj(repository, newCommit);
                    revisionObjs.add(revisionObj);
                }
            }
            return revisionObjs;
        } catch (Exception e) {
            throw new RuntimeException("get newer revisions failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public Revision getDetailsForRevision(String sha) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            LogCommand logCmd = git.log().all();
            Iterable<RevCommit> log = logCmd.call();
            for (RevCommit commit : log) {
                if (commit.getName().equals(sha)) {
                    return getRevisionObj(repository, commit);
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("get latest revision failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public Map<String, String> getBranchToRevisionMap(String pattern) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Map<String, Ref> allRefs = repository.getAllRefs();
            Map<String, String> branchToRevisionMap = new HashMap<String, String>();
            for (String refName : allRefs.keySet()) {
                if (refName.contains(pattern)) {
                    String branch = refName.replace(pattern, "");
                    String revision = allRefs.get(refName).getObjectId().getName();
                    branchToRevisionMap.put(branch, revision);
                }
            }
            return branchToRevisionMap;
        } catch (Exception e) {
            throw new RuntimeException("fetch failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void pull() {
    }

    @Override
    public void fetch(String refSpec) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            FetchCommand fetch = git.fetch().setRemoveDeletedRefs(true);
            if (!StringUtils.isEmpty(refSpec)) {
                fetch.setRefSpecs(new RefSpec(refSpec));
            }
            setCredentials(fetch);
            fetch.call();
        } catch (Exception e) {
            throw new RuntimeException("fetch failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void resetHard(String revision) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            ResetCommand reset = git.reset().setMode(ResetCommand.ResetType.HARD).setRef(revision);
            reset.call();
        } catch (Exception e) {
            throw new RuntimeException("reset failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void cleanAllUnversionedFiles() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);

            SubmoduleWalk walk = SubmoduleWalk.forIndex(repository);
            while (walk.next()) {
                cleanSubmoduleOfAllUnversionedFiles(walk);
            }

            CleanCommand clean = git.clean().setCleanDirectories(true);
            clean.call();
        } catch (Exception e) {
            throw new RuntimeException("clean failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    private void cleanSubmoduleOfAllUnversionedFiles(SubmoduleWalk walk) {
        Repository submoduleRepository = null;
        try {
            submoduleRepository = walk.getRepository();
            CleanCommand clean = Git.wrap(submoduleRepository).clean().setCleanDirectories(true);
            clean.call();
        } catch (Exception e) {
            throw new RuntimeException("sub-module clean failed", e);
        } finally {
            if (submoduleRepository != null) {
                submoduleRepository.close();
            }
        }
    }

    @Override
    public void gc() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            GarbageCollectCommand gc = git.gc();
            gc.call();
        } catch (Exception e) {
            throw new RuntimeException("gc failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public Map<String, String> submoduleUrls() {
        return null;
    }

    @Override
    public List<String> submoduleFolders() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            SubmoduleStatusCommand submoduleStatus = git.submoduleStatus();
            Map<String, SubmoduleStatus> submoduleStatusMap = submoduleStatus.call();
            List<String> submoduleFolders = new ArrayList<String>();
            for (String submoduleFolder : submoduleStatusMap.keySet()) {
                submoduleFolders.add(submoduleFolder);
            }
            return submoduleFolders;
        } catch (Exception e) {
            throw new RuntimeException("sub-module folders list failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void printSubmoduleStatus() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            SubmoduleStatusCommand submoduleStatus = git.submoduleStatus();
            Map<String, SubmoduleStatus> submoduleStatusMap = submoduleStatus.call();
            for (String submoduleFolder : submoduleStatusMap.keySet()) {
                // print
            }
        } catch (Exception e) {
            throw new RuntimeException("sub-module print status failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void checkoutAllModifiedFilesInSubmodules() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);

            SubmoduleWalk walk = SubmoduleWalk.forIndex(repository);
            while (walk.next()) {
                checkoutSubmodule(walk);
            }
        } catch (Exception e) {
            throw new RuntimeException("checkout all sub-modules failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    private void checkoutSubmodule(SubmoduleWalk walk) {
        Repository submoduleRepository = null;
        try {
            submoduleRepository = walk.getRepository();
            CheckoutCommand checkout = Git.wrap(submoduleRepository).checkout();
            checkout.call();
        } catch (Exception e) {
            throw new RuntimeException("sub-module checkout failed", e);
        } finally {
            if (submoduleRepository != null) {
                submoduleRepository.close();
            }
        }
    }

    @Override
    public int getSubModuleCommitCount(String subModuleFolder) {
        Repository repository = null;
        Repository subModuleRepository = null;
        int count = 0;
        try {
            repository = getRepository(workingDir);
            subModuleRepository = SubmoduleWalk.getSubmoduleRepository(repository, subModuleFolder);
            Git git = new Git(subModuleRepository);
            Iterable<RevCommit> log = git.log().call();
            for (RevCommit commit : log) {
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("sub-module commit count failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
            if (subModuleRepository != null) {
                subModuleRepository.close();
            }
        }
        return count;
    }

    @Override
    public void submoduleInit() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            git.submoduleInit().call();
        } catch (Exception e) {
            throw new RuntimeException("sub-module init failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void submoduleSync() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            git.submoduleSync().call();
        } catch (Exception e) {
            throw new RuntimeException("sub-module sync failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void submoduleUpdate() {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            git.submoduleUpdate().call();
        } catch (Exception e) {
            throw new RuntimeException("sub-module update failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void init() {
        Repository repository = null;
        try {
            Git.init().setDirectory(workingDir).call();

            repository = FileRepositoryBuilder.create(new File(workingDir.getAbsolutePath(), ".git"));
        } catch (Exception e) {
            throw new RuntimeException("init failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void add(File fileToAdd) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            AddCommand add = git.add().addFilepattern(fileToAdd.getName());
            add.call();
        } catch (Exception e) {
            throw new RuntimeException("add failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void commit(String message) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            Git git = new Git(repository);
            CommitCommand commit = git.commit().setAuthor("author", "author@nodomain.com").setMessage(message);
            commit.call();
        } catch (Exception e) {
            throw new RuntimeException("commit failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void commitOnDate(String message, Date commitDate) {
    }

    @Override
    public void submoduleAdd(String subModuleFolder, String subModuleName, String relativePath) {
        Repository parentRepository = null;
        Repository subModuleRepository = null;
        try {
            parentRepository = getRepository(workingDir);
            subModuleRepository = getRepository(new File(subModuleFolder));
            Git git = new Git(parentRepository);
            SubmoduleAddCommand subModuleAdd = git.submoduleAdd().setURI(subModuleRepository.getDirectory().getCanonicalPath()).setPath(relativePath);
            subModuleAdd.call();
        } catch (Exception e) {
            throw new RuntimeException("add sub-module failed", e);
        } finally {
            if (parentRepository != null) {
                parentRepository.close();
            }
            if (subModuleRepository != null) {
                subModuleRepository.close();
            }
        }
    }

    @Override
    public void removeSubmoduleSectionsFromGitConfig() {
        List<String> submoduleFolders = submoduleFolders();

        for (String submoduleFolder : submoduleFolders) {
            configRemoveSection(submoduleFolder);
        }
    }

    @Override
    public void submoduleRemove(String folderName) {
        configRemoveSection(folderName);

        Repository repository = null;
        try {
            repository = getRepository(workingDir);

            StoredConfig gitSubmodulesConfig = new FileBasedConfig(null, new File(repository.getWorkTree(), Constants.DOT_GIT_MODULES), FS.DETECTED);
            gitSubmodulesConfig.unsetSection(ConfigConstants.CONFIG_SUBMODULE_SECTION, folderName);
            gitSubmodulesConfig.save();

            Git git = Git.wrap(repository);
            git.rm().setCached(true).addFilepattern(folderName).call();

            FileUtils.deleteQuietly(new File(workingDir, folderName));
        } catch (Exception e) {
            throw new RuntimeException("sub-module remove failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    private void configRemoveSection(String folderName) {
        Repository repository = null;
        try {
            repository = getRepository(workingDir);
            StoredConfig repositoryConfig = repository.getConfig();
            repositoryConfig.unsetSection(ConfigConstants.CONFIG_SUBMODULE_SECTION, folderName);
            repositoryConfig.save();
        } catch (Exception e) {
            throw new RuntimeException("sub-module section remove failed", e);
        } finally {
            if (repository != null) {
                repository.close();
            }
        }
    }

    @Override
    public void changeSubmoduleUrl(String submoduleName, String newUrl) {
    }

    @Override
    public void push() {
    }

    private Revision getRevisionObj(Repository repository, RevCommit commit) throws IOException {
        String commitSHA = commit.getName();
        Date commitTime = commit.getAuthorIdent().getWhen();
        String comment = commit.getFullMessage().trim();
        String user = commit.getAuthorIdent().getName();
        String emailId = commit.getAuthorIdent().getEmailAddress();
        List<ModifiedFile> modifiedFiles = new ArrayList<ModifiedFile>();
        if (commit.getParentCount() == 0) {
            TreeWalk treeWalk = new TreeWalk(repository);
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(false);
            while (treeWalk.next()) {
                modifiedFiles.add(new ModifiedFile(treeWalk.getPathString(), "added"));
            }
        } else {
            RevWalk rw = new RevWalk(repository);
            RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);
            List<DiffEntry> diffEntries = diffFormatter.scan(parent.getTree(), commit.getTree());
            for (DiffEntry diffEntry : diffEntries) {
                modifiedFiles.add(new ModifiedFile(diffEntry.getNewPath(), getAction(diffEntry.getChangeType().name())));
            }
        }

        return new Revision(commitSHA, commitTime, comment, user, emailId, modifiedFiles);
    }

    private String getAction(String gitAction) {
        if (gitAction.equalsIgnoreCase("ADD") || gitAction.equalsIgnoreCase("RENAME")) {
            return "added";
        }
        if (gitAction.equals("MODIFY")) {
            return "modified";
        }
        if (gitAction.equals("DELETE")) {
            return "deleted";
        }
        return "unknown";
    }

    private Repository getRepository(File folder) throws IOException {
        return new FileRepositoryBuilder().setGitDir(getGitDir(folder)).readEnvironment().findGitDir().build();
    }

    private File getGitDir(File folder) {
        return new File(folder, ".git");
    }

    private void setCredentials(TransportCommand command) {
        if (gitConfig.isRemoteUrl() && gitConfig.hasCredentials()) {
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitConfig.getUsername(), gitConfig.getPassword()));
        }
    }
}
