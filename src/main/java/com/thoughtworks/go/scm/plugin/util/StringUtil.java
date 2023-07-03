package com.thoughtworks.go.scm.plugin.util;

public class StringUtil {
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
