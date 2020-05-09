package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.helpers.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Mockito.mock;

public class SCMViewRequestHandlerTests {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnSuccessJsonResponseForScmViewRequest() throws IOException {
        String template = IOUtils.toString(getClass().getResourceAsStream("/scm.template.html"), StandardCharsets.UTF_8);
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        RequestHandler requestHandler = new SCMViewRequestHandler();

        GoPluginApiResponse apiResponse = requestHandler.handle(apiRequest);

        Map<String, Object> response = JsonHelper.getResponse(apiResponse);
        assertThat(apiResponse.responseCode(), is(equalTo(200)));
        assertThat(response, hasEntry("displayValue", SCMViewRequestHandler.PLUGIN_NAME));
        assertThat(response, hasEntry("template", template));
    }
}
