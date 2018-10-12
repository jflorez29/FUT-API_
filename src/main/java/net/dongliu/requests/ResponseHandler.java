package net.dongliu.requests;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handler raw response body, convert to result T
 */
public interface ResponseHandler<T> {

    /**
     * Handler raw response body, convert to result T
     */
    T handle(ResponseInfo responseInfo) throws IOException;

    /**
     * Response info
     */
    class ResponseInfo {
        private String url;
        private int statusCode;
        private Headers headers;
        private InputStream in;

        ResponseInfo(String url, int statusCode, Headers headers, InputStream in) {
            this.url = url;
            this.statusCode = statusCode;
            this.headers = headers;
            this.in = in;
        }

        /**
         * The url after redirect. if no redirect during request, this url is the origin url send.
         */
        public String getUrl() {
            return url;
        }

        /**
         * The response status code
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * The response headers
         */
        public Headers getHeaders() {
            return headers;
        }

        /**
         * The response input stream
         */
        public InputStream getIn() {
            return in;
        }
    }
}
