package net.dongliu.requests.executor;

import net.dongliu.requests.Cookie;
import net.dongliu.requests.utils.Cookies;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * CookieJar that store cookie in memory, maintaining cookies following RFC 6265
 */
class DefaultCookieJar implements CookieJar, Serializable {

    private static final long serialVersionUID = 8372575235144209124L;
    private Map<CookieKey, Cookie> cookieMap = new HashMap<>();

    @Override
    public synchronized void storeCookies(Collection<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            CookieKey key = new CookieKey(cookie.getDomain(), cookie.getPath(), cookie.getName());
            cookieMap.put(key, cookie);
        }
        removeExpiredCookies();
    }

    private void removeExpiredCookies() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<CookieKey, Cookie>> iterator = cookieMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<CookieKey, Cookie> entry = iterator.next();
            if (entry.getValue().expired(now)) {
                iterator.remove();
            }
        }
    }

    @NotNull
    @Override
    public synchronized List<Cookie> getCookies(URL url) {
        long now = System.currentTimeMillis();
        List<Cookie> matched = new ArrayList<>();
        for (Cookie cookie : cookieMap.values()) {
            if (!Cookies.match(cookie, url.getProtocol(), url.getHost().toLowerCase(), url.getPath())) {
                continue;
            }
            if (cookie.expired(now)) {
                continue;
            }
            matched.add(cookie);
        }
        Collections.sort(matched, new Comparator<Cookie>() { // we did not sort using create time here
            @Override
            public int compare(Cookie cookie1, Cookie cookie2) {
                return cookie2.getPath().length() - cookie1.getPath().length();
            }
        });
        return matched;
    }

    @NotNull
    @Override
    public synchronized List<Cookie> getCookies() {
        return new ArrayList<>(cookieMap.values());
    }

    private static class CookieKey {
        @NotNull
        private final String domain;
        @NotNull
        private final String path;
        @NotNull
        private final String name;

        public CookieKey(String domain, String path, String name) {
            this.domain = domain;
            this.path = path;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CookieKey cookieKey = (CookieKey) o;

            if (!domain.equals(cookieKey.domain)) return false;
            if (!path.equals(cookieKey.path)) return false;
            return name.equals(cookieKey.name);
        }

        @Override
        public int hashCode() {
            int result = domain.hashCode();
            result = 31 * result + path.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
