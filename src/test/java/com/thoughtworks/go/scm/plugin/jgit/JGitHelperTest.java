package com.thoughtworks.go.scm.plugin.jgit;

import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.Revision;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Git.class)
public class JGitHelperTest {

    private static final File TEST_REPO = new File(System.getProperty("java.io.tmpdir"), "test_repo");
    private RevCommit initialCommit;
    private String destinationFolder;
    private GitConfig gitConfig;

    @Before
    public void after() throws IOException, GitAPIException {
        if (TEST_REPO.exists()) FileUtils.delete(TEST_REPO, FileUtils.RECURSIVE);
        FileUtils.mkdir(TEST_REPO);
        initialCommit = addContentAndCommit(TEST_REPO.getPath(), "Initial commit");
        destinationFolder = TEST_REPO.getAbsolutePath();
        gitConfig = new GitConfig("http://url.test", "username", "password", "master");
    }

    @Test
    public void shouldGetLatestRevision() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);

        Revision latestRevision = helper.getLatestRevision();

        assertThat(latestRevision.getRevision(), is(equalTo(initialCommit.getName())));
    }

    @Test
    public void shouldGetLatestRevisionForGivenPath() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        String service1 = "service1";
        String service2 = "service2";

        RevCommit service1Commit = addContentAndCommit(getFilePath(service1), "Service 1 commit");
        RevCommit service2Commit = addContentAndCommit(getFilePath(service2), "Service 2 commit");

        assertThat(helper.getLatestRevision().getRevision(), is(equalTo(service2Commit.getName())));
        assertThat(helper.getLatestRevision(service1).getRevision(), is(equalTo(service1Commit.getName())));
        assertThat(helper.getLatestRevision(service2).getRevision(), is(equalTo(service2Commit.getName())));
    }

    @Test
    public void shouldGetLatestRevisionForGivenCommaSeparatedPath() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        String service1 = "service1";
        String service2 = "service2";

        RevCommit service1Commit = addContentAndCommit(getFilePath(service1), "Service 1 commit");
        RevCommit service2Commit = addContentAndCommit(getFilePath(service2), "Service 2 commit");

        assertThat(helper.getLatestRevision().getRevision(), is(equalTo(service2Commit.getName())));
        assertThat(helper.getLatestRevision("service1, service2").getRevision(), is(equalTo(service2Commit.getName())));
    }

    @Test
    public void shouldGetAllRevisions() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        RevCommit commit2 = addContentAndCommit(TEST_REPO.getPath(), "Another commit.");

        List<Revision> revisions = helper.getAllRevisions();

        assertThat(revisions, hasSize(2));
        assertThat(revisions.get(1).getRevision(), is(equalTo(initialCommit.getName())));
        assertThat(revisions.get(0).getRevision(), is(equalTo(commit2.getName())));
    }

    @Test
    public void shouldGetRevisionsSince() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        RevCommit commit2 = addContentAndCommit(TEST_REPO.getPath(), "Another commit.");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName());

        assertThat(revisions, hasSize(1));
        assertThat(revisions.get(0).getRevision(), is(equalTo(commit2.getName())));
    }

    @Test
    public void shouldGetRevisionsSinceForGivenPath() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        String service1 = "service1";
        String service2 = "service2";
        RevCommit service1Commit = addContentAndCommit(getFilePath(service1), "Service 1 commit");
        RevCommit service2Commit = addContentAndCommit(getFilePath(service2), "Service 2 commit");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName(), service1);

        assertThat(revisions, hasSize(1));
        assertThat(revisions.get(0).getRevision(), is(equalTo(service1Commit.getName())));

        revisions = helper.getRevisionsSince(initialCommit.getName(), service2);

        assertThat(revisions, hasSize(1));
        assertThat(revisions.get(0).getRevision(), is(equalTo(service2Commit.getName())));
    }

    @Test
    public void shouldGetRevisionsSinceForCommaSeparatedGivenPath() throws Exception {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        String service1 = "service1";
        String service2 = "service2";
        RevCommit service1Commit = addContentAndCommit(getFilePath(service1), "Service 1 commit");
        RevCommit service2Commit = addContentAndCommit(getFilePath(service2), "Service 2 commit");

        List<Revision> revisions = helper.getRevisionsSince(initialCommit.getName(), "service1, service2");

        assertThat(revisions, hasSize(2));
        assertThat(revisions.get(0).getRevision(), is(equalTo(service2Commit.getName())));
        assertThat(revisions.get(1).getRevision(), is(equalTo(service1Commit.getName())));
    }

    @Test
    public void shouldCheckConnection() throws GitAPIException {
        GitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        LsRemoteCommand remoteCommand = mock(LsRemoteCommand.class);
        ArgumentCaptor<CredentialsProvider> credentialsProviderArgumentCaptor = ArgumentCaptor.forClass(CredentialsProvider.class);
        CredentialItem.Username username = new CredentialItem.Username();
        CredentialItem.Password password = new CredentialItem.Password();

        PowerMockito.mockStatic(Git.class);
        when(Git.lsRemoteRepository()).thenReturn(remoteCommand);
        when(remoteCommand.setHeads(true)).thenReturn(remoteCommand);
        when(remoteCommand.setRemote(gitConfig.getUrl())).thenReturn(remoteCommand);

        helper.checkConnection();

        verify(remoteCommand).setHeads(true);
        verify(remoteCommand).setRemote(gitConfig.getUrl());
        verify(remoteCommand).setCredentialsProvider(credentialsProviderArgumentCaptor.capture());
        verify(remoteCommand).call();

        CredentialsProvider credentialsProvider = credentialsProviderArgumentCaptor.getValue();
        credentialsProvider.get(null, username, password);

        assertThat(username.getValue(), is(equalTo("username")));
        assertThat(password.getValue(), is(equalTo("password".toCharArray())));
    }

    @Test
    public void checkConnectionShouldNotProvideCredentialForNonRemoteUrl() throws GitAPIException {
        GitConfig gitConfig = new GitConfig("git@github.com:gocd/gocd-docker.git", "username", "password", "master");
        JGitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        LsRemoteCommand remoteCommandMock = mock(LsRemoteCommand.class);
        ArgumentCaptor<CredentialsProvider> credentialsProviderArgumentCaptor = ArgumentCaptor.forClass(CredentialsProvider.class);

        PowerMockito.mockStatic(Git.class);
        when(Git.lsRemoteRepository()).thenReturn(remoteCommandMock);
        when(remoteCommandMock.setHeads(true)).thenReturn(remoteCommandMock);
        when(remoteCommandMock.setRemote(gitConfig.getUrl())).thenReturn(remoteCommandMock);


        helper.checkConnection();

        verify(remoteCommandMock).setHeads(true);
        verify(remoteCommandMock).setRemote(gitConfig.getUrl());
        verify(remoteCommandMock, never()).setCredentialsProvider(credentialsProviderArgumentCaptor.capture());
        verify(remoteCommandMock).call();
    }

    @Test
    public void checkConnectionShouldNotProvideCredentialWhenCredentialsAreNotProvided() throws GitAPIException {
        GitConfig gitConfig = new GitConfig("git@github.com:gocd/gocd-docker.git", "", "", "master");
        JGitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        LsRemoteCommand remoteCommandMock = mock(LsRemoteCommand.class);
        ArgumentCaptor<CredentialsProvider> credentialsProviderArgumentCaptor = ArgumentCaptor.forClass(CredentialsProvider.class);

        PowerMockito.mockStatic(Git.class);
        when(Git.lsRemoteRepository()).thenReturn(remoteCommandMock);
        when(remoteCommandMock.setHeads(true)).thenReturn(remoteCommandMock);
        when(remoteCommandMock.setRemote(gitConfig.getUrl())).thenReturn(remoteCommandMock);

        helper.checkConnection();

        verify(remoteCommandMock).setHeads(true);
        verify(remoteCommandMock).setRemote(gitConfig.getUrl());
        verify(remoteCommandMock, never()).setCredentialsProvider(credentialsProviderArgumentCaptor.capture());
        verify(remoteCommandMock).call();
    }

    @Test
    public void shouldCloneRepository() throws GitAPIException {
        JGitHelper helper = JGitHelper.create(gitConfig, destinationFolder);
        CloneCommand cloneCommandMock = mock(CloneCommand.class);
        ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);

        PowerMockito.mockStatic(Git.class);
        when(Git.cloneRepository()).thenReturn(cloneCommandMock);
        when(cloneCommandMock.setURI(gitConfig.getUrl())).thenReturn(cloneCommandMock);
        when(cloneCommandMock.setDirectory(any(File.class))).thenReturn(cloneCommandMock);
        when(cloneCommandMock.setBranch(gitConfig.getEffectiveBranch())).thenReturn(cloneCommandMock);

        helper.cloneRepository();

        verify(cloneCommandMock).setURI(gitConfig.getUrl());
        verify(cloneCommandMock).setDirectory(fileArgumentCaptor.capture());
        verify(cloneCommandMock).setBranch(gitConfig.getEffectiveBranch());

        assertThat(fileArgumentCaptor.getValue().getPath(), is(equalTo(destinationFolder)));
    }

    private String getFilePath(String service1) {
        return new File(TEST_REPO.getPath(), service1).getPath();
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
        FileUtils.createNewFile(new File(dir.getPath(), UUID.randomUUID().toString()));
    }

}