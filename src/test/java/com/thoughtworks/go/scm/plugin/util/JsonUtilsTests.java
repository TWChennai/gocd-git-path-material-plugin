package com.thoughtworks.go.scm.plugin.util;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import com.tw.go.plugin.model.GitConfig;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonUtilsTests {

    private class Response {
        private String message;

        Response(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private class ConfigurationItem {
        String value;

        ConfigurationItem(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
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
        GoPluginApiResponse apiResponse = JsonUtils.renderErrorApiResponse(response);

        assertThat(apiResponse.responseCode(), is(equalTo(500)));
        assertThat(apiResponse.responseHeaders(), is(nullValue()));
        assertThat(apiResponse.responseBody(), is(equalTo(JsonHelper.toJson(response))));
    }

    @Test
    public void shouldReturnGoPluginApiResponseFromThrowable() throws IOException {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn("test-request");

        Throwable throwable = new IllegalArgumentException("bad args", new IOException("connection failure"));

        GoPluginApiResponse apiResponse = JsonUtils.renderErrorApiResponse(request, throwable);

        assertThat(apiResponse.responseCode(), is(equalTo(500)));
        assertThat(apiResponse.responseHeaders(), is(nullValue()));
        assertThat(apiResponse.responseBody(), is(equalTo(JsonHelper.toJson("test-request failed due to [IllegalArgumentException: bad args], rootCause [IOException: connection failure]"))));
    }

    @Test
    public void shouldReturnGitConfig() throws IOException {
        final Map<String, Object> configurationMap = Map.of(
                "url", new ConfigurationItem("http://localhost.com"),
                "username", new ConfigurationItem("user"),
                "password", new ConfigurationItem("pass")
        );

        GitConfig config = JsonUtils.toAgentGitConfig(mockApiRequestFor(configurationMap));

        assertThat(config.getUrl(), is("http://localhost.com"));
        assertThat(config.getUsername(), is("user"));
        assertThat(config.getPassword(), is("pass"));
        assertThat(config.getEffectiveBranch(), is("master"));
        assertThat(config.isRecursiveSubModuleUpdate(), is(true));
        assertThat(config.isShallowClone(), is(false));
    }

    @Test
    public void shouldReturnGitConfigWithShallowClone() throws IOException {
        final Map<String, Object> configurationMap = Map.of(
                "url", new ConfigurationItem("http://localhost.com"),
                "username", new ConfigurationItem("user"),
                "password", new ConfigurationItem("pass"),
                "shallow_clone", new ConfigurationItem("true")
        );

        GitConfig config = JsonUtils.toAgentGitConfig(mockApiRequestFor(configurationMap));

        assertThat(config.getUrl(), is("http://localhost.com"));
        assertThat(config.getUsername(), is("user"));
        assertThat(config.getPassword(), is("pass"));
        assertThat(config.getEffectiveBranch(), is("master"));
        assertThat(config.isRecursiveSubModuleUpdate(), is(true));
        assertThat(config.isShallowClone(), is(true));
    }

    private GoPluginApiRequest mockApiRequestFor(Map<String, Object> configurationMap) throws IOException {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        Map<String, Object> scmConfiguration = new HashMap<>();
        scmConfiguration.put("scm-configuration", configurationMap);
        String responseBody = JsonHelper.toJson(scmConfiguration);
        when(apiRequest.requestBody()).thenReturn(responseBody);
        return apiRequest;
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
