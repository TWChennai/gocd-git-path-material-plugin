package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.HelperFactory;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import com.tw.go.plugin.jgit.JGitHelper;
import com.tw.go.plugin.model.GitConfig;
import com.tw.go.plugin.model.Revision;
import org.eclipse.jgit.errors.TransportException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtils.class, GitConfig.class, HelperFactory.class})
public class GetLatestRevisionRequestHandlerTest {
    private GoPluginApiRequest pluginApiRequestMock;
    private JGitHelper jGitHelperMock;
    private GitConfig gitConfigMock;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(JsonUtils.class);
        PowerMockito.mockStatic(GitConfig.class);
        PowerMockito.mockStatic(HelperFactory.class);

        final String revision = "b6d7a9c";
        pluginApiRequestMock = mock(GoPluginApiRequest.class);
        jGitHelperMock = mock(JGitHelper.class);

        gitConfigMock = mock(GitConfig.class);
        final String flyWeightFolder = "flyweightFolder";
        final String responseBody = "mocked body";

        Map<String, Object> requestBody = Map.of(
                "flyweight-folder", flyWeightFolder,
                "revision", Map.of("revision", revision));

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        when(JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);
        when(JsonUtils.toServerSideGitConfig(pluginApiRequestMock)).thenReturn(gitConfigMock);
        when(HelperFactory.git(gitConfigMock, new File(flyWeightFolder))).thenReturn(jGitHelperMock);
    }

    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponseWhenUrlIsNotSpecified() {
        RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
        ArgumentCaptor<String> responseArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(JsonUtils.renderErrrorApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));

        checkoutRequestHandler.handle(pluginApiRequestMock);

        String responseMap = responseArgumentCaptor.getValue();
        assertThat(responseMap, is(equalTo("URL is a required field")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleApiRequestAndRenderSuccessApiResponse() {
        Revision revision = new Revision("1", new Date(), "comment", "user", "blah@blah.com", Collections.emptyList());
        RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
        ArgumentCaptor<Map> responseArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        final String paths = "path1, path2";

        Map<String, String> configuration = Map.of("path", paths);

        when(JsonUtils.renderSuccessApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));
        when(JsonUtils.parseScmConfiguration(pluginApiRequestMock)).thenReturn(configuration);
        when(gitConfigMock.getUrl()).thenReturn("https://github.com/TWChennai/gocd-git-path-material-plugin.git");
        when(jGitHelperMock.getLatestRevision(any())).thenReturn(revision);

        checkoutRequestHandler.handle(pluginApiRequestMock);

        verify(jGitHelperMock).cloneOrFetch();
        verify(jGitHelperMock).getLatestRevision(List.of("path1", " path2"));

        Map<String, Object> responseMap = responseArgumentCaptor.getValue();
        assertThat(responseMap.size(), is(1));
    }

    @Test
    public void shouldHandleApiRequestAndRenderErrorApiResponseWhenCloneFailed() {
        RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
        ArgumentCaptor<String> responseArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TransportException cause = new TransportException("git@github.com:lifealike/gocd-config.git: UnknownHostKey: github.com. RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48");
        RuntimeException runtimeException = new RuntimeException("clone failed", cause);

        when(JsonUtils.renderErrrorApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));
        when(gitConfigMock.getUrl()).thenReturn("https://github.com/TWChennai/gocd-git-path-material-plugin.git");
        doThrow(runtimeException).when(jGitHelperMock).cloneOrFetch();

        checkoutRequestHandler.handle(pluginApiRequestMock);

        String responseMap = responseArgumentCaptor.getValue();
        assertThat(responseMap, is(equalTo(getRootCauseMessage(runtimeException))));
    }
}