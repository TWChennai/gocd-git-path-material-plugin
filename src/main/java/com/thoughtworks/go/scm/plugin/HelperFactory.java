package com.thoughtworks.go.scm.plugin;


import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.cmd.InMemoryConsumer;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.git.GitCmdHelper;
import com.tw.go.plugin.jgit.JGitHelper;
import com.tw.go.plugin.model.GitConfig;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class HelperFactory {
    private enum HelperType {CMD, JGIT}

    private static final Logger LOGGER = Logger.getLoggerFor(HelperFactory.class);
    private static final AtomicReference<HelperType> HELPER_TYPE = new AtomicReference<>(null);

    public static GitHelper git(GitConfig gitConfig, File workingDirectory, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        if (determineType() == HelperType.CMD) {
            return new GitCmdHelper(gitConfig, workingDirectory, stdOut, stdErr);
        } else {
            return new JGitHelper(gitConfig, workingDirectory, stdOut, stdErr);
        }
    }

    public static GitHelper git(GitConfig gitConfig, File workingDirectory) {
        return git(gitConfig,
                workingDirectory,
                new ProcessOutputStreamConsumer(new InMemoryConsumer()),
                new ProcessOutputStreamConsumer(new InMemoryConsumer())
        );
    }

    public static HelperType determineType() {
        HelperType helperType = HELPER_TYPE.get();
        if (helperType == null) {
            try {
                LOGGER.info("Command line git found [{}]", new GitCmdHelper(null, null).version());
                helperType = HelperType.CMD;
            } catch (Exception e) {
                LOGGER.info("No command line git found; will continue with JGit [{}]", e.toString());
                helperType = HelperType.JGIT;
            }
            HELPER_TYPE.compareAndSet(null, helperType);
        }
        return helperType;
    }

}
