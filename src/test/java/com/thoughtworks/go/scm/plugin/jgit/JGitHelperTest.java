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
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JGitHelperTest {

    private static final File TEST_REPO = new File(System.getProperty("java.io.tmpdir") + File.separator + "test_repo");
    private RevCommit initialCommit;
    private JGitHelper helper;

    @Before
    public void after() throws IOException, GitAPIException {
        if (TEST_REPO.exists()) FileUtils.delete(TEST_REPO, FileUtils.RECURSIVE);
        FileUtils.mkdir(TEST_REPO);
        initialCommit = addContentAndCommit(TEST_REPO.getPath(), "Initial commit");
        helper = JGitHelper.create(null, TEST_REPO.getAbsolutePath());
    }

    @Test
    public void shouldGetLatestRevision() throws Exception {
        Revision latestRevision = helper.getLatestRevision();
        assertEquals(initialCommit.getName(), latestRevision.getRevision());
    }

    @Test
    public void shouldGetLatestRevisionForGivenPath() throws Exception {
        String service1 = "service1";
        RevCommit service1Commit = addContentAndCommit(TEST_REPO.getPath() + File.separator + service1, "Service 1 commit");
        String service2 = "service2";
        RevCommit service2Commit = addContentAndCommit(TEST_REPO.getPath() + File.separator + service2, "Service 2 commit");

        assertEquals(service2Commit.getName(), helper.getLatestRevision().getRevision());
        assertEquals(service1Commit.getName(), helper.getLatestRevision(service1).getRevision());
        assertEquals(service2Commit.getName(), helper.getLatestRevision(service2).getRevision());
    }

    @Test
    public void shouldGetAllRevisions() throws Exception {
        RevCommit commit2 = addContentAndCommit(TEST_REPO.getPath(), "Another commit.");

        List<Revision> revisions = helper.getAllRevisions();

        assertEquals(2, revisions.size());
        assertEquals(initialCommit.getName(), revisions.get(1).getRevision());
        assertEquals(commit2.getName(), revisions.get(0).getRevision());
    }

    @Test
    public void shouldGetRevisionsSince() throws Exception {
        RevCommit commit2 = addContentAndCommit(TEST_REPO.getPath(), "Another commit.");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName());

        assertArrayEquals(new Object[]{commit2.getName()}, revisions.stream().map(Revision::getRevision).toArray());
    }

    @Test
    public void shouldGetRevisionsSinceForGivenPath() throws Exception {
        String service1 = "service1";
        RevCommit service1Commit = addContentAndCommit(TEST_REPO.getPath() + File.separator + service1, "Service 1 commit");
        String service2 = "service2";
        RevCommit service2Commit = addContentAndCommit(TEST_REPO.getPath() + File.separator + service2, "Service 2 commit");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName(), service1);
        assertArrayEquals(new Object[]{service1Commit.getName()}, revisions.stream().map(Revision::getRevision).toArray());

        revisions = helper.getRevisionsSince(initialCommit.getName(), service2);
        assertArrayEquals(new Object[]{service2Commit.getName()}, revisions.stream().map(Revision::getRevision).toArray());
    }

    private RevCommit addContentAndCommit(String path, String msg) throws IOException, GitAPIException {
        addContent(path);
        return commit(msg);
    }

    private RevCommit commit(String msg) throws GitAPIException {
        Git repo = Git.init().setDirectory(TEST_REPO).call();
        repo.add().addFilepattern(".").call();
        return repo.commit().setMessage(msg).call();
    }

    private void addContent(String path) throws IOException {
        File dir = new File(path);
        FileUtils.mkdir(dir, true);
        FileUtils.createNewFile(new File(dir.getPath() + File.separator + UUID.randomUUID().toString()));
    }

}