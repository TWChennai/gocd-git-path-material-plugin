package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.scm.plugin.FieldValidator;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.Validator;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCMValidationRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        final GitConfig gitConfig = GitConfig.create(apiRequest);
        List<Map<String, Object>> response = new ArrayList<>();

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                Validator.validateUrl(gitConfig, fieldValidation);
            }
        });

        return JsonUtils.renderSuccessApiResponse(response);
    }

    private void validate(List<Map<String, Object>> response, FieldValidator fieldValidator) {
        Map<String, Object> fieldValidation = new HashMap<>();
        fieldValidator.validate(fieldValidation);
        if (!fieldValidation.isEmpty()) {
            response.add(fieldValidation);
        }
    }
}
