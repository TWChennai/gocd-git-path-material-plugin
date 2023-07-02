package com.thoughtworks.go.scm.plugin;


import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.cmd.InMemoryConsumer;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.git.GitCmdHelper;
import com.tw.go.plugin.model.GitConfig;

import java.io.File;

public class HelperFactory {

    public static GitHelper git(GitConfig gitConfig, File workingDirectory, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        return new GitCmdHelper(gitConfig, workingDirectory, stdOut, stdErr);
    }

    public static GitHelper git(GitConfig gitConfig, File workingDirectory) {
        return git(gitConfig,
                workingDirectory,
                new ProcessOutputStreamConsumer(new InMemoryConsumer()),
                new ProcessOutputStreamConsumer(new InMemoryConsumer())
        );
    }
}
