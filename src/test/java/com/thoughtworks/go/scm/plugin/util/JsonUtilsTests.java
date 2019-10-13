package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import lombok.Getter;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonUtilsTests {

    @Getter
    private class Response {
        private String message;

        Response(String message) {
            this.message = message;
        }
    }

    @Getter
    private class ConfigurationItem {
        String value;

        ConfigurationItem(String value) {
            this.value = value;
        }
    }

    @Test
    public void shouldReturnGoPluginApiResponseWithSuccessCode() throws IOException {
        Response response = new Response("Hello");
        GoPluginApiResponse apiResponse = JsonUtils.renderSuccessApiResponse(response);

        assertThat(apiResponse.responseCode(), is(equalTo(200)));
        assertThat(apiResponse.responseHeaders(), is(nullValue()));
        assertThat(apiResponse.responseBody(), is(equalTo(JsonHelper.toJson(response))));
    }

    @Test
    public void shouldReturnGoPluginApiResponseWithFailureCode() throws IOException {
        Response response = new Response("Hello");
        GoPluginApiResponse apiResponse = JsonUtils.renderErrrorApiResponse(response);

        assertThat(apiResponse.responseCode(), is(equalTo(500)));
        assertThat(apiResponse.responseHeaders(), is(nullValue()));
        assertThat(apiResponse.responseBody(), is(equalTo(JsonHelper.toJson(response))));
    }

    @Test
    public void shouldReturnScmConfiguration() throws IOException {
        final Map<String, Object> configurationMap = new HashMap<String, Object>() {{
            put("SCM_URL", new ConfigurationItem("http://localhost.com"));
            put("USERNAME", new ConfigurationItem("user"));
            put("PASSWORD", new ConfigurationItem("pass"));
        }};

        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        Map<String, Object> scmConfiguration = new HashMap<>();
        scmConfiguration.put("scm-configuration", configurationMap);
        String responseBody = JsonHelper.toJson(scmConfiguration);
        when(apiRequest.requestBody()).thenReturn(responseBody);

        Map<String, String> actualScmConfiguration = JsonUtils.parseScmConfiguration(apiRequest);

        assertThat(actualScmConfiguration, hasEntry("SCM_URL", "http://localhost.com"));
        assertThat(actualScmConfiguration, hasEntry("USERNAME", "user"));
        assertThat(actualScmConfiguration, hasEntry("PASSWORD", "pass"));
    }

    @Test
    public void shouldSplitPath() {
        assertThat(JsonUtils.splitPaths(null), empty());
        assertThat(JsonUtils.splitPaths(""), empty());
        assertThat(JsonUtils.splitPaths("a"), is(List.of("a")));
        assertThat(JsonUtils.splitPaths(" a   "), is(List.of("a")));
        assertThat(JsonUtils.splitPaths("a/b, c/d"), is(List.of("a/b", "c/d")));
    }

}
