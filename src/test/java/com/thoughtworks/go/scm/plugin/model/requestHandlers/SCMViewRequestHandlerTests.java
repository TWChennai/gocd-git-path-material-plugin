package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SCMViewRequestHandlerTests {

    @Test
    @SuppressWarnings("ConstantConditions")
    public void shouldReturnSuccessJsonResponseForScmViewRequest() throws Exception {
        String expectedTemplate = Files.readString(Path.of(getClass().getResource("/scm.template.html").toURI()), StandardCharsets.UTF_8);
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        RequestHandler requestHandler = new SCMViewRequestHandler();

        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        assertThat(apiResponse.responseCode()).isEqualTo(200);
        assertThat(response).containsEntry("displayValue", SCMViewRequestHandler.PLUGIN_NAME);
        assertThat(response).containsEntry("template", expectedTemplate);
    }
}
