package com.thoughtworks.go.scm.plugin.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class StringUtil {
    private static final String REDACT_REPLACEMENT = "******";

    /**
     * Replaces passed redactables within the redactable text, in the order specified. Earlier redactables are
     * seen as higher priority. The order is important if one redactable is a substring of another.
     */
    public static String replaceSecretText(String redactableText, List<String> redactables) {
        return Optional.ofNullable(redactables).orElse(List.of()).stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .reduce(redactableText, (partiallyRedacted, secret) -> partiallyRedacted.replaceAll(secret, REDACT_REPLACEMENT));
    }
}
