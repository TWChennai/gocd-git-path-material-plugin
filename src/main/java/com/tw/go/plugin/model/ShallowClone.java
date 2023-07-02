package com.tw.go.plugin.model;

import java.util.Objects;

public class ShallowClone {
    private final int defaultCommitsDepth;
    private final int additionalFetchDepth;

    public ShallowClone() {
        this(2, 100);
    }

    public ShallowClone(int defaultCommitsDepth, int additionalFetchDepth) {
        this.defaultCommitsDepth = defaultCommitsDepth;
        this.additionalFetchDepth = additionalFetchDepth;
        if (additionalFetchDepth <= defaultCommitsDepth) {
            throw new IllegalArgumentException(String.format("Additional fetch depth must be greater than default (%s)", defaultCommitsDepth));
        }
    }

    public int getAdditionalFetchDepth() {
        return additionalFetchDepth;
    }

    public int getDefaultCommitsDepth() {
        return defaultCommitsDepth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShallowClone that = (ShallowClone) o;
        return defaultCommitsDepth == that.defaultCommitsDepth &&
                additionalFetchDepth == that.additionalFetchDepth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultCommitsDepth, additionalFetchDepth);
    }
}
