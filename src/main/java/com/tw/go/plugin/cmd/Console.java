package com.tw.go.plugin.cmd;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.File;

public class Console {
    public static CommandLine createCommand(String... args) {
        CommandLine gitCmd = new CommandLine("git");
        gitCmd.addArguments(args, false);
        return gitCmd;
    }

    public static ConsoleResult runOrBomb(CommandLine commandLine, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr) {
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(stdOut, stdErr));
        if (workingDir != null) {
            executor.setWorkingDirectory(workingDir);
        }

        try {
            int exitCode = executor.execute(commandLine);
            return new ConsoleResult(exitCode, stdOut.output(), stdErr.output());
        } catch (Exception e) {
            throw new RuntimeException(getMessage(String.format("Exception (%s)", e.getMessage()), commandLine, workingDir), e);
        }
    }

    private static String getMessage(String type, CommandLine commandLine, File workingDir) {
        return String.format("%s Occurred: %s - %s", type, commandLine.toString(), workingDir);
    }
}
