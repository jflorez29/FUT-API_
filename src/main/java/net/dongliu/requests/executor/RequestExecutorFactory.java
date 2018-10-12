package net.dongliu.requests.executor;

import org.jetbrains.annotations.NotNull;

/**
 * Request Client interface
 */
public abstract class RequestExecutorFactory {

    public static RequestExecutorFactory getInstance() {
        return URLConnectionExecutorFactory.instance;
    }

    /**
     * Create new session context
     */
    @NotNull
    public abstract SessionContext newSessionContext();

    @NotNull
    public abstract HttpExecutor getHttpExecutor();
}
