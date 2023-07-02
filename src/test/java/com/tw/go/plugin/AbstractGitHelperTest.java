package com.tw.go.plugin;

import com.tw.go.plugin.git.GitCmdHelper;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.org.webcompere.systemstubs.SystemStubs.restoreSystemProperties;

public abstract class AbstractGitHelperTest {
    private static final int BUFFER_SIZE = 4096;

    protected final File testRepository = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    protected final File simpleGitRepository = new File(System.getProperty("java.io.tmpdir"), "simple-git-repository");
    private final File subModuleGitRepository = new File(System.getProperty("java.io.tmpdir"), "sub-module-git-repository");
    private final File branchGitRepository = new File(System.getProperty("java.io.tmpdir"), "branch-git-repository");
    private final File mergeCommitGitRepository = new File(System.getProperty("java.io.tmpdir"), "merge-commit-git-repository");

    @BeforeEach
    public void setUp() {
        cleanTmpFiles();
    }

    @AfterEach
    public void tearDown() {
        cleanTmpFiles();
    }

    private void cleanTmpFiles() {
        FileUtils.deleteQuietly(testRepository);
        FileUtils.deleteQuietly(simpleGitRepository);
        FileUtils.deleteQuietly(subModuleGitRepository);
        FileUtils.deleteQuietly(branchGitRepository);
        FileUtils.deleteQuietly(mergeCommitGitRepository);
    }

    protected abstract GitHelper getHelper(GitConfig gitConfig, File workingDir);

    @Test
    public void shouldGetVersion() {
        GitHelper git = getHelper(null, null);
        assertThat(git.version()).isNotNull();
    }

    @Test
    public void shouldCheckConnection() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-1.zip");

