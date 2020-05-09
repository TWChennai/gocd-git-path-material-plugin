package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Mockito.mock;


public class SCMConfigurationRequestHandlerTests {

    private RequestHandler requestHandler;
    private GoPluginApiRequest apiRequest;

    @Before
    public void setUp() {
        requestHandler = new SCMConfigurationRequestHandler();
        apiRequest = mock(GoPluginApiRequest.class);
    }

    @Test
    public void shouldReturnSuccessJsonResponseForScmConfigurationRequest() {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        assertThat(apiResponse.responseCode(), is(equalTo(200)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainUrlField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, String> urlField = (Map<String, String>) response.get("url");

        assertThat(urlField, hasEntry("display-name", "URL"));
        assertThat(urlField, hasEntry("part-of-identity", (Object) true));
        assertThat(urlField, hasEntry("required", (Object) true));
        assertThat(urlField, hasEntry("secure", (Object) false));
        assertThat(urlField, hasEntry("display-order", "0"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainBranchField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, String> urlField = (Map<String, String>) response.get("branch");

        assertThat(urlField, hasEntry("display-name", "Branch"));
        assertThat(urlField, hasEntry("default-value", "master"));
        assertThat(urlField, hasEntry("part-of-identity", (Object) true));
        assertThat(urlField, hasEntry("required", (Object) false));
        assertThat(urlField, hasEntry("secure", (Object) false));
        assertThat(urlField, hasEntry("display-order", "4"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainPathField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, String> urlField = (Map<String, String>) response.get("path");

        assertThat(urlField, hasEntry("display-name", "Monitored Paths"));
        assertThat(urlField, hasEntry("part-of-identity", (Object) true));
        assertThat(urlField, hasEntry("required", (Object) true));
        assertThat(urlField, hasEntry("secure", (Object) false));
        assertThat(urlField, hasEntry("display-order", "3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void responseShouldContainShallowCloneField() throws IOException {
        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        Map<String, String> urlField = (Map<String, String>) response.get("shallow_clone");

        assertThat(urlField, hasEntry("display-name", "Shallow Clone"));
        assertThat(urlField, hasEntry("part-of-identity", (Object) false));
        assertThat(urlField, hasEntry("required", (Object) false));
        assertThat(urlField, hasEntry("secure", (Object) false));
        assertThat(urlField, hasEntry("display-order", "5"));
    }
}
