package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

public class SCMConfigurationRequestHandler implements RequestHandler {

    public static final String CONFIG_URL = "url";
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_PATHS = "path";
    public static final String CONFIG_BRANCH = "branch";
    public static final String CONFIG_SHALLOW_CLONE = "shallow_clone";

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> response = new HashMap<>();
        response.put(CONFIG_URL, createField("URL", null, true, true, false, "0"));
        response.put(CONFIG_USERNAME, createField("Username", null, true, false, false, "1"));
        response.put(CONFIG_PASSWORD, createField("Password", null, true, false, true, "2"));
        response.put(CONFIG_PATHS, createField("Monitored Paths", null, true, true, false, "3"));
        response.put(CONFIG_BRANCH, createField("Branch", "master", true, false, false, "4"));
        response.put(CONFIG_SHALLOW_CLONE, createField("Shallow Clone", "false", false, false, false, "5"));
        return JsonUtils.renderSuccessApiResponse(response);
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isPartOfIdentity, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("part-of-identity", isPartOfIdentity);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }
}
