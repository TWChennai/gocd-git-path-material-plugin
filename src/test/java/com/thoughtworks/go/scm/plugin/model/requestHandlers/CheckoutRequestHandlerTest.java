package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.jgit.JGitHelper;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import org.eclipse.jgit.errors.TransportException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtils.class, JGitHelper.class, GitConfig.class})
public class CheckoutRequestHandlerTest {

    private GoPluginApiRequest pluginApiRequestMock;
    private JGitHelper jGitHelperMock;
    private String revision;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(JsonUtils.class);
        PowerMockito.mockStatic(JGitHelper.class);
        PowerMockito.mockStatic(GitConfig.class);

        pluginApiRequestMock = mock(GoPluginApiRequest.class);
        revision = "b6d7a9c";
        jGitHelperMock = mock(JGitHelper.class);

        GitConfig gitConfigMock = mock(GitConfig.class);
        final String destinationFolder = "destination";
        final String responseBody = "mocked body";

        HashMap<String, Object> requestBody = new HashMap<String, Object>() {{
            put("destination-folder", destinationFolder);
            put("revision", new HashMap<String, Object>() {{
                put("revision", revision);
            }});
        }};

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        when(JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);
        when(GitConfig.create(pluginApiRequestMock)).thenReturn(gitConfigMock);
        when(JGitHelper.create(gitConfigMock, destinationFolder)).thenReturn(jGitHelperMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleApiRequestAndRenderSuccessApiResponse() {
        RequestHandler checkoutRequestHandler = new CheckoutRequestHandler();
        ArgumentCaptor<HashMap> responseArgumentCaptor = ArgumentCaptor.forClass(HashMap.class);

        when(JsonUtils.renderSuccessApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

        checkoutRequestHandler.handle(pluginApiRequestMock);

        verify(jGitHelperMock).cloneOrFetch();
        verify(jGitHelperMock).resetHard(revision);

        Map<String, Object> responseMap = responseArgumentCaptor.getValue();
        ArrayList<String> messages = (ArrayList<String>)responseMap.get("messages");

        assertThat(responseMap, hasEntry("status", "success"));
        assertThat(messages, hasItem(String.format("Checked out to revision %s", revision)));
    }

    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponse() {
        RequestHandler checkoutRequestHandler = new CheckoutRequestHandler();
        ArgumentCaptor<String> responseArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TransportException cause = new TransportException("git@github.com:lifealike/gocd-config.git: UnknownHostKey: github.com. RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48");
        RuntimeException runtimeException = new RuntimeException("clone failed", cause);
        doThrow(runtimeException).when(jGitHelperMock).cloneOrFetch();
        when(JsonUtils.renderErrrorApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

        checkoutRequestHandler.handle(pluginApiRequestMock);

        String message = responseArgumentCaptor.getValue();
        assertThat(message, is(equalTo(getRootCauseMessage(runtimeException))));
    }
}