        GitHelper gitValidRepository = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), null);
        try {
            gitValidRepository.checkConnection();
        } catch (Throwable t) {
            fail("check connection failed for a valid repository");
        }

        GitHelper gitInValidRepository = getHelper(new GitConfig(new File(System.getProperty("java.io.tmpdir"), "non-existing-repository").getAbsolutePath()), null);
        try {
            gitInValidRepository.checkConnection();
            fail("check connection failed for a valid repository");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    public void shouldGetRevisionForRepository() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-1.zip");

        GitHelper git = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), testRepository);
        git.cloneOrFetch();

        assertThat(git.workingRepositoryUrl()).isEqualTo(simpleGitRepository.getAbsolutePath());
        assertThat(git.getCommitCount()).isEqualTo(1);
        assertThat(git.currentRevision()).isEqualTo("012e893acea10b140688d11beaa728e8c60bd9f6");

        Revision revision = git.getDetailsForRevision("012e893acea10b140688d11beaa728e8c60bd9f6");
        verifyRevision(revision, "012e893acea10b140688d11beaa728e8c60bd9f6", "1", 1422184635000L, List.of(new Pair("a.txt", "added")));
    }

    @Test
    public void shouldPollRepository() throws Exception {
        // Checkout & Get LatestRevision
        extractToTmp("/sample-repository/simple-git-repository-1.zip");

        GitHelper git = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), testRepository);
        git.cloneOrFetch();

        assertThat(git.getCurrentBranch()).isEqualTo("master");
        assertThat(git.getCommitCount()).isEqualTo(1);

        Revision revision = git.getLatestRevision();

        verifyRevision(revision, "012e893acea10b140688d11beaa728e8c60bd9f6", "1", 1422184635000L, List.of(new Pair("a.txt", "added")));

        // Fetch & Get LatestRevisionsSince
        FileUtils.deleteQuietly(simpleGitRepository.getAbsoluteFile());
        extractToTmp("/sample-repository/simple-git-repository-2.zip");

        git.cloneOrFetch();

        assertThat(git.getCurrentBranch()).isEqualTo("master");
        assertThat(git.getCommitCount()).isEqualTo(3);

        List<Revision> newerRevisions = git.getRevisionsSince("012e893acea10b140688d11beaa728e8c60bd9f6");

        assertThat(newerRevisions.size()).isEqualTo(2);
        verifyRevision(newerRevisions.get(0), "24ce45d1a1427b643ae859777417bbc9f0d7cec8", "3\ntest multiline\ncomment", 1422189618000L, List.of(new Pair("a.txt", "modified"), new Pair("b.txt", "added")));
        verifyRevision(newerRevisions.get(1), "1320a78055558603a2c29d803bbaa50d3542ff50", "2", 1422189545000L, List.of(new Pair("a.txt", "modified")));

        // poll again
        git.cloneOrFetch();

        newerRevisions = git.getRevisionsSince("24ce45d1a1427b643ae859777417bbc9f0d7cec8");

        assertThat(newerRevisions.isEmpty()).isEqualTo(true);
    }

    @Test
    public void shouldGetLatestRevisionForSubpaths() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-3.zip");

        GitHelper git = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), testRepository);
        git.cloneOrFetch();

        assertThat(git.getCommitCount()).isEqualTo(4);

        final Revision aRevision = git.getLatestRevision(List.of("a.txt"));
        final Revision bRevision = git.getLatestRevision(List.of("b.txt"));

        assertThat(aRevision.getRevision()).isEqualTo("7d14db6ec07f2cfac82195e401780bf127349ddb");
        assertThat(aRevision.getModifiedFiles()).hasSize(1);
        verifyRevision(aRevision, "7d14db6ec07f2cfac82195e401780bf127349ddb", "Change to a.txt", 1567878351000L, List.of(new Pair("a.txt", "modified")));

        assertThat(bRevision.getRevision()).isEqualTo("24ce45d1a1427b643ae859777417bbc9f0d7cec8");
        assertThat(bRevision.getModifiedFiles()).hasSize(2);

        final Revision eitherRevision = git.getLatestRevision(List.of("a.txt", "b.txt"));
        assertThat(eitherRevision.getRevision()).isEqualTo(aRevision.getRevision());

        List<Revision> aRevisions = git.getRevisionsSince("24ce45d1a1427b643ae859777417bbc9f0d7cec8", List.of("a.txt"));
        assertThat(aRevisions).hasSize(1);
        verifyRevision(aRevisions.get(0), "7d14db6ec07f2cfac82195e401780bf127349ddb", "Change to a.txt", 1567878351000L, List.of(new Pair("a.txt", "modified")));

        List<Revision> bRevisions = git.getRevisionsSince("24ce45d1a1427b643ae859777417bbc9f0d7cec8", List.of("b.txt"));
        assertThat(bRevisions).hasSize(0);

        List<Revision> eitherRevisions = git.getRevisionsSince("012e893acea10b140688d11beaa728e8c60bd9f6", List.of("a.txt","b.txt"));
        assertThat(eitherRevisions).hasSize(3);
        assertThat(eitherRevisions.get(2).getRevision()).isEqualTo("1320a78055558603a2c29d803bbaa50d3542ff50");
    }

    @Test
    public void shouldCheckoutBranch() throws Exception {
        extractToTmp("/sample-repository/branch-git-repository.zip");

        GitHelper git = getHelper(new GitConfig(branchGitRepository.getAbsolutePath(), null, null, "feature-branch"), testRepository);
        git.cloneOrFetch();

        assertThat(git.getCurrentBranch()).isEqualTo("feature-branch");
        assertThat(git.getCommitCount()).isEqualTo(2);
        assertThat(new File(testRepository, "a.txt").exists()).isEqualTo(true);
        assertThat(new File(testRepository, "b.txt").exists()).isEqualTo(true);
    }

    @Test
    public void shouldGetBranchToRevisionMap() throws Exception {
        extractToTmp("/sample-repository/branch-git-repository.zip");

        GitHelper git = getHelper(new GitConfig(branchGitRepository.getAbsolutePath(), null, null, null), testRepository);
        git.cloneOrFetch();

        Map<String, String> branchToRevisionMap = git.getBranchToRevisionMap();

        assertThat(branchToRevisionMap.size()).isEqualTo(2);
        assertThat(branchToRevisionMap.get("master")).isEqualTo("012e893acea10b140688d11beaa728e8c60bd9f6");
        assertThat(branchToRevisionMap.get("feature-branch")).isEqualTo("765e24764ee4f6fc10e4301b4f9528c08ff178d4");
    }

    @Test
    public void shouldRecursiveSubModuleUpdate() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty(GitCmdHelper.GIT_SUBMODULE_ALLOW_FILE_PROTOCOL, "Y");
            extractToTmp("/sample-repository/simple-git-repository-1.zip");
            extractToTmp("/sample-repository/sub-module-git-repository.zip");

            GitHelper gitRemote = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), simpleGitRepository);
            gitRemote.submoduleAdd(subModuleGitRepository.getAbsolutePath(), "sub-module", "sub-module");
            gitRemote.commit("add sub-module");

            GitConfig gitConfig = new GitConfig(simpleGitRepository.getAbsolutePath(), null, null, "master", true, false);
            GitHelper gitMain = getHelper(gitConfig, testRepository);
            gitMain.cloneOrFetch();

            assertThat(gitMain.getCommitCount()).isEqualTo(2);

            assertThat(gitMain.getSubModuleCommitCount("sub-module")).isEqualTo(2);

            // TODO: add commit to sub-module & main-repo

            // poll again
            gitMain.cloneOrFetch();

            assertThat(gitMain.getCommitCount()).isEqualTo(2);

            assertThat(gitMain.getSubModuleCommitCount("sub-module")).isEqualTo(2);
        });
    }

    @Test
    public void shouldWorkWithRepositoriesWithSubModules() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty(GitCmdHelper.GIT_SUBMODULE_ALLOW_FILE_PROTOCOL, "Y");
            extractToTmp("/sample-repository/simple-git-repository-1.zip");
            extractToTmp("/sample-repository/sub-module-git-repository.zip");

            GitHelper gitRemote = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), simpleGitRepository);
            gitRemote.submoduleAdd(subModuleGitRepository.getAbsolutePath(), "sub-module", "sub-module");
            gitRemote.commit("add sub-module");

            List<String> submoduleFolders = gitRemote.submoduleFolders();
            assertThat(submoduleFolders.size()).isEqualTo(1);
            assertThat(submoduleFolders.get(0)).isEqualTo("sub-module");
        });
    }



    @Test
    public void shouldCheckoutToRevision() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-2.zip");

        GitHelper git = getHelper(new GitConfig(simpleGitRepository.getAbsolutePath()), testRepository);
        git.cloneOrFetch();

        git.resetHard("24ce45d1a1427b643ae859777417bbc9f0d7cec8");

        assertThat(new File(testRepository, "a.txt").exists()).isEqualTo(true);
        assertThat(new File(testRepository, "b.txt").exists()).isEqualTo(true);

        git.resetHard("1320a78055558603a2c29d803bbaa50d3542ff50");

        assertThat(new File(testRepository, "a.txt").exists()).isEqualTo(true);
        assertThat(new File(testRepository, "b.txt").exists()).isEqualTo(false);
    }

    @Test
    public void shouldInitAddCommit() throws Exception {
        testRepository.mkdirs();

        GitHelper git = getHelper(null, testRepository);
        git.init();
        File file = new File(testRepository, "a.txt");
        FileUtils.writeStringToFile(file, "content", StandardCharsets.UTF_8);
        git.add(file);
        git.commit("comment");

        List<Revision> allRevisions = git.getAllRevisions();
        assertThat(allRevisions.size()).isEqualTo(1);

        Revision revision = allRevisions.get(0);
        assertThat(revision.getComment()).isEqualTo("comment");
        assertThat(revision.getModifiedFiles().size()).isEqualTo(1);
        assertThat(revision.getModifiedFiles().get(0).getFileName()).isEqualTo("a.txt");
        assertThat(revision.getModifiedFiles().get(0).getAction()).isEqualTo("added");
    }

    @Test @Disabled
    public void shouldWorkWithGithubRepository() {
        GitHelper git = getHelper(new GitConfig("https://github.com/mdaliejaz/samplerepo.git"), testRepository);
        git.cloneOrFetch("+refs/pull/*/merge:refs/gh-merge/remotes/origin/*");

        Map<String, String> branchToRevisionMap = git.getBranchToRevisionMap("refs/gh-merge/remotes/origin/");

        assertThat(branchToRevisionMap.size()).isEqualTo(1);
        assertThat(branchToRevisionMap.get("1")).isEqualTo("aabd0f242bd40bfaaa4ce359123b2a2d976077d1");
    }

    @Test
    public void shouldReturnModifiedFilesForMergeCommit() throws Exception {
        extractToTmp("/sample-repository/merge-commit-git-repository.zip");

        GitHelper git = getHelper(new GitConfig(mergeCommitGitRepository.getAbsolutePath()), mergeCommitGitRepository);
        Revision revision = git.getDetailsForRevision("66a1b17514622a8e4a620a033cca3715ef870e71");

        verifyRevision(revision, "66a1b17514622a8e4a620a033cca3715ef870e71", "Merge branch 'master' into test-branch", 1477248891000L, List.of(new Pair("file.txt", "modified")));
        assertTrue(revision.isMergeCommit(), "Revision should be a merge commit");
    }

    protected void extractToTmp(String zipResourcePath) throws IOException {
        File zipFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + ".zip");

        IOUtils.copy(getClass().getResourceAsStream(zipResourcePath), new FileOutputStream(zipFile));

        unzip(zipFile.getAbsolutePath(), System.getProperty("java.io.tmpdir"));

        FileUtils.deleteQuietly(zipFile);
    }

    private void unzip(String zipFilePath, String destinationDirectoryPath) throws IOException {
        File destinationDirectory = new File(destinationDirectoryPath);
        if (!destinationDirectory.exists()) {
            FileUtils.forceMkdir(destinationDirectory);
        }

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipInputStream.getNextEntry();
        while (entry != null) {
            String filePath = destinationDirectoryPath + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(zipInputStream, filePath);
            } else {
                FileUtils.forceMkdir(new File(filePath));
            }

            zipInputStream.closeEntry();
            entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    private void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesRead = new byte[BUFFER_SIZE];
        int readByteCount;
        while ((readByteCount = zipInputStream.read(bytesRead)) != -1) {
            bufferedOutputStream.write(bytesRead, 0, readByteCount);
        }
        bufferedOutputStream.close();
    }

    protected void verifyRevision(Revision revision, String sha, String comment, long timestamp, List<Pair> files) {
        assertThat(revision.getRevision()).isEqualTo(sha);
        assertThat(revision.getTimestamp().getTime()).isEqualTo(timestamp);
        assertThat(revision.getComment()).isEqualTo(comment);
        assertThat(revision.getModifiedFiles().size()).isEqualTo(files.size());
        for (int i = 0; i < files.size(); i++) {
            assertThat(revision.getModifiedFiles().get(i).getFileName()).isEqualTo(files.get(i).a);
            assertThat(revision.getModifiedFiles().get(i).getAction()).isEqualTo(files.get(i).b);
        }
    }
}
