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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class LatestRevisionSinceRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(LatestRevisionSinceRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());

        GitConfig gitConfig = JsonUtils.toServerSideGitConfig(apiRequest);

        File flyweightFolder = new File((String) responseMap.get("flyweight-folder"));
        Map<String, Object> previousRevisionMap = (Map<String, Object>) responseMap.get("previous-revision");
        String previousRevision = (String) previousRevisionMap.get("revision");
        LOGGER.debug(String.format("flyweight: %s, previous commit: %s", flyweightFolder, previousRevision));

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
            List<Revision> newerRevisions = git.getRevisionsSince(previousRevision, paths);

            LOGGER.debug(String.format("Fetching newerRevisions for paths %s", paths));

            if (newerRevisions.isEmpty()) {
                return JsonUtils.renderSuccessApiResponse(null);
            } else {
                LOGGER.debug(String.format("New commits: %s", newerRevisions.size()));
                return JsonUtils.renderSuccessApiResponse(
                        Map.of("revisions",
                                newerRevisions
                                        .stream()
                                        .map(RevisionUtil::toMap)
                                        .collect(toList())));
            }
        } catch (Throwable t) {
            return JsonUtils.renderErrorApiResponse(apiRequest, t);
        }
    }
}
