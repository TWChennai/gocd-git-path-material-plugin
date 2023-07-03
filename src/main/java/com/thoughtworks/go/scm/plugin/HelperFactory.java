package com.thoughtworks.go.scm.plugin;


import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.git.GitHelper;
import com.tw.go.plugin.model.GitConfig;

import java.io.File;

public class HelperFactory {

    public static GitHelper git(GitConfig gitConfig, File workingDirectory, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        return new GitHelper(gitConfig, workingDirectory, stdOut, stdErr);
    }

    public static GitHelper git(GitConfig gitConfig, File workingDirectory) {
        return new GitHelper(gitConfig, workingDirectory);
    }
}
