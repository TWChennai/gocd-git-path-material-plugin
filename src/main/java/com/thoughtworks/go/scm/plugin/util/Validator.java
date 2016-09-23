package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.scm.plugin.model.GitConfig;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.util.Map;

public class Validator {

    public static boolean isValidURL(String url) {
        return new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS).isValid(url);
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
