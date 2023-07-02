package com.tw.go.plugin.git;

import com.tw.go.plugin.AbstractGitHelperTest;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.Pair;
import com.tw.go.plugin.cmd.InMemoryConsumer;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import com.tw.go.plugin.model.ShallowClone;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GitCmdHelperTest extends AbstractGitHelperTest {
    @Override
    protected GitHelper getHelper(GitConfig gitConfig, File workingDir) {
        ProcessOutputStreamConsumer stdOut = new ProcessOutputStreamConsumer(new InMemoryConsumer() {
            @Override
            public void consumeLine(String line) {
                System.out.println(line);
            }
        });
        return new GitCmdHelper(gitConfig, workingDir, stdOut, new ProcessOutputStreamConsumer(new InMemoryConsumer() {
                    @Override
                    public void consumeLine(String line) {
                       System.err.println(line);
                    }
                }));
    }

    @Test
    public void shouldShallowClone() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-2.zip");
        GitConfig config = new GitConfig("file://" + simpleGitRepository.getAbsolutePath());
        config.setShallowClone(new ShallowClone(1, 2));
        GitHelper git = getHelper(config, testRepository);

        git.cloneOrFetch();

        assertThat(git.getCommitCount()).isEqualTo(1);

        Revision revision = git.getLatestRevision();
        verifyRevision(revision, "24ce45d1a1427b643ae859777417bbc9f0d7cec8", "3\ntest multiline\ncomment", 1422189618000L, List.of(new Pair("a.txt", "added"), new Pair("b.txt", "added")));
        List<Revision> newerRevisions = git.getRevisionsSince("24ce45d1a1427b643ae859777417bbc9f0d7cec8");
        assertThat(newerRevisions.isEmpty()).isEqualTo(true);

        FileUtils.deleteQuietly(testRepository);

        // Increase default depth
        config.setShallowClone(new ShallowClone(2, 3));
        // poll again
        git.cloneOrFetch();

        assertThat(git.getCommitCount()).isEqualTo(2);
        verifyRevision(revision, "24ce45d1a1427b643ae859777417bbc9f0d7cec8", "3\ntest multiline\ncomment", 1422189618000L, List.of(new Pair("a.txt", "added"), new Pair("b.txt", "added")));
    }

    @Test
    public void shallowCloneShouldFetchMoreCommitsOnResetIfNecessary() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-2.zip");
        GitConfig config = new GitConfig("file://" + simpleGitRepository.getAbsolutePath());
        config.setShallowClone(new ShallowClone(1, 2));
        GitHelper git = getHelper(config, testRepository);

        git.cloneOrFetch();

        assertThat(git.getCommitCount()).isEqualTo(1);

        Revision revision = git.getLatestRevision();
        assertThat(revision.getRevision()).isEqualTo("24ce45d1a1427b643ae859777417bbc9f0d7cec8");

        git.resetHard("012e893acea10b140688d11beaa728e8c60bd9f6");

        assertThat(git.getCommitCount()).isEqualTo(1);
    }

    @Test
    public void shouldCloneWithNoCheckout() throws Exception {
        extractToTmp("/sample-repository/simple-git-repository-2.zip");

        GitConfig config = new GitConfig("file://" + simpleGitRepository.getAbsolutePath());
        config.setNoCheckout(true);
        GitHelper git = getHelper(config, testRepository);

        git.cloneOrFetch();
        assertThat(List.of(testRepository.list())).contains(".git");

        assertThat(git.getCommitCount()).isEqualTo(3);

        Revision revision = git.getLatestRevision();
        verifyRevision(revision, "24ce45d1a1427b643ae859777417bbc9f0d7cec8", "3\ntest multiline\ncomment", 1422189618000L, List.of(new Pair("a.txt", "modified"), new Pair("b.txt", "added")));

        // poll again
        git.cloneOrFetch();
        assertThat(List.of(testRepository.list())).contains(".git");

        List<Revision> newerRevisions = git.getRevisionsSince("24ce45d1a1427b643ae859777417bbc9f0d7cec8");

        assertThat(newerRevisions.isEmpty()).isEqualTo(true);
    }
}
