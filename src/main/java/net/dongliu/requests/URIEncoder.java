package net.dongliu.requests;

import net.dongliu.requests.exception.RequestsException;
import org.jetbrains.annotations.NotNull;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * Util methods for encode / decode uri.
 * </p>
 * Encode diffs between url path segment, path parameter, and query part, follow the RFC3986:
 * http://www.rfc-base.org/txt/rfc-3986.txt.
 * java.net.URLEncoder is used to convert a String to the application/x-www-form-urlencoded MIME format,
 * which is not suitable for encode urls
 */
public class URIEncoder {
    /**
     * URL And URN:
     * foo://example.com:8042/over/there?name=ferret#nose
     * \_/   \______________/\_________/ \_________/ \__/
     * |           |            |            |        |
     * scheme     authority       path        query   fragment
     * |   _____________________|__
     * / \ /                        \
     * urn:example:animal:ferret:nose
     */

    // for user info
    private static final BitSet userInfoWhiteSet;
    // for domain name. domain name must be encoded with UTF-8
    private static final BitSet regNameWhiteSet;
    // for query parameter escape
    private static BitSet queryWhiteSet;
    // for url segment escape
    private static BitSet segmentWhiteSet;
    // for url fragment escape
    private static BitSet fragmentWhiteSet;
    private static final int caseDiff = ('a' - 'A');

    private static final char[] subDelims = "!$&'()*+,;=".toCharArray();

    /*
          reserved    = gen-delims / sub-delims

      gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"

      sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                  / "*" / "+" / "," / ";" / "="
     */
    static {

        // Use of the format "user:password" in the userinfo field is deprecated.
        // Applications should not render as clear text any data  after the first colon (":") character found within 
        // a userinfo subcomponent unless the data after the colon is the empty string (indicating no password).
        userInfoWhiteSet = createBasicSet();
        for (char c : subDelims) {
            userInfoWhiteSet.set(c);
        }
        userInfoWhiteSet.set(':');

        regNameWhiteSet = createBasicSet();
        for (char c : subDelims) {
            regNameWhiteSet.set(c);
        }

        queryWhiteSet = createBasicSet();
        for (char c : subDelims) {
            queryWhiteSet.set(c);
        }
        queryWhiteSet.set(':');
        queryWhiteSet.set('@');
        queryWhiteSet.set('/');
        queryWhiteSet.set('?');

        // URI reference may be a relative-path reference, in which case the first path segment cannot contain a 
        // colon (":") character.
        segmentWhiteSet = createBasicSet();
        for (char c : subDelims) {
            segmentWhiteSet.set(c);
        }
        segmentWhiteSet.set('@');

        fragmentWhiteSet = createBasicSet();
        for (char c : subDelims) {
            segmentWhiteSet.set(c);
        }
        queryWhiteSet.set(':');
        queryWhiteSet.set('@');
        segmentWhiteSet.set('/');
        segmentWhiteSet.set('?');
    }

