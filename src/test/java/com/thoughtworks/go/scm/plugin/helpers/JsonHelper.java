package com.thoughtworks.go.scm.plugin.helpers;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonHelper {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getResponse(GoPluginApiResponse apiResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(apiResponse.responseBody(), Map.class);
    }

    public static String toJson(Object object, Type type) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper.writeValueAsString(object);
    }

    public static String toJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
