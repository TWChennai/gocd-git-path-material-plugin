package com.thoughtworks.go.scm.plugin.git.cmd;

import org.apache.commons.exec.LogOutputStream;

import java.util.List;

public class ProcessOutputStreamConsumer extends LogOutputStream {
    private final StreamConsumer streamConsumer;

    public ProcessOutputStreamConsumer(StreamConsumer streamConsumer) {
        this.streamConsumer = streamConsumer;
    }

    public void consumeLine(String line) {
        streamConsumer.consumeLine(line);
    }

    public List<String> output() {
        return streamConsumer.asList();
    }

    @Override
    protected void processLine(String line, int level) {
        consumeLine(line);
    }
}
