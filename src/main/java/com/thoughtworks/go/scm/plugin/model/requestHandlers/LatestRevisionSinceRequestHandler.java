package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.jgit.GitHelper;
import com.thoughtworks.go.scm.plugin.jgit.JGitHelper;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.Revision;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.ListUtils;
import com.thoughtworks.go.scm.plugin.util.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class LatestRevisionSinceRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(LatestRevisionSinceRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());

        GitConfig gitConfig = GitConfig.create(apiRequest);

        String flyweightFolder = (String) responseMap.get("flyweight-folder");
        Map<String, Object> previousRevisionMap = (Map<String, Object>) responseMap.get("previous-revision");
        String previousRevision = (String) previousRevisionMap.get("revision");
        LOGGER.warn("flyweight: " + flyweightFolder + ". previous commit: " + previousRevision);

        Map<String, Object> fieldMap = new HashMap<>();
        Validator.validateUrl(gitConfig, fieldMap);
        if (!fieldMap.isEmpty()) {
            LOGGER.warn("invalid url");
            return JsonUtils.renderErrrorApiResponse(null);
        }

        try {
            GitHelper git = JGitHelper.create(gitConfig, flyweightFolder);
            git.cloneOrFetch();
            Map<String, String> configuration = JsonUtils.parseScmConfiguration(apiRequest);
            LOGGER.info("Fetching newerRevisions for path " + configuration.get("path"));
            List<Revision> newerRevisions = git.getRevisionsSince(previousRevision, configuration.get("path"));

            if (ListUtils.isEmpty(newerRevisions)) {
                return JsonUtils.renderSuccessApiResponse(null);
            } else {
                LOGGER.warn("new commits: " + newerRevisions.size());

                Map<String, Object> response = new HashMap<>();
                List<Map> revisions = new ArrayList<>();
                for (Revision revision : newerRevisions) {
                    Map<String, Object> revisionMap = revision.getRevisionMap();
                    revisions.add(revisionMap);
                }
                response.put("revisions", revisions);
                return JsonUtils.renderSuccessApiResponse(response);
            }
        } catch (Throwable t) {
            LOGGER.warn("get latest revisions since: ", t);
            return JsonUtils.renderErrrorApiResponse(getRootCauseMessage(t));
        }
    }
}
