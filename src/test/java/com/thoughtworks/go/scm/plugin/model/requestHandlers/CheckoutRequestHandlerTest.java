package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.jgit.JGitHelper;
import com.tw.go.plugin.model.GitConfig;
import org.eclipse.jgit.errors.TransportException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtils.class, JGitHelper.class, GitConfig.class, HelperFactory.class})
public class CheckoutRequestHandlerTest {

    private GoPluginApiRequest pluginApiRequestMock;
    private JGitHelper jGitHelperMock;
    private String revision = "b6d7a9c";
    private final String destinationFolder = "destination";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(JsonUtils.class);
        PowerMockito.mockStatic(JGitHelper.class);
        PowerMockito.mockStatic(GitConfig.class);
        PowerMockito.mockStatic(HelperFactory.class);

        pluginApiRequestMock = mock(GoPluginApiRequest.class);
        jGitHelperMock = mock(JGitHelper.class);

        GitConfig gitConfigMock = mock(GitConfig.class);
        final String responseBody = "mocked body";

        Map<String, Object> requestBody = Map.of(
                "destination-folder", destinationFolder,
                "revision", Map.of("revision", revision)
        );

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        when(JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);
        when(JsonUtils.toAgentGitConfig(pluginApiRequestMock)).thenReturn(gitConfigMock);
        when(HelperFactory.git(eq(gitConfigMock),
                Mockito.any(File.class),
                Mockito.any(ProcessOutputStreamConsumer.class),
                Mockito.any(ProcessOutputStreamConsumer.class))).thenReturn(jGitHelperMock);
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
        ArrayList<String> messages = (ArrayList<String>) responseMap.get("messages");

        assertThat(responseMap, hasEntry("status", "success"));
        assertThat(messages, Matchers.contains(String.format("Start updating %s to revision %s from null", destinationFolder, revision)));
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