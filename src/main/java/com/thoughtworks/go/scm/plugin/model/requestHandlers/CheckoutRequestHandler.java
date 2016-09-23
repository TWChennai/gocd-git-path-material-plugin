package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.scm.plugin.jgit.GitHelper;
import com.thoughtworks.go.scm.plugin.jgit.JGitHelper;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(CheckoutRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        GitConfig gitConfig = GitConfig.create(apiRequest);

        String destinationFolder = (String) responseMap.get("destination-folder");
        Map<String, Object> revisionMap = (Map<String, Object>) responseMap.get("revision");
        String revision = (String) revisionMap.get("revision");

        LOGGER.warn("destination: " + destinationFolder + ". commit: " + revision);

        try {
            GitHelper git = JGitHelper.create(gitConfig, destinationFolder);
            git.cloneOrFetch();
            git.resetHard(revision);

            Map<String, Object> response = new HashMap<>();
            ArrayList<String> messages = new ArrayList<>();
            response.put("status", "success");
            messages.add("Checked out to revision " + revision);
            response.put("messages", messages);

            return JsonUtils.renderSuccessApiResponse(response);
        } catch (Throwable t) {
            LOGGER.warn("checkout: ", t);
            return JsonUtils.renderErrrorApiResponse(null);
        }
    }
}
