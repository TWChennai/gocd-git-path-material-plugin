package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.scm.plugin.model.GitConfig;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public class Validator {

    public static final String GIT_URL_REGEX = "^(?:git|ssh|https?|git@[\\w-.]+[\\w]+\\.[\\w]+):(?://)?[\\w.@:/~_-]+\\.git(?:/?|#[\\d\\w.\\-_]+?)$";
    public static final Pattern pattern = Pattern.compile(GIT_URL_REGEX);

    public static boolean isValidURL(String url) {
        return pattern.matcher(url).matches();
    }

    public static void validateUrl(GitConfig gitConfig, Map<String, Object> fieldMap) {
        if (StringUtils.isEmpty(gitConfig.getUrl())) {
            fieldMap.put("key", "url");
            fieldMap.put("message", "URL is a required field");
        } else {
            if (gitConfig.getUrl().startsWith("/")) {
                if (!new File(gitConfig.getUrl()).exists()) {
                    fieldMap.put("key", "url");
                    fieldMap.put("message", "Invalid URL. Directory does not exist");
                }
            } else {
                if (!isValidURL(gitConfig.getUrl())) {
                    fieldMap.put("key", "url");
                    fieldMap.put("message", "Invalid URL format");
                }
            }
        }
    }
}
