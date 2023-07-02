package com.tw.go.plugin.cmd;

import java.util.List;

public interface StreamConsumer {
    void consumeLine(String line);

    List<String> asList();
}
