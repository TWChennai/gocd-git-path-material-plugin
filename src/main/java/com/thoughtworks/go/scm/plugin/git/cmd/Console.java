package com.thoughtworks.go.scm.plugin.git.cmd;

import com.thoughtworks.go.scm.plugin.util.StringUtil;
import org.apache.commons.exec.*;
import org.apache.commons.exec.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Console {
    public static CommandLine createCommand(String... args) {
        CommandLine gitCmd = new CommandLine("git");
        gitCmd.addArguments(args, false);
        return gitCmd;
    }

    public static ConsoleResult runOrBomb(CommandLine commandLine, File workingDir, ProcessOutputStreamConsumer stdOut, ProcessOutputStreamConsumer stdErr, List<String> redactables) {
        Executor executor = DefaultExecutor
                .builder()
                .setExecuteStreamHandler(new PumpStreamHandler(stdOut, stdErr))
                .setWorkingDirectory(workingDir)
                .get();

        try {
            int exitCode = executor.execute(commandLine);
            return new ConsoleResult(exitCode, stdOut.output(), stdErr.output());
        } catch (ExecuteException e) {
            throw new RuntimeException(String.format("%s exited with code %d (%s%s)",
                    StringUtil.replaceSecretText(commandLine.getExecutable(), redactables),
                    e.getExitValue(),
                    StringUtil.replaceSecretText(niceCommandLine(commandLine), redactables),
                    workingDir == null ? "" : " in " + workingDir));
        } catch (Exception e) {
            throw new RuntimeException(String.format("%s failed: %s (%s%s)",
                    StringUtil.replaceSecretText(commandLine.getExecutable(), redactables),
                    StringUtil.replaceSecretText(e.getMessage(), redactables),
                    StringUtil.replaceSecretText(niceCommandLine(commandLine), redactables),
                    workingDir == null ? "" : " in " + workingDir), e);
        }
    }

    private static String niceCommandLine(CommandLine commandLine) {
        return Stream.concat(Stream.of(commandLine.getExecutable()), Arrays.stream(commandLine.getArguments()))
                .map(StringUtils::quoteArgument)
                .collect(Collectors.joining(" "));
    }
}
