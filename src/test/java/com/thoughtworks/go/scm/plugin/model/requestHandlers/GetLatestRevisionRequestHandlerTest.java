package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.tw.go.plugin.GitHelper;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetLatestRevisionRequestHandlerTest {

    @Mock
    private GoPluginApiRequest pluginApiRequestMock;

    @Mock
    private GitHelper gitHelperMock;

    @Mock
    private GitConfig gitConfigMock;


    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponseWhenUrlIsNotSpecified() {

        try (MockedStatic<JsonUtils> mockedUtils = mockStatic(JsonUtils.class);
             MockedStatic<HelperFactory> mockedFactory = mockStatic(HelperFactory.class)) {

            setupMockedRequestAndGitConfig(mockedUtils, mockedFactory);

            RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
            ArgumentCaptor<String> responseArgumentCaptor = ArgumentCaptor.forClass(String.class);

            when(JsonUtils.renderErrorApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

            checkoutRequestHandler.handle(pluginApiRequestMock);

            String responseMap = responseArgumentCaptor.getValue();
            assertThat(responseMap).isEqualTo("URL is a required field");
        }

    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleApiRequestAndRenderSuccessApiResponse() {
        try (MockedStatic<JsonUtils> mockedUtils = mockStatic(JsonUtils.class);
             MockedStatic<HelperFactory> mockedFactory = mockStatic(HelperFactory.class)) {

            setupMockedRequestAndGitConfig(mockedUtils, mockedFactory);

            Revision revision = new Revision("1", new Date(), "comment", "user", "blah@blah.com", Collections.emptyList());
            RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
            ArgumentCaptor<Map<String, Object>> responseArgumentCaptor = ArgumentCaptor.forClass(Map.class);
            List<String> paths = List.of("path1", "path2");

            when(JsonUtils.renderSuccessApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));
            when(JsonUtils.getPaths(pluginApiRequestMock)).thenReturn(paths);
            when(gitConfigMock.getUrl()).thenReturn("https://github.com/TWChennai/gocd-git-path-material-plugin.git");
            when(gitHelperMock.getLatestRevision(any())).thenReturn(revision);

            checkoutRequestHandler.handle(pluginApiRequestMock);

            verify(gitHelperMock).cloneOrFetch();
            verify(gitHelperMock).getLatestRevision(paths);

            Map<String, Object> responseMap = responseArgumentCaptor.getValue();
            assertThat(responseMap.size()).isEqualTo(1);
        }
    }

    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponseWhenCloneFailed() {
        try (MockedStatic<JsonUtils> mockedUtils = mockStatic(JsonUtils.class);
             MockedStatic<HelperFactory> mockedFactory = mockStatic(HelperFactory.class)) {

            setupMockedRequestAndGitConfig(mockedUtils, mockedFactory);

            RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            Exception cause = new IllegalArgumentException("git@github.com:lifealike/gocd-config.git: UnknownHostKey: github.com. RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48");
            RuntimeException runtimeException = new RuntimeException("clone failed", cause);

            when(JsonUtils.renderErrorApiResponse(eq(pluginApiRequestMock), errorCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));
            when(gitConfigMock.getUrl()).thenReturn("https://github.com/TWChennai/gocd-git-path-material-plugin.git");
            doThrow(runtimeException).when(gitHelperMock).cloneOrFetch();

            checkoutRequestHandler.handle(pluginApiRequestMock);

            assertThat(errorCaptor.getValue()).isEqualTo(runtimeException);
        }
    }


    private void setupMockedRequestAndGitConfig(MockedStatic<JsonUtils> mockedUtils, MockedStatic<HelperFactory> mockedFactory) {
        final String flyWeightFolder = "flyweightFolder";
        final String responseBody = "mocked body";

        Map<String, Object> requestBody = Map.of(
                "flyweight-folder", flyWeightFolder,
                "revision", Map.of("revision", "b6d7a9c"));

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        mockedUtils.when(() -> JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);
        mockedUtils.when(() -> JsonUtils.toServerSideGitConfig(pluginApiRequestMock)).thenReturn(gitConfigMock);
        mockedFactory.when(() -> HelperFactory.git(gitConfigMock, new File(flyWeightFolder))).thenReturn(gitHelperMock);
    }
}