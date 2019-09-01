package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.model.GitConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final int INTERNAL_ERROR_RESPONSE_CODE = 500;
    private static final Logger LOGGER = Logger.getLoggerFor(JsonUtils.class);


    public static GoPluginApiResponse renderSuccessApiResponse(Object response) {
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    public static GoPluginApiResponse renderErrrorApiResponse(Object response) {
        return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
    }

    public static Map<String, String> parseScmConfiguration(GoPluginApiRequest apiRequest) {
        return parseJSON(apiRequest, "scm-configuration");
    }

    public static Object parseJSON(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> parseJSON(GoPluginApiRequest apiRequest, String key) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        return keyValuePairs(responseMap, key);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
        Map<String, String> keyValuePairs = new HashMap<>();
        Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
        for (String field : fieldsMap.keySet()) {
            Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
            String value = (String) fieldProperties.get("value");
            keyValuePairs.put(field, value);
        }
        return keyValuePairs;
    }

    private static GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        String tempJson = null;
        try {
            tempJson = response == null ? null : new ObjectMapper().writeValueAsString(response);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.debug("GoPluginApiResponse: " + tempJson);

        final String json = tempJson;
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }

    public static GitConfig toGitConfig(GoPluginApiRequest apiRequest) {
        Map<String, String> configuration = parseScmConfiguration(apiRequest);
        return new GitConfig(configuration.get("url"), configuration.get("username"), configuration.get("password"), configuration.get("branch"));
    }
}
