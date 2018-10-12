package net.dongliu.requests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dongliu.requests.ResponseHandler.ResponseInfo;
import net.dongliu.requests.exception.RequestsException;
import net.dongliu.requests.json.JsonLookup;
import net.dongliu.requests.json.TypeInfer;
import net.dongliu.requests.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Raw http response.
 * It you do not consume http response body, with readToText, readToBytes, writeToFile, toTextResponse,
 * toJsonResponse, etc.., you need to close this raw response manually
 *
 * @author Liu Dong
 */
public class RawResponse extends AbstractResponse implements AutoCloseable {
    private final String statusLine;
    private final InputStream input;
    private final HttpURLConnection conn;
    @Nullable
    private Charset charset;
    private String text;

    public RawResponse(String url, int statusCode, String statusLine, List<Cookie> cookies, Headers headers, InputStream input,
                       HttpURLConnection conn) {
        super(url, statusCode, cookies, headers);
        this.statusLine = statusLine;
        this.input = input;
        this.conn = conn;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(input);
        conn.disconnect();
    }

    /**
     * Set response read charset.
     * If not set, will get charset from response headers.
     *
     * @deprecated use {{@link #charset(Charset)}} instead
     */
    @Deprecated
    public RawResponse withCharset(Charset charset) {
        this.charset = Objects.requireNonNull(charset);
        return this;
    }

    /**
     * Set response read charset.
     * If not set, would get charset from response headers. If not found, would use UTF-8.
     */
    public RawResponse charset(Charset charset) {
        this.charset = Objects.requireNonNull(charset);
        return this;
    }

    /**
     * Set response read charset.
     * If not set, would get charset from response headers. If not found, would use UTF-8.
     */
    public RawResponse charset(String charset) {
        this.charset = Charset.forName(Objects.requireNonNull(charset));
        return this;
    }


    /**
     * Read response body to string. return empty string if response has no body
     */
    private String readToText() {
        Charset charset = getCharset();
        try (Reader reader = new InputStreamReader(input, charset)) {
            return IOUtils.readAll(reader);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Convert to response, with body as text. The origin raw response will be closed
     */
    public Response<String> toTextResponse() {
        return new Response<>(this.url, this.statusCode, this.cookies, this.headers, this.text);
    }

    /**
     * Read response body to byte array. return empty byte array if response has no body
     */
    public byte[] readToBytes() {
        try {
            return IOUtils.readAll(input);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Handle response body with handler, return a new response with content as handler result.
     * The response is closed whether this call succeed or failed with exception.
     */
    public <T> Response<T> toResponse(ResponseHandler<T> handler) {
        ResponseInfo responseInfo = new ResponseInfo(this.url, this.statusCode, this.headers, this.input);
        try {
            T result = handler.handle(responseInfo);
            return new Response<>(this.url, this.statusCode, this.cookies, this.headers, result);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Convert to response, with body as byte array
     */
    public Response<byte[]> toBytesResponse() {
        return new Response<>(this.url, this.statusCode, this.cookies, this.headers, readToBytes());
    }

    /**
     * Deserialize response content as json
     *
     * @return null if json value is null or empty
     */
    public <T> T readToJson(Type type) {
        try {
            return JsonLookup.getInstance().lookup().unmarshal(input, getCharset(), type);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Deserialize response content as json
     *
     * @return null if json value is null or empty
     */
    public <T> T readToJson(TypeInfer<T> typeInfer) {
        return readToJson(typeInfer.getType());
    }

    /**
     * Deserialize response content as json
     *
     * @return null if json value is null or empty
     */
    public <T> T readToJson(Class<T> cls) {
        return readToJson((Type) cls);
    }

    /**
     * Convert http response body to json result
     */
    public <T> Response<T> toJsonResponse(TypeInfer<T> typeInfer) {
        return new Response<>(this.url, this.statusCode, this.cookies, this.headers, readToJson(typeInfer));
    }

    /**
     * Convert http response body to json result
     */
    public <T> Response<T> toJsonResponse(Class<T> cls) {
        return new Response<>(this.url, this.statusCode, this.cookies, this.headers, readToJson(cls));
    }

    public JsonObject toJson(){
        JsonParser parser = new JsonParser();
        return parser.parse(text()).getAsJsonObject();
    }

    /**
     * Write response body to file
     */
    public void writeToFile(File file) {
        try {
            try (OutputStream os = new FileOutputStream(file)) {
                IOUtils.copy(input, os);
            }
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Write response body to file
     */
    public void writeToFile(Path path) {
        try {
            try (OutputStream os = Files.newOutputStream(path)) {
                IOUtils.copy(input, os);
            }
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }


    /**
     * Write response body to file
     */
    public void writeToFile(String path) {
        try {
            try (OutputStream os = new FileOutputStream(path)) {
                IOUtils.copy(input, os);
            }
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Write response body to file, and return response contains the file.
     */
    public Response<File> toFileResponse(Path path) {
        File file = path.toFile();
        this.writeToFile(file);
        return new Response<>(this.url, this.statusCode, this.cookies, this.headers, file);
    }

    /**
     * Write response body to OutputStream. OutputStream will not be closed.
     */
    public void writeTo(OutputStream out) {
        try {
            IOUtils.copy(input, out);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Write response body to Writer, charset can be set using {@link #charset(Charset)},
     * or will use charset detected from response header if not set.
     * Writer will not be closed.
     */
    public void writeTo(Writer writer) {
        try {
            try (Reader reader = new InputStreamReader(input, getCharset())) {
                IOUtils.copy(reader, writer);
            }
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    /**
     * Consume and discard this response body.
     */
    public void discardBody() {
        try {
            IOUtils.skipAll(input);
        } catch (IOException e) {
            throw new RequestsException(e);
        } finally {
            close();
        }
    }

    @Override
    public String text() {
        if (this.text == null) this.text = readToText();
        return this.text;
    }

    /**
     * The response status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the status line
     */
    public String getStatusLine() {
        return statusLine;
    }

    /**
     * The response body input stream
     */
    public InputStream getInput() {
        return input;
    }

    private Charset getCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        return headers.getCharset(StandardCharsets.UTF_8);
    }
}
