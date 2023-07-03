package com.thoughtworks.go.scm.plugin.git;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class GitConfigTest {
    @Test
    public void shouldGetEffectiveBranch() {
        assertThat(new GitConfig("url", null, null, null).getEffectiveBranch()).isEqualTo("master");
        assertThat(new GitConfig("url", null, null, "branch").getEffectiveBranch()).isEqualTo("branch");
    }

    @Test
    public void isRemoteUrlShouldBeTrueForHttp() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void isRemoteUrlShouldBeTrueForHttps() {
        GitConfig gitConfig = new GitConfig("https://url.test", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void isRemoteUrlShouldBeTrueForGitUrl() {
        GitConfig gitConfig = new GitConfig("git@github.com:test/sample.git", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void hasCredentialsShouldBeTrueIfUrlAndPasswordAreProvided() {
        GitConfig gitConfig = new GitConfig("https://url.test", "user", "password", "master");

        assertThat(gitConfig.hasCredentials()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void hasCredentialsShouldBeFalseIfUrlIsEmptyWithValidPassword() {
        GitConfig gitConfig = new GitConfig("", "user", "password", "master");

        assertThat(gitConfig.hasCredentials()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void hasCredentialsShouldBeFalseWithValidUrlAndEmptyPassword() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "", "master");

        assertThat(gitConfig.hasCredentials()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void shouldGetEffectiveUrl() {
        assertThat(new GitConfig("/tmp/git-repo", null, null, null).getEffectiveUrl()).isEqualTo("/tmp/git-repo");
        assertThat(new GitConfig("/tmp/git-repo", "username", "password", null).getEffectiveUrl()).isEqualTo("/tmp/git-repo");
        assertThat(new GitConfig("http://github.com/gocd/gocd", null, null, null).getEffectiveUrl()).isEqualTo("http://github.com/gocd/gocd");
        assertThat(new GitConfig("http://github.com/gocd/gocd", "username", "password", null).getEffectiveUrl()).isEqualTo("http://username:password@github.com/gocd/gocd");
        assertThat(new GitConfig("https://github.com/gocd/gocd", "username", "password", null).getEffectiveUrl()).isEqualTo("https://username:password@github.com/gocd/gocd");
    }

    @Test
    public void getEffectiveUrlShouldContainUserNameAndPasswordForRemoteUrlWithValidCredential() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl).isEqualTo("http://user:password@url.test");
    }

    @Test
    public void getEffectiveUrlShouldNotContainUserNameAndPasswordForNonRemoteUrlWithValidCredential() {
        GitConfig gitConfig = new GitConfig("git@github.com:test/sample.git", "user", "password", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl).isEqualTo("git@github.com:test/sample.git");
    }

    @Test
    public void getEffectiveUrlShouldNotContainUserNameAndPasswordForRemoteUrlWithoutCredential() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl).isEqualTo("http://url.test");
    }

    @Test
    public void getEffectiveBranchShouldReturnTheSpecifiedBranch() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "staging");

        String effectiveBranch = gitConfig.getEffectiveBranch();

        assertThat(effectiveBranch).isEqualTo("staging");
    }

    @Test
    public void getEffectiveBranchShouldReturnMasterIfBranchisNotSpecified() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "");

        String effectiveBranch = gitConfig.getEffectiveBranch();

        assertThat(effectiveBranch).isEqualTo("master");
    }

    @Test
    public void shouldBeAbleToGetIsRecursiveSubModuleUpdate() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        boolean recursiveSubModuleUpdate = gitConfig.isRecursiveSubModuleUpdate();

        assertThat(recursiveSubModuleUpdate).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void shouldGetUrl() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getUrl()).isEqualTo("http://url.test");
    }

    @Test
    public void shouldGetUsername() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getUsername()).isEqualTo("username");
    }

    @Test
    public void getPassword() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getPassword()).isEqualTo("password");
    }

    @Test
    public void getBranch() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getBranch()).isEqualTo("branch");
    }
}
