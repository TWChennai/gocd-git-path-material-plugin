package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.cmd.ProcessOutputStreamConsumer;
import com.tw.go.plugin.model.GitConfig;
import org.eclipse.jgit.errors.TransportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutRequestHandlerTest {

    @Mock
    private GoPluginApiRequest pluginApiRequestMock;

    @Mock
    private GitHelper gitHelperMock;

    @Mock
    private GitConfig gitConfigMock;

    private String revision = "b6d7a9c";
    private final String destinationFolder = "destination";

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleApiRequestAndRenderSuccessApiResponse() {
        try (MockedStatic<JsonUtils> mockedUtils = mockStatic(JsonUtils.class);
             MockedStatic<HelperFactory> mockedFactory = mockStatic(HelperFactory.class)) {
            setupMockedRequestAndGitConfig(mockedUtils, mockedFactory);

            RequestHandler checkoutRequestHandler = new CheckoutRequestHandler();
            ArgumentCaptor<Map<String, Object>> responseArgumentCaptor = ArgumentCaptor.forClass(Map.class);

            when(JsonUtils.renderSuccessApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

            checkoutRequestHandler.handle(pluginApiRequestMock);

            verify(gitHelperMock).cloneOrFetch();
            verify(gitHelperMock).resetHard(revision);

            Map<String, Object> responseMap = responseArgumentCaptor.getValue();
            ArrayList<String> messages = (ArrayList<String>) responseMap.get("messages");

            assertThat(responseMap).containsEntry("status", "success");
            assertThat(messages).contains(String.format("Start updating %s to revision %s from null", destinationFolder, revision));
        }
    }

    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponse() {
        try (MockedStatic<JsonUtils> mockedUtils = mockStatic(JsonUtils.class);
             MockedStatic<HelperFactory> mockedFactory = mockStatic(HelperFactory.class)) {
            setupMockedRequestAndGitConfig(mockedUtils, mockedFactory);

            RequestHandler checkoutRequestHandler = new CheckoutRequestHandler();
            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            TransportException cause = new TransportException("git@github.com:lifealike/gocd-config.git: UnknownHostKey: github.com. RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48");
            RuntimeException runtimeException = new RuntimeException("clone failed", cause);
            doThrow(runtimeException).when(gitHelperMock).cloneOrFetch();
            when(JsonUtils.renderErrorApiResponse(eq(pluginApiRequestMock), errorCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

            checkoutRequestHandler.handle(pluginApiRequestMock);
            assertThat(errorCaptor.getValue()).isEqualTo(runtimeException);
        }
    }


    private void setupMockedRequestAndGitConfig(MockedStatic<JsonUtils> mockedUtils, MockedStatic<HelperFactory> mockedFactory) {

        final String responseBody = "mocked body";

        Map<String, Object> requestBody = Map.of(
                "destination-folder", destinationFolder,
                "revision", Map.of("revision", revision)
        );

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        mockedUtils.when(() -> JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);

        mockedUtils.when(() -> JsonUtils.toAgentGitConfig(pluginApiRequestMock)).thenReturn(gitConfigMock);

        mockedFactory.when(() -> HelperFactory.git(eq(gitConfigMock),
                Mockito.any(File.class),
                Mockito.any(ProcessOutputStreamConsumer.class),
                Mockito.any(ProcessOutputStreamConsumer.class))).thenReturn(gitHelperMock);
    }

}