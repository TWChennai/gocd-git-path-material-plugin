package com.thoughtworks.go.scm.plugin.util;

import org.junit.Test;

import static com.thoughtworks.go.scm.plugin.util.Validator.isValidURL;
import static org.junit.Assert.*;


public class ValidatorTest {
    @Test
    public void shouldValidateUrl() throws Exception {
        String[] validURLs = new String[]{
                "git://github.com/ember-cli/ember-cli.git#v0.1.0",
                "git://github.com/ember-cli/ember-cli.git#ff786f9f",
                "git://github.com/ember-cli/ember-cli.git#master",
                "git://github.com/ember-cli/ember-cli.git#gh-pages",
                "git://github.com/ember-cli/ember-cli.git#quick_fix",
                "git://github.com/ember-cli/ember-cli.git#Quick-Fix",
                "git@github.com:user/project.git",
                "git@github.com:user/some-project.git",
                "git@github.com:user/some_project.git",
                "https://github.com/user/project.git",
                "http://github.com/user/project.git",
                "git@192.168.101.127:user/project.git",
                "https://192.168.101.127/user/project.git",
                "http://192.168.101.127/user/project.git",
                "ssh://user@host.xz:port/path/to/repo.git/",
                "ssh://user@host.xz/path/to/repo.git/",
                "ssh://host.xz:port/path/to/repo.git/",
                "ssh://host.xz/path/to/repo.git/",
                "ssh://user@host.xz/path/to/repo.git/",
                "ssh://host.xz/path/to/repo.git/",
                "ssh://user@host.xz/~user/path/to/repo.git/",
                "ssh://host.xz/~user/path/to/repo.git/",
                "ssh://user@host.xz/~/path/to/repo.git",
                "ssh://host.xz/~/path/to/repo.git",
                "git://host.xz/path/to/repo.git/",
                "git://host.xz/~user/path/to/repo.git/",
                "http://host.xz/path/to/repo.git/",
                "https://host.xz/path/to/repo.git/",
                "git@github.com:user/some-project.git",
                "git@github.com:user/some_project.git"
        };

        String[] invalidURLs = new String[]{
                "git@github.com:user/some_project.gitfoo",
                "git@github.com:user/some_project.git/foo",
                "/path/to/repo.git/",
                "path/to/repo.git/",
                "~/path/to/repo.git",
                "file:///path/to/repo.git/",
                "file://~/path/to/repo.git/",
                "user@host.xz:/path/to/repo.git/",
                "host.xz:/path/to/repo.git/",
                "user@host.xz:~user/path/to/repo.git/",
                "host.xz:~user/path/to/repo.git/",
                "user@host.xz:path/to/repo.git",
                "host.xz:path/to/repo.git",
                "rsync://host.xz/path/to/repo.git/"
        };

        for (String validUrl : validURLs) assertTrue("Failing for " + validUrl, isValidURL(validUrl));
        for (String invaildUrl : invalidURLs) assertFalse("Failing for " + invaildUrl, isValidURL(invaildUrl));
    }

}