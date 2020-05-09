package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.model.requestHandlers.SCMConfigurationRequestHandler;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.ShallowClone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonUtils {
    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final int INTERNAL_ERROR_RESPONSE_CODE = 500;
    private static final Logger LOGGER = Logger.getLoggerFor(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static GoPluginApiResponse renderSuccessApiResponse(Object response) {
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    public static GoPluginApiResponse renderErrorApiResponse(Object response) {
        return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
    }

    public static GoPluginApiResponse renderErrorApiResponse(GoPluginApiRequest apiRequest, Throwable t) {
        LOGGER.error(apiRequest.requestName() + " failed", t);
        return renderJSON(INTERNAL_ERROR_RESPONSE_CODE,
                String.format("%s failed due to [%s], rootCause [%s]",
                        apiRequest.requestName(),
                        ExceptionUtils.getMessage(t),
                        ExceptionUtils.getRootCauseMessage(t)));
    }

    public static Map<String, String> parseScmConfiguration(GoPluginApiRequest apiRequest) {
        return parseJSON(apiRequest, "scm-configuration");
    }

    public static Object parseJSON(String json) {
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

    private static GitConfig toBaseGitConfig(Map<String, String> configuration) {
        return new GitConfig(
                StringUtils.trim(configuration.get(SCMConfigurationRequestHandler.CONFIG_URL)),
                StringUtils.trim(configuration.get(SCMConfigurationRequestHandler.CONFIG_USERNAME)),
                StringUtils.trim(configuration.get(SCMConfigurationRequestHandler.CONFIG_PASSWORD)),
                StringUtils.trim(configuration.get(SCMConfigurationRequestHandler.CONFIG_BRANCH)));
    }

    public static GitConfig toAgentGitConfig(GoPluginApiRequest apiRequest) {
        Map<String, String> configuration = parseScmConfiguration(apiRequest);
        GitConfig config = toBaseGitConfig(configuration);

        if ("true".equalsIgnoreCase(StringUtils.trim(configuration.get(SCMConfigurationRequestHandler.CONFIG_SHALLOW_CLONE)))) {
            config.setShallowClone(new ShallowClone());
        }
        return config;
    }

    public static GitConfig toServerSideGitConfig(GoPluginApiRequest apiRequest) {
        GitConfig config = toBaseGitConfig(parseScmConfiguration(apiRequest));
        config.setNoCheckout(true);
        return config;
    }

    public static List<String> getPaths(GoPluginApiRequest apiRequest) {
        return splitPaths(parseScmConfiguration(apiRequest).get(SCMConfigurationRequestHandler.CONFIG_PATHS));
    }

    static List<String> splitPaths(String paths) {
        return Stream.ofNullable(paths)
                .flatMap(rawPaths -> Arrays.stream(rawPaths.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }
}
