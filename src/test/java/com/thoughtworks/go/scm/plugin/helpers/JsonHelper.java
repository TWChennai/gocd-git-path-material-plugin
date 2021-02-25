package com.thoughtworks.go.scm.plugin.helpers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.io.IOException;
import java.util.Map;

public class JsonHelper {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getResponse(GoPluginApiResponse apiResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(apiResponse.responseBody(), Map.class);
    }

    public static String toJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper.writeValueAsString(object);
    }
}
