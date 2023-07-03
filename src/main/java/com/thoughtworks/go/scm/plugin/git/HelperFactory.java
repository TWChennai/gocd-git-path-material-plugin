package com.thoughtworks.go.scm.plugin.git;


import com.thoughtworks.go.scm.plugin.git.cmd.ProcessOutputStreamConsumer;

import java.io.File;

public class HelperFactory {

    public static GitHelper git(GitConfig gitConfig, File workingDirectory, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        return new GitHelper(gitConfig, workingDirectory, stdOut, stdErr);
    }

    public static GitHelper git(GitConfig gitConfig, File workingDirectory) {
        return new GitHelper(gitConfig, workingDirectory);
    }
}