    private static BitSet createBasicSet() {
        // basic unreserved char set
        //      unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
        BitSet basicSet = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            basicSet.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            basicSet.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            basicSet.set(i);
        }
        basicSet.set('-');
        basicSet.set('.');
        basicSet.set('_');
        basicSet.set('~');
        return basicSet;
    }

    /**
     * used to encode query parameter: key or value
     */
    public static String encodeParam(String s, Charset charset) {
        return _encode(s, charset, queryWhiteSet);
    }


    /**
     * decode query parameter: key or value
     */
    public static String decodeParam(String s, Charset charset) {
        return _decode(s, charset, true);
    }

    /**
     * Encode key-value query parameter
     */
    public static String encodeQuery(Map.Entry<String, String> query, Charset charset) {
        return encodeParam(query.getKey(), charset) + "=" + encodeParam(query.getValue(), charset);
    }

    /**
     * Encode multi queries
     */
    public static String encodeQueries(Collection<? extends Map.Entry<String, String>> queries, Charset charset) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> query : queries) {
            sb.append(encodeParam(query.getKey(), charset));
            sb.append('=');
            sb.append(encodeParam(query.getValue(), charset));
            sb.append('&');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Decode key-value query parameter
     */
    public static Parameter<String> decodeQuery(String s, Charset charset) {
        int idx = s.indexOf("=");
        if (idx < 0) {
            return Parameter.of("", decodeParam(s, charset));
        }
        return Parameter.of(decodeParam(s.substring(0, idx), charset), decodeParam(s.substring(idx + 1), charset));
    }

    /**
     * Parse query params
     */
    public static List<Parameter<String>> decodeQueries(String queryStr, Charset charset) {
        String[] queries = queryStr.split("&");
        List<Parameter<String>> list = new ArrayList<>(queries.length);
        for (String query : queries) {
            list.add(decodeQuery(query, charset));
        }
        return list;
    }

    /**
     * encode url path segment
     */
    public static String encodeSegment(String s, Charset charset) {
        return _encode(s, charset, segmentWhiteSet);
    }

    /**
     * decode url path segment
     */
    public static String decodeSegment(String s, Charset charset) {
        return _decode(s, charset, false);
    }

    /**
     * encode url fragment
     */
    public static String encodeFragment(String s, Charset charset) {
        return _encode(s, charset, fragmentWhiteSet);
    }

    /**
     * decode url fragment
     */
    public static String decodeFragment(String s, Charset charset) {
        return _decode(s, charset, false);
    }

    /**
     * Encode reg name
     */
    public static String encodeRegName(String regName) {
        return _encode(regName, StandardCharsets.UTF_8, regNameWhiteSet);
    }

    /**
     * Encode user part of url
     */
    public static String encodeUserInfo(String userInfo, Charset charset) {
        return _encode(userInfo, charset, userInfoWhiteSet);
    }

    private static String _encode(String s, Charset charset, BitSet whiteSet) {
        boolean needToChange = false;
        StringBuilder sb = new StringBuilder(s.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();

        for (int i = 0; i < s.length(); ) {
            int c = (int) s.charAt(i);
            if (whiteSet.get(c)) {
                sb.append((char) c);
                i++;
            } else {
                do {
                    charArrayWriter.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        if ((i + 1) < s.length()) {
                            int d = (int) s.charAt(i + 1);
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !whiteSet.get((c = (int) s.charAt(i))));

                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] ba = str.getBytes(charset);
                for (byte b : ba) {
                    sb.append('%');
                    char ch = Character.forDigit((b >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex load if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    sb.append(ch);
                    ch = Character.forDigit(b & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    sb.append(ch);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }

        return (needToChange ? sb.toString() : s);
    }


    public static String _decode(String s, Charset charset, boolean isQueryPart) {

        boolean needToChange = false;
        int numChars = s.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;

        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
                case '+':
                    if (isQueryPart) {
                        sb.append(' ');
                    } else {
                        sb.append('+');
                    }
                    i++;
                    needToChange = true;
                    break;
                case '%':
                    try {
                        // (numChars-i)/3 is an upper bound for the number
                        // of remaining bytes
                        if (bytes == null)
                            bytes = new byte[(numChars - i) / 3];
                        int pos = 0;

                        while (((i + 2) < numChars) &&
                                (c == '%')) {
                            int v = Integer.parseInt(s.substring(i + 1, i + 3), 16);
                            if (v < 0)
                                throw new IllegalArgumentException("Illegal hex characters in escape (%) pattern - " +
                                        "negative value");
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < numChars)
                                c = s.charAt(i);
                        }

                        // A trailing, incomplete byte encoding such as
                        // "%x" will cause an exception to be thrown

                        if ((i < numChars) && (c == '%'))
                            throw new IllegalArgumentException("Incomplete trailing escape (%) pattern");

                        sb.append(new String(bytes, 0, pos, charset));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Illegal hex characters in escape (%) pattern - " + e
                                .getMessage());
                    }
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }

        return (needToChange ? sb.toString() : s);
    }


    /**
     * Encode key-value form parameter
     */
    public static String encodeForm(Parameter<String> query, Charset charset) {
        try {
            return URLEncoder.encode(query.getName(), charset.name()) + "=" + URLEncoder.encode(query.getValue(),
                    charset.name());
        } catch (UnsupportedEncodingException e) {
            // should not happen
            throw new RequestsException(e);
        }
    }

    /**
     * Encode multi form parameters
     */
    public static String encodeForms(Collection<? extends Parameter<String>> queries, Charset charset) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Parameter<String> query : queries) {
                sb.append(URLEncoder.encode(query.getName(), charset.name()));
                sb.append('=');
                sb.append(URLEncoder.encode(query.getValue(), charset.name()));
                sb.append('&');
            }
        } catch (UnsupportedEncodingException e) {
            // should not happen
            throw new RequestsException(e);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Decode key-value query parameter
     */
    public static Parameter<String> decodeForm(String s, Charset charset) {
        int idx = s.indexOf("=");
        try {
            if (idx < 0) {
                return Parameter.of("", URLDecoder.decode(s, charset.name()));
            }
            return Parameter.of(URLDecoder.decode(s.substring(0, idx), charset.name()),
                    URLDecoder.decode(s.substring(idx + 1), charset.name()));
        } catch (UnsupportedEncodingException e) {
            // should not happen
            throw new RequestsException(e);
        }
    }

    /**
     * Parse query params
     */
    public static List<Parameter<String>> decodeForms(String queryStr, Charset charset) {
        String[] queries = queryStr.split("&");
        List<Parameter<String>> list = new ArrayList<>(queries.length);
        for (String query : queries) {
            list.add(decodeForm(query, charset));
        }

        return list;
    }

    @NotNull
    public static List<Parameter<String>> toStringParameters(Collection<? extends Map.Entry<String, ?>> params) {
        List<Parameter<String>> parameters = new ArrayList<>(params.size());
        for (Map.Entry<String, ?> entry : params) {
            parameters.add(Parameter.of(entry.getKey(), String.valueOf(entry.getValue())));
        }
        return parameters;
    }

    @NotNull
    public static URL joinUrl(URL url, Collection<? extends Map.Entry<String, String>> params, Charset charset) {
        if (params.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol()).append(':');
        if (url.getAuthority() != null && !url.getAuthority().isEmpty()) {
            sb.append("//").append(url.getAuthority());
        }
        if (url.getPath() != null) {
            sb.append(url.getPath());
        }

        String query = url.getQuery();
        String newQuery = encodeQueries(params, charset);
        if (query == null || query.isEmpty()) {
            sb.append('?').append(newQuery);
        } else {
            sb.append('?').append(query).append('&').append(newQuery);
        }

        if (url.getRef() != null) {
            sb.append('#').append(url.getRef());
        }

        URL fullURL;
        try {
            fullURL = new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new RequestsException(e);

        }
        return fullURL;
    }
}
