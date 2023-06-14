package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.git.GitConfig;
import com.thoughtworks.go.scm.plugin.git.GitHelper;
import com.thoughtworks.go.scm.plugin.git.HelperFactory;
import com.thoughtworks.go.scm.plugin.git.cmd.InMemoryConsumer;
import com.thoughtworks.go.scm.plugin.git.cmd.ProcessOutputStreamConsumer;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckoutRequestHandler implements RequestHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(CheckoutRequestHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public GoPluginApiResponse handle(GoPluginApiRequest apiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) JsonUtils.parseJSON(apiRequest.requestBody());
        GitConfig gitConfig = JsonUtils.toAgentGitConfig(apiRequest);

        String destinationFolder = (String) responseMap.get("destination-folder");
        Map<String, Object> revisionMap = (Map<String, Object>) responseMap.get("revision");
        String revision = (String) revisionMap.get("revision");

        LOGGER.debug(String.format("destination: %s , commit: %s", destinationFolder, revision));

        try {
            List<String> messages = new ArrayList<>();
            messages.add(String.format("Start updating %s to revision %s from %s", destinationFolder, revision, gitConfig.getUrl()));
            ProcessOutputStreamConsumer outputConsumer = new ProcessOutputStreamConsumer(new InMemoryConsumer());
            GitHelper git = HelperFactory.git(gitConfig, new File(destinationFolder), outputConsumer, outputConsumer);
            git.cloneOrFetch();
            git.resetHard(revision);

            messages.addAll(outputConsumer.output());

            return JsonUtils.renderSuccessApiResponse(Map.of(
                    "status", "success",
                    "messages", messages)
            );
        } catch (Throwable t) {
            return JsonUtils.renderErrorApiResponse(apiRequest, t, gitConfig.redactables());
        }
    }
}
