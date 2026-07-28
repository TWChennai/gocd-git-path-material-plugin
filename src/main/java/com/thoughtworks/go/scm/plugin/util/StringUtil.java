package com.thoughtworks.go.scm.plugin.util;

import java.util.List;
import java.util.Optional;

public class StringUtil {
    private static final String REDACT_REPLACEMENT = "******";

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Replaces passed redactables within the redactable text, in the order specified. Earlier redactables are
     * seen as higher priority. The order is important if one redactable is a substring of another.
     */
    public static String replaceSecretText(String redactableText, List<String> redactables) {
        return Optional.ofNullable(redactables).orElse(List.of()).stream()
                .filter(StringUtil::isNotBlank)
                .map(String::trim)
                .reduce(redactableText, (partiallyRedacted, secret) -> partiallyRedacted.replaceAll(secret, REDACT_REPLACEMENT));
    }
}
