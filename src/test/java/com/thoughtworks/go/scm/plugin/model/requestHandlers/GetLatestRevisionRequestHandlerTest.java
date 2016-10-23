package com.thoughtworks.go.scm.plugin.model.requestHandlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.scm.plugin.jgit.JGitHelper;
import com.thoughtworks.go.scm.plugin.model.GitConfig;
import com.thoughtworks.go.scm.plugin.model.Revision;
import com.thoughtworks.go.scm.plugin.util.JsonUtils;
import org.eclipse.jgit.errors.TransportException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtils.class, JGitHelper.class, GitConfig.class})
public class GetLatestRevisionRequestHandlerTest {
    private GoPluginApiRequest pluginApiRequestMock;
    private JGitHelper jGitHelperMock;
    private GitConfig gitConfigMock;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(JsonUtils.class);
        PowerMockito.mockStatic(JGitHelper.class);
        PowerMockito.mockStatic(GitConfig.class);

        final String revision = "b6d7a9c";
        pluginApiRequestMock = mock(GoPluginApiRequest.class);
        jGitHelperMock = mock(JGitHelper.class);

        gitConfigMock = mock(GitConfig.class);
        final String flyWeightFolder = "flyweightFolder";
        final String responseBody = "mocked body";

        HashMap<String, Object> requestBody = new HashMap<String, Object>() {{
            put("flyweight-folder", flyWeightFolder);
            put("revision", new HashMap<String, Object>() {{
                put("revision", revision);
            }});
        }};

        when(pluginApiRequestMock.requestBody()).thenReturn(responseBody);
        when(JsonUtils.parseJSON(responseBody)).thenReturn(requestBody);
        when(GitConfig.create(pluginApiRequestMock)).thenReturn(gitConfigMock);
        when(JGitHelper.create(gitConfigMock, flyWeightFolder)).thenReturn(jGitHelperMock);
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
        final Revision revisionMock = mock(Revision.class);
        RequestHandler checkoutRequestHandler = new GetLatestRevisionRequestHandler();
        ArgumentCaptor<HashMap> responseArgumentCaptor = ArgumentCaptor.forClass(HashMap.class);
        final String path = "path1, path2";

        HashMap<String, Object> revisionMap = new HashMap<>();
        HashMap<String, String> configuration = new HashMap<String, String>() {{
            put("path", path);
        }};

        when(JsonUtils.renderSuccessApiResponse(responseArgumentCaptor.capture())).thenReturn(mock(GoPluginApiResponse.class));
        when(JsonUtils.parseScmConfiguration(pluginApiRequestMock)).thenReturn(configuration);
        when(gitConfigMock.getUrl()).thenReturn("https://github.com/TWChennai/gocd-git-path-material-plugin.git");
        when(jGitHelperMock.getLatestRevision(path)).thenReturn(revisionMock);
        when(revisionMock.getRevisionMap()).thenReturn(revisionMap);

        checkoutRequestHandler.handle(pluginApiRequestMock);

        verify(jGitHelperMock).cloneOrFetch();
        verify(jGitHelperMock).getLatestRevision(path);

        Map<String, String> responseMap = responseArgumentCaptor.getValue();
        assertThat(responseMap, hasEntry("revision", (Object) revisionMap));
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