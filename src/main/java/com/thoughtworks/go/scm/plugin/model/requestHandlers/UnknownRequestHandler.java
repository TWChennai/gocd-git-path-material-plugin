package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public class UnknownRequestHandler implements RequestHandler {

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        return JsonUtils.renderErrorApiResponse(apiRequest, null);
    }
}
