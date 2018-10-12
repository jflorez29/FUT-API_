package net.dongliu.requests;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Wrap to deal with response headers
 *
 * @author Liu Dong
 */
public class Headers implements Serializable {
    private static final long serialVersionUID = -1283402589869346874L;
    private final List<Header> headers;
    private transient volatile Map<String, List<String>> map;

    public Headers(List<Header> headers) {
        this.headers = Collections.unmodifiableList(Objects.requireNonNull(headers));
    }

    /**
     * Get headers by name. If not exists, return empty list
     */
    @NotNull
    public List<String> getHeaders(String name) {
        Objects.requireNonNull(name);
        ensureMap();
        List<String> values = map.get(name.toLowerCase());
        if (values == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Get the first header value matched name. If not exists, return null.
     *
     * @deprecated using {@link #getHeader(String)} instead
     */
    @Deprecated
    @Nullable
    public String getFirstHeader(String name) {
        Objects.requireNonNull(name);
        return getHeader(name);
    }

    /**
     * Get the first header value matched name. If not exists, return null.
     */
    @Nullable
    public String getHeader(String name) {
        ensureMap();
        Objects.requireNonNull(name);
        List<String> values = map.get(name.toLowerCase());
        if (values == null) {
            return null;
        }
        return values.get(0);
    }

    /**
     * Get header value as long. If not exists, return defaultValue
     */
    public long getLongHeader(String name, long defaultValue) {
        String firstHeader = getHeader(name);
        if (firstHeader == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(firstHeader.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @NotNull
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * Get charset set in content type header.
     *
     * @return the charset, or defaultCharset if no charset is set.
     */
    public Charset getCharset(Charset defaultCharset) {
        Charset charset = getCharset();
        return charset == null ? defaultCharset : charset;
    }

    /**
     * Get charset set in content type header.
     *
     * @return null if no charset is set.
     */
    @Nullable
    public Charset getCharset() {
        String contentType = getHeader(HttpHeaders.NAME_CONTENT_TYPE);
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        String[] items = contentType.split(";");
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) {
                continue;
            }
            int idx = item.indexOf('=');
            if (idx < 0) {
                continue;
            }
            String key = item.substring(0, idx).trim();
            if (key.equalsIgnoreCase("charset")) {
                return Charset.forName(item.substring(idx + 1).trim());
            }
        }
        return StandardCharsets.UTF_8;
    }


    private void ensureMap() {
        if (this.map != null) {
            return;
        }
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, String> header : headers) {
            String key = header.getKey().toLowerCase();
            String value = header.getValue();
            List<String> list = map.get(key);
            if (list == null) {
                list = new ArrayList<>(4);
                list.add(value);
                map.put(key, list);
            } else {
                list.add(value);
            }
        }
        this.map = map;
    }
}
