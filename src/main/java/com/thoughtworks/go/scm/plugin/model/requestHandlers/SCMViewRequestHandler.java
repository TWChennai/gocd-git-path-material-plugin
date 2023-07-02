package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SCMViewRequestHandler implements RequestHandler {
    public static final String PLUGIN_NAME = "Git Path";

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> response = new HashMap<>();
        response.put("displayValue", PLUGIN_NAME);
        try {
            response.put("template", resourceAsString());
            return JsonUtils.renderSuccessApiResponse(response);
        } catch (IOException e) {
            String message = String.format("Failed to find template: %s", e.getMessage());
            return JsonUtils.renderErrorApiResponse(message);
        }
    }

    private String resourceAsString() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/scm.template.html"))) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
