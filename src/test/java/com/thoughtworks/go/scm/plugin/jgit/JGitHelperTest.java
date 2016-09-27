package com.thoughtworks.go.scm.plugin.jgit;

import com.thoughtworks.go.scm.plugin.model.Revision;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JGitHelperTest {

    private static final File TEST_REPO = new File(System.getProperty("java.io.tmpdir") + File.separator + "test_repo");
    private RevCommit initialCommit;
    private JGitHelper helper;

    @Before
    public void after() throws IOException, GitAPIException {
        if (TEST_REPO.exists()) FileUtils.delete(TEST_REPO, FileUtils.RECURSIVE);
        FileUtils.mkdir(TEST_REPO);
        FileUtils.createNewFile(new File(TEST_REPO.getPath() + File.separator + "content1.txt"));
        initialCommit = addAndCommit("Initial commit");
        helper = JGitHelper.create(null, TEST_REPO.getAbsolutePath());
    }

    @Test
    public void shouldGetLatestRevision() throws Exception {
        Revision latestRevision = helper.getLatestRevision();
        assertEquals(initialCommit.getName(), latestRevision.getRevision());
    }

    @Test
    public void shouldGetLatestRevisionForGivenPath() throws Exception {
        String[] subDirs = {"service1", "service2"};
        Map<String, RevCommit> commits = new HashMap<>();
        for (String subDirName : subDirs) {
            File subdir = new File(TEST_REPO.getPath() + File.separator + subDirName);
            FileUtils.mkdir(subdir);
            FileUtils.createNewFile(new File(subdir.getPath() + File.separator + "content.txt"));
            commits.put(subDirName, addAndCommit(subDirName + " commit"));
        }
        assertEquals(commits.get(subDirs[1]).getName(), helper.getLatestRevision().getRevision());
        assertEquals(commits.get(subDirs[0]).getName(), helper.getLatestRevision(subDirs[0]).getRevision());
        assertEquals(commits.get(subDirs[1]).getName(), helper.getLatestRevision(subDirs[1]).getRevision());
    }

    @Test
    public void shouldGetAllRevisions() throws Exception {
        FileUtils.createNewFile(new File(TEST_REPO.getPath() + File.separator + "content2.txt"));
        RevCommit commit2 = addAndCommit("Another commit.");

        List<Revision> revisions = helper.getAllRevisions();

        assertEquals(2, revisions.size());
        assertEquals(initialCommit.getName(), revisions.get(1).getRevision());
        assertEquals(commit2.getName(), revisions.get(0).getRevision());
    }

    @Test
    public void shouldGetRevisionsSince() throws Exception {
        FileUtils.createNewFile(new File(TEST_REPO.getPath() + File.separator + "content2.txt"));
        RevCommit commit2 = addAndCommit("Another commit.");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName());

        assertEquals(1, revisions.size());
        assertEquals(commit2.getName(), revisions.get(0).getRevision());
    }

    private RevCommit addAndCommit(String msg) throws GitAPIException {
        Git repo = Git.init().setDirectory(TEST_REPO).call();
        repo.add().addFilepattern(".").call();
        return repo.commit().setMessage(msg).call();
    }

}