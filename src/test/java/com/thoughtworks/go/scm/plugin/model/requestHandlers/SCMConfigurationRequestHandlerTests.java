package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SCMConfigurationRequestHandlerTests {

    @Mock
    private GoPluginApiRequest apiRequest;

    private final RequestHandler requestHandler = new SCMConfigurationRequestHandler();

    @Test
    public void shouldReturnSuccessJsonResponseForScmConfigurationRequest() {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        assertThat(apiResponse.responseCode()).isEqualTo(200);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainUrlField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, Object> urlField = (Map<String, Object>) response.get("url");

        assertThat(urlField).containsEntry("display-name", "URL");
        assertThat(urlField).containsEntry("part-of-identity", true);
        assertThat(urlField).containsEntry("required", true);
        assertThat(urlField).containsEntry("secure", false);
        assertThat(urlField).containsEntry("display-order", "0");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainBranchField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, Object> urlField = (Map<String, Object>) response.get("branch");

        assertThat(urlField).containsEntry("display-name", "Branch");
        assertThat(urlField).containsEntry("default-value", "master");
        assertThat(urlField).containsEntry("part-of-identity", true);
        assertThat(urlField).containsEntry("required", false);
        assertThat(urlField).containsEntry("secure", false);
        assertThat(urlField).containsEntry("display-order", "4");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainPathField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, Object> urlField = (Map<String, Object>) response.get("path");

        assertThat(urlField).containsEntry("display-name", "Monitored Paths");
        assertThat(urlField).containsEntry("part-of-identity", true);
        assertThat(urlField).containsEntry("required", true);
        assertThat(urlField).containsEntry("secure", false);
        assertThat(urlField).containsEntry("display-order", "3");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainShallowCloneField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, Object> urlField = (Map<String, Object>) response.get("shallow_clone");

        assertThat(urlField).containsEntry("display-name", "Shallow Clone");
        assertThat(urlField).containsEntry("part-of-identity", false);
        assertThat(urlField).containsEntry("required", false);
        assertThat(urlField).containsEntry("secure", false);
        assertThat(urlField).containsEntry("display-order", "5");
    }
}
