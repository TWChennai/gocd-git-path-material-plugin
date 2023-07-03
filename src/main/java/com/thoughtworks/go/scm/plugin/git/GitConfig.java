package com.thoughtworks.go.scm.plugin.git;

import com.thoughtworks.go.scm.plugin.util.StringUtil;

import java.util.Objects;
import java.util.Optional;

public class GitConfig {
    private String url;
    private String username;
    private String password;
    private String branch;
    private boolean subModule = false;
    private boolean recursiveSubModuleUpdate = true;
    private boolean noCheckout = false;
    private Optional<ShallowClone> shallowClone = Optional.empty();

    public GitConfig(String url) {
        this.url = url;
    }

    public GitConfig(String url, String username, String password, String branch) {
        this(url, username, password, branch, true, false);
    }

    public GitConfig(String url, String username, String password, String branch, boolean recursiveSubModuleUpdate, boolean shallowClone) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.branch = branch;
        this.recursiveSubModuleUpdate = recursiveSubModuleUpdate;
        this.shallowClone = shallowClone ? Optional.of(new ShallowClone()) : Optional.empty();
    }

    public boolean isRemoteUrl() {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    public boolean hasCredentials() {
        return !StringUtil.isBlank(url) && !StringUtil.isBlank(password);
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemoteBranch() {
        return String.format("origin/%s", getEffectiveBranch());
    }

    public String getEffectiveBranch() {
        return StringUtil.isBlank(branch) ? "master" : branch;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isSubModule() {
        return subModule;
    }

    public void setSubModule(boolean subModule) {
        this.subModule = subModule;
    }

    public boolean isRecursiveSubModuleUpdate() {
        return recursiveSubModuleUpdate;
    }

    public void setRecursiveSubModuleUpdate(boolean recursiveSubModuleUpdate) {
        this.recursiveSubModuleUpdate = recursiveSubModuleUpdate;
    }

    public boolean isShallowClone() {
        return shallowClone.isPresent();
    }

    public Optional<ShallowClone> getShallowClone() {
        return shallowClone;
    }

    public void setShallowClone(ShallowClone shallowClone) {
        this.shallowClone = Optional.of(shallowClone);
    }

    public boolean isNoCheckout() {
        return noCheckout;
    }

    public void setNoCheckout(boolean noCheckout) {
        this.noCheckout = noCheckout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitConfig gitConfig = (GitConfig) o;
        return subModule == gitConfig.subModule &&
                recursiveSubModuleUpdate == gitConfig.recursiveSubModuleUpdate &&
                noCheckout == gitConfig.noCheckout &&
                Objects.equals(url, gitConfig.url) &&
                Objects.equals(username, gitConfig.username) &&
                Objects.equals(password, gitConfig.password) &&
                Objects.equals(branch, gitConfig.branch) &&
                shallowClone.equals(gitConfig.shallowClone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, branch, subModule, recursiveSubModuleUpdate, noCheckout, shallowClone);
    }
}
