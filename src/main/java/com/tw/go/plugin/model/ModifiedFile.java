package com.tw.go.plugin.model;

import java.util.Objects;

public class ModifiedFile {
    private final String fileName;
    private final String action;

    public ModifiedFile(String fileName, String action) {
        this.fileName = fileName;
        this.action = action;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return String.format("ModifiedFile{fileName='%s', action='%s'}", fileName, action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModifiedFile that = (ModifiedFile) o;

        if (!Objects.equals(fileName, that.fileName)) return false;
        return Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
