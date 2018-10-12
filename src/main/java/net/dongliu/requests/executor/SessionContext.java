package net.dongliu.requests.executor;

import java.io.Serializable;

/**
 * Maintain session.
 */
public class SessionContext implements Serializable {
    private static final long serialVersionUID = -2357887929783737274L;
    private final CookieJar cookieJar;

    public SessionContext(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }
}
