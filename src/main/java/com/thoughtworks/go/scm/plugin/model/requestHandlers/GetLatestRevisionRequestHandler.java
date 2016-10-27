package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.jgit.GitHelper;
import com.thoughtworks.go.scm.plugin.jgit.JGitHelper;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.Revision;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.Validator;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class GetLatestRevisionRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(GetLatestRevisionRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        GitConfig gitConfig = GitConfig.create(apiRequest);
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        String flyweightFolder = (String) responseMap.get("flyweight-folder");

        Map<String, Object> fieldMap = new HashMap<>();
        Validator.validateUrl(gitConfig, fieldMap);
        if (!fieldMap.isEmpty()) {
            String message = (String) fieldMap.get("message");
            LOGGER.error(String.format("Invalid url: %s", message));
            return JsonUtils.renderErrrorApiResponse(message);
        }

        try {
            GitHelper git = JGitHelper.create(gitConfig, flyweightFolder);
            git.cloneOrFetch();
            Map<String, String> configuration = JsonUtils.parseScmConfiguration(apiRequest);
            final Revision revision = git.getLatestRevision(configuration.get("path"));

            LOGGER.debug(String.format("Fetching latestRevision for path %s", configuration.get("path")));

            if (revision == null) {
                return JsonUtils.renderSuccessApiResponse(null);
            } else {
                Map<String, Object> response = new HashMap<String, Object>() {{
                    put("revision", revision.getRevisionMap());
                }};
                return JsonUtils.renderSuccessApiResponse(response);
            }
        } catch (Throwable t) {
            LOGGER.error("get latest revision: ", t);
            return JsonUtils.renderErrrorApiResponse(getRootCauseMessage(t));
        }
    }
}
