package net.dongliu.requests;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Common parent for RawResponse and Response
 */
abstract class AbstractResponse {
    protected final String url;
    protected final int statusCode;
    protected final List<Cookie> cookies;
    protected final Headers headers;

    protected AbstractResponse(String url, int statusCode, List<Cookie> cookies, Headers headers) {
        this.url = url;
        this.statusCode = statusCode;
        this.cookies = Collections.unmodifiableList(cookies);
        this.headers = headers;
    }

    /**
     * Get actual url (redirected)
     */
    public String getURL() {
        return url;
    }

    /**
     * return actual url (redirected)
     */
    public String url() {
        return url;
    }

    public abstract String text();

    /**
     * return response status code
     * @return status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * return response status code
     * @return status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Get all cookies returned by this response
     */
    @NotNull
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Get all response headers
     */
    @NotNull
    public List<Header> getHeaders() {
        return headers.getHeaders();
    }

    /**
     * Return all response headers
     */
    @NotNull
    public List<Header> headers() {
        return headers.getHeaders();
    }

    /**
     * Get first cookie match the name returned by this response, return null if not found
     *
     * @deprecated using {{@link #getCookie(String)}} instead
     */
    @Deprecated
    @Nullable
    public Cookie getFirstCookie(String name) {
        return getCookie(name);
    }

    /**
     * Get first cookie match the name returned by this response, return null if not found
     */
    @Nullable
    public Cookie getCookie(String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Get first header value match the name, return null if not exists
     *
     * @deprecated using {@link #getHeader(String)} instead
     */
    @Deprecated
    @Nullable
    public String getFirstHeader(String name) {
        return headers.getFirstHeader(name);
    }

    /**
     * Get first header value match the name, return null if not exists
     */
    @Nullable
    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    /**
     * Get all headers values with name. If not exists, return empty list
     */
    @NotNull
    public List<String> getHeaders(String name) {
        return this.headers.getHeaders(name);
    }

}
