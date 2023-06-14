package com.thoughtworks.go.scm.plugin.git.cmd;

import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsoleTest {

    @TempDir
    File tempDir;

    @Test
    public void shouldRedactExceptionMessagesFromCommandLineExecuteException() {
        CommandLine command = CommandLine.parse("git https://secret:thing/here 'some thing'");
        List<String> secrets = List.of("secret", "thing");
        assertThatThrownBy(() -> Console.runOrBomb(command, tempDir, null, null, secrets))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("git exited with code 1 (git https://******:******/here \"some ******\" in")
                .hasMessageNotContainingAny(secrets.toArray(String[]::new))
                .hasNoCause();
    }

    @Test
    public void shouldRedactExceptionMessagesFromCommandLineRandomException() {
        CommandLine command = CommandLine.parse("badbinary https://secret:thing/here");
        List<String> secrets = List.of("secret", "thing");
        assertThatThrownBy(() -> Console.runOrBomb(command, tempDir, null, null, secrets))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot run program \"badbinary\"")
                .hasMessageContaining("https://******:******/here")
                .hasMessageNotContainingAny(secrets.toArray(String[]::new))
                .hasCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    public void shouldRedactExceptionMessagesFromException() {
        CommandLine command = CommandLine.parse("secretthing and some args secret");
        List<String> secrets = List.of("secret", "thing");
        assertThatThrownBy(() -> Console.runOrBomb(command, tempDir, null, null, secrets))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot run program \"************\"")
                .hasMessageContaining("************ and some args ******")
                .hasMessageNotContainingAny(secrets.toArray(String[]::new))
                .hasCauseExactlyInstanceOf(IOException.class);
    }
}