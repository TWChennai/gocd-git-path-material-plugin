package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.Validator;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetLatestRevisionRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(GetLatestRevisionRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        GitConfig gitConfig = JsonUtils.toServerSideGitConfig(apiRequest);
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        File flyweightFolder = new File((String) responseMap.get("flyweight-folder"));

        Map<String, Object> fieldMap = new HashMap<>();
        Validator.validateUrl(gitConfig, fieldMap);
        if (!fieldMap.isEmpty()) {
            String message = (String) fieldMap.get("message");
            LOGGER.error(String.format("Invalid url: %s", message));
            return JsonUtils.renderErrorApiResponse(message);
        }

        try {
            GitHelper git = HelperFactory.git(gitConfig, flyweightFolder);
            git.cloneOrFetch();
            final List<String> paths = JsonUtils.getPaths(apiRequest);
            final Revision revision = git.getLatestRevision(paths);

            LOGGER.debug(String.format("Fetching latestRevision for paths %s", paths));

            if (revision == null) {
                return JsonUtils.renderSuccessApiResponse(null);
            } else {
                return JsonUtils.renderSuccessApiResponse(Map.of("revision", RevisionUtil.toMap(revision)));
            }
        } catch (Throwable t) {
            return JsonUtils.renderErrorApiResponse(apiRequest, t);
        }
    }
}
