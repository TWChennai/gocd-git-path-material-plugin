package com.thoughtworks.go.scm.plugin.model.requestHandlers;

public class RequestHandlerFactory {
    private static final String REQUEST_SCM_CONFIGURATION = "scm-configuration";
    private static final String REQUEST_SCM_VIEW = "scm-view";
    private static final String REQUEST_VALIDATE_SCM_CONFIGURATION = "validate-scm-configuration";
    private static final String REQUEST_CHECK_SCM_CONNECTION = "check-scm-connection";
    private static final String REQUEST_LATEST_REVISION = "latest-revision";
    private static final String REQUEST_LATEST_REVISIONS_SINCE = "latest-revisions-since";
    private static final String REQUEST_CHECKOUT = "checkout";

    public static RequestHandler create(String requestName) {
        switch (requestName) {
            case REQUEST_SCM_CONFIGURATION:
                return new SCMConfigurationRequestHandler();
            case REQUEST_SCM_VIEW:
                return new SCMViewRequestHandler();
            case REQUEST_VALIDATE_SCM_CONFIGURATION:
                return new SCMValidationRequestHandler();
            case REQUEST_CHECK_SCM_CONNECTION:
                return new SCMCheckConnectionRequestHandler();
            case REQUEST_LATEST_REVISION:
                return new GetLatestRevisionRequestHandler();
            case REQUEST_LATEST_REVISIONS_SINCE:
                return new LatestRevisionSinceRequestHandler();
            case REQUEST_CHECKOUT:
                return new CheckoutRequestHandler();
            default:
                return new UnkownRequestHandler();
        }

    }
}
