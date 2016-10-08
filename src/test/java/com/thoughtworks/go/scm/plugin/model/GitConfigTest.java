package com.thoughtworks.go.scm.plugin.model;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JsonUtils.class)
public class GitConfigTest {


    @Test
    public void shouldCreateGitConfig() {
        HashMap<String, String> scmConfiguration = new HashMap<String, String>() {{
            put("url", "http://url.test");
            put("username", "user");
            put("password", "pass");
            put("branch", "master");
        }};

        PowerMockito.mockStatic(JsonUtils.class);
        GoPluginApiRequest pluginApiRequest = mock(GoPluginApiRequest.class);
        when(JsonUtils.parseScmConfiguration(pluginApiRequest)).thenReturn(scmConfiguration);

        GitConfig gitConfig = GitConfig.create(pluginApiRequest);

        assertThat(gitConfig.getUrl(), is(equalTo("http://url.test")));
        assertThat(gitConfig.getUsername(), is(equalTo("user")));
        assertThat(gitConfig.getPassword(), is(equalTo("pass")));
        assertThat(gitConfig.getBranch(), is(equalTo("master")));
    }

    @Test
    public void isRemoteUrlShouldBeTrueForHttp() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void isRemoteUrlShouldBeTrueForHttps() {
        GitConfig gitConfig = new GitConfig("https://url.test", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void isRemoteUrlShouldBeTrueForGitUrl() {
        GitConfig gitConfig = new GitConfig("git@github.com:test/sample.git", "user", "password", "master");

        assertThat(gitConfig.isRemoteUrl(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void hasCredentialsShouldBeTrueIfUrlAndPasswordAreProvided() {
        GitConfig gitConfig = new GitConfig("https://url.test", "user", "password", "master");

        assertThat(gitConfig.hasCredentials(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void hasCredentialsShouldBeFalseIfUrlIsEmptyWithValidPassword() {
        GitConfig gitConfig = new GitConfig("", "user", "password", "master");

        assertThat(gitConfig.hasCredentials(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void hasCredentialsShouldBeFalseWithValidUrlAndEmptyPassword() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "", "master");

        assertThat(gitConfig.hasCredentials(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void getEffectiveUrlShouldContainUserNameAndPasswordForRemoteUrlWithValidCredential() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl, is(equalTo("http://user:password@url.test")));
    }

    @Test
    public void getEffectiveUrlShouldNotContainUserNameAndPasswordForNonRemoteUrlWithValidCredential() {
        GitConfig gitConfig = new GitConfig("git@github.com:test/sample.git", "user", "password", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl, is(equalTo("git@github.com:test/sample.git")));
    }

    @Test
    public void getEffectiveUrlShouldNotContainUserNameAndPasswordForRemoteUrlWithoutCredential() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "", "master");

        String effectiveUrl = gitConfig.getEffectiveUrl();
        assertThat(effectiveUrl, is(equalTo("http://url.test")));
    }

    @Test
    public void getEffectiveBranchShouldReturnTheSpecifiedBranch() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "staging");

        String effectiveBranch = gitConfig.getEffectiveBranch();

        assertThat(effectiveBranch, is(equalTo("staging")));
    }

    @Test
    public void getEffectiveBranchShouldReturnMasterIfBranchisNotSpecified() {
        GitConfig gitConfig = new GitConfig("http://url.test", "user", "password", "");

        String effectiveBranch = gitConfig.getEffectiveBranch();

        assertThat(effectiveBranch, is(equalTo("master")));
    }

    @Test
    public void shouldBeAbleToGetIsRecursiveSubModuleUpdate() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        boolean recursiveSubModuleUpdate = gitConfig.isRecursiveSubModuleUpdate();

        assertThat(recursiveSubModuleUpdate, is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldGetUrl() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getUrl(), is(equalTo("http://url.test")));
    }

    @Test
    public void shouldGetUsername() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getUsername(), is(equalTo("username")));
    }

    @Test
    public void getPassword() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getPassword(), is(equalTo("password")));
    }

    @Test
    public void getBranch() {
        GitConfig gitConfig = new GitConfig("http://url.test", "username", "password", "branch");

        assertThat(gitConfig.getBranch(), is(equalTo("branch")));
    }
}