package com.thoughtworks.go.scm.plugin.model;

import lombok.Getter;

@Getter
public class ModifiedFile {
    private String fileName;
    private String action;

    public ModifiedFile(String fileName, String action) {
        this.fileName = fileName;
        this.action = action;
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

        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        return action != null ? action.equals(that.action) : that.action == null;
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
