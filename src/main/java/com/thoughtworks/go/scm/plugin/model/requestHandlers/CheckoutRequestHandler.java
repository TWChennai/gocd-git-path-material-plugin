package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.model.GitConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

public class CheckoutRequestHandler implements RequestHandler {
    private static Logger LOGGER = Logger.getLoggerFor(CheckoutRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        GitConfig gitConfig = JsonUtils.toGitConfig(apiRequest);

        String destinationFolder = (String) responseMap.get("destination-folder");
        Map<String, Object> revisionMap = (Map<String, Object>) responseMap.get("revision");
        String revision = (String) revisionMap.get("revision");

        LOGGER.debug(String.format("destination: %s , commit: %s", destinationFolder, revision));

        try {
            GitHelper git = HelperFactory.git(gitConfig, new File(destinationFolder));
            git.cloneOrFetch();
            git.resetHard(revision);

            Map<String, Object> response = new HashMap<>();
            ArrayList<String> messages = new ArrayList<>();

            response.put("status", "success");
            messages.add(String.format("Checked out to revision %s", revision));
            response.put("messages", messages);
            return JsonUtils.renderSuccessApiResponse(response);
        } catch (Throwable t) {
            LOGGER.error("checkout: ", t);
            return JsonUtils.renderErrrorApiResponse(getRootCauseMessage(t));
        }
    }
}
