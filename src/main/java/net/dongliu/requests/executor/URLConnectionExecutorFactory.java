package net.dongliu.requests.executor;

import org.jetbrains.annotations.NotNull;

/**
 * Only for internal use
 */
class URLConnectionExecutorFactory extends RequestExecutorFactory {
    static final RequestExecutorFactory instance = new URLConnectionExecutorFactory();

    @NotNull
    @Override
    public SessionContext newSessionContext() {
        return new SessionContext(new DefaultCookieJar());
    }

    @NotNull
    @Override
    public HttpExecutor getHttpExecutor() {
        return new URLConnectionExecutor();
    }
}
