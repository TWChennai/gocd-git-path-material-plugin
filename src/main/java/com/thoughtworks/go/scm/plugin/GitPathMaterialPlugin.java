package com.thoughtworks.go.scm.plugin;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.model.requestHandlers.RequestHandler;
import com.thoughtworks.go.scm.plugin.model.requestHandlers.RequestHandlerFactory;

import java.util.Arrays;
import java.util.List;

@Extension
public class GitPathMaterialPlugin implements GoPlugin {
    private static final String EXTENSION_NAME = "scm";
    private static final List<String> goSupportedVersions = Arrays.asList("1.0");
    private static Logger LOGGER = Logger.getLoggerFor(GitPathMaterialPlugin.class);

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // ignore
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        String requestName = apiRequest.requestName();
        LOGGER.info("Got request: " + requestName);
        LOGGER.debug("With request body: " + apiRequest.requestBody());
        RequestHandler requestHandler = RequestHandlerFactory.create(requestName);
        GoPluginApiResponse response = requestHandler.handle(apiRequest);
        LOGGER.info("Response code: " + response.responseCode());
        LOGGER.debug("With response body: " + response.responseBody());
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    @Load
    public void onLoad(PluginContext context) {
        LOGGER.info("Loading GitPathMaterialPlugin...");
        LOGGER.info("Type is {}", HelperFactory.determineType());
    }
}
