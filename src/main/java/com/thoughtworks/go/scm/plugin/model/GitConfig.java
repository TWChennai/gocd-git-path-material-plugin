package com.thoughtworks.go.scm.plugin.model;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.StringUtils;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class GitConfig {

    private String url;
    private String username;
    private String password;
    private String branch;
    private boolean subModule = false;
    private boolean recursiveSubModuleUpdate = true;
    private boolean shallowClone = false;

    public static GitConfig create(GoPluginApiRequest apiRequest) {
        Map<String, String> configuration = JsonUtils.parseScmConfiguration(apiRequest);
        return new GitConfig(configuration.get("url"), configuration.get("username"), configuration.get("password"), configuration.get("branch"));
    }

    public GitConfig(String url, String username, String password, String branch) {
        this(url, username, password, branch, true, false);
    }

    private GitConfig(String url, String username, String password, String branch, boolean recursiveSubModuleUpdate, boolean shallowClone) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.branch = branch;
        this.recursiveSubModuleUpdate = recursiveSubModuleUpdate;
        this.shallowClone = shallowClone;
    }

    public boolean isRemoteUrl() {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    public boolean hasCredentials() {
        return !StringUtils.isEmpty(url) && !StringUtils.isEmpty(password);
    }

    public String getEffectiveUrl() {
        if (isRemoteUrl() && hasCredentials()) {
            return getUrlWithCredentials();
        }
        return getUrl();
    }

    public String getUrlWithCredentials() {
        String[] parts = url.split("://");
        return String.format("%s://%s:%s@%s", parts[0], username, password, parts[1]);
    }

    public String getEffectiveBranch() {
        return StringUtils.isEmpty(branch) ? "master" : branch;
    }

    public boolean isRecursiveSubModuleUpdate() {
        return recursiveSubModuleUpdate;
    }
}
