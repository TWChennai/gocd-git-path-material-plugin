package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.git.GitConfig;
import com.thoughtworks.go.scm.plugin.git.GitHelper;
import com.thoughtworks.go.scm.plugin.git.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.thoughtworks.go.scm.plugin.util.Validator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.go.scm.plugin.util.Validator.isValidURL;

public class SCMCheckConnectionRequestHandler implements RequestHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(SCMCheckConnectionRequestHandler.class);

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        GitConfig gitConfig = JsonUtils.toServerSideGitConfig(goPluginApiRequest);

        Map<String, Object> response = new HashMap<>();
        ArrayList<String> messages = new ArrayList<>();

        checkConnection(gitConfig, response, messages);

        if (response.get("status") == null) {
            response.put("status", "success");
            messages.add("Could connect to URL successfully");
        }
        response.put("messages", messages);
        return JsonUtils.renderSuccessApiResponse(response);
    }

    private void checkConnection(GitConfig gitConfig, Map<String, Object> response, ArrayList<String> messages) {
        LOGGER.debug("SCMCheckConnectionRequestHandler In handle");
        try {
            if (StringUtils.isBlank(gitConfig.getUrl())) {
                response.put("status", "failure");
                messages.add("URL is empty");
            } else if (gitConfig.getUrl().startsWith("/")) {
                if (!new File(gitConfig.getUrl()).exists()) {
                    response.put("status", "failure");
                    messages.add("Could not find Git repository");
                } else {
                    GitHelper gitHelper = new GitHelper(gitConfig, null);
                    gitHelper.checkConnection();
                }
            } else if (!isValidURL(gitConfig.getUrl())) {
                response.put("status", "failure");
                messages.add("Invalid URL format. Should match " + Validator.GIT_URL_REGEX);
            } else {
                GitHelper gitHelper = HelperFactory.git(gitConfig, null);
                gitHelper.checkConnection();
            }
        } catch (Exception e) {
            response.put("status", "failure");
            messages.add(e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName());
        }
    }
}
