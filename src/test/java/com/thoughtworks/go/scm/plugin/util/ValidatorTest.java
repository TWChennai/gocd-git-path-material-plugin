package com.thoughtworks.go.scm.plugin.util;

import org.junit.Test;

import java.util.List;

import static com.thoughtworks.go.scm.plugin.util.Validator.isValidURL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ValidatorTest {
    @Test
    public void shouldValidateUrl() {
        List.of(
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
                "https://github.com/user/project",
                "http://github.com/user/project",
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
                "git@github.com:user/some_project.git",
                "git@git.my-org.com:user/some_project.git",
                "git@git.my01-org.com:user/some_project.git"
        ).forEach(url -> assertTrue("Expected to be valid: " + url, isValidURL(url)));

        List.of(
                "git@git.my01-.com:user/some_project.git",
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
        ).forEach(url -> assertFalse("Expected to be invalid: " + url, isValidURL(url)));
    }
}
