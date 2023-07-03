package com.thoughtworks.go.scm.plugin;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.model.requestHandlers.RequestHandlerFactory;

import java.util.Arrays;
import java.util.List;

@Extension
public class GitPathMaterialPlugin implements GoPlugin {
    private static final String EXTENSION_NAME = "scm";
    private static final List<String> goSupportedVersions = Arrays.asList("1.0");
    private static final Logger LOGGER = Logger.getLoggerFor(GitPathMaterialPlugin.class);

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // ignore
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        LOGGER.debug("Got request [{}] with body: {}",
                apiRequest.requestName(),
                apiRequest.requestBody());
        GoPluginApiResponse response = RequestHandlerFactory.create(apiRequest.requestName()).handle(apiRequest);
        LOGGER.debug("Responding to [{}] with [{}] and body: {}",
                apiRequest.requestName(),
                response.responseCode(),
                response.responseBody());
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }
}
