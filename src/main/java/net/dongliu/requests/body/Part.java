package net.dongliu.requests.body;

import net.dongliu.requests.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;

import static java.util.Objects.requireNonNull;
import static net.dongliu.requests.HttpHeaders.CONTENT_TYPE_BINARY;

/**
 * This class represent one part(field) of http multipart request body.
 *
 * @author Liu Dong
 */
public class Part<T> implements Outputable, Serializable {
    private static final long serialVersionUID = -8628605676399143491L;

    /**
     * Filed name
     */
    private final String name;
    /**
     * The file name, for http form's file input.
     * If is text input, this field is null.
     */
    @Nullable
    private final String fileName;
    /**
     * The part content
     */
    private final T body;

    /**
     * The content type of this Part.
     */
    @Nullable
    private String contentType;

    private final Outputor<T> outputor;
    /**
     * The charset of this part content.
     */
    @Nullable
    private Charset charset;

    private Part(String name, @Nullable String fileName, T body, Outputor<T> outputor, @Nullable String contentType) {
        this.name = requireNonNull(name);
        this.fileName = fileName;
        this.body = requireNonNull(body);
        this.contentType = contentType;
        this.outputor = requireNonNull(outputor);
    }

    /**
     * Set content type for this part.
     */
    public Part<T> contentType(String contentType) {
        this.contentType = requireNonNull(contentType);
        return this;
    }

    /**
     * The charset of this part's content. Each part of MultiPart body can has it's own charset set.
     * If not set, the part is supposed to be binary content, without charset in content type.
     * Default not set.
     *
     * @param charset the charset
     * @return self
     */
    public Part<T> charset(Charset charset) {
        this.charset = requireNonNull(charset);
        return this;
    }

    /**
     * Create a file multi-part field, from file.
     * This return a part equivalent to &lt;input type="file" /&gt; field in multi part form.
     */
    public static Part<File> file(String name, File file) {
        return file(name, file.getName(), file);
    }

    /**
     * Create a file multi-part field, from file.
     * This return a part equivalent to &lt;input type="file" /&gt; field in multi part form.
     */
    public static Part<File> file(String name, String fileName, File file) {
        return new Part<>(name, fileName, file, new Outputor<File>() {

            @Override
            public void writeBody(File body, OutputStream out, Charset charset) throws IOException {
                IOUtils.copy(new FileInputStream(body), out);
            }
        }, ContentTypes.probeContentType(file));
    }


    /**
     * Create a file multi-part field, from InputStream.
     * This return a part equivalent to &lt;input type="file" /&gt; field in multi part form.
     *
     * @deprecated Http body may be send multi times(because of redirect or other reasons), use {@link Part#file(String, String, InputStreamSupplier)} instead.
     */
    @Deprecated
    public static Part<InputStream> file(String name, String fileName, InputStream in) {
        return new Part<>(name, fileName, in, new Outputor<InputStream>() {

            @Override
            public void writeBody(InputStream body, OutputStream out, Charset charset) throws IOException {
                IOUtils.copy(body, out);
            }
        }, CONTENT_TYPE_BINARY);
    }

    /**
     * Create a file multi-part field, from InputStream.
     * This return a part equivalent to &lt;input type="file" /&gt; field in multi part form.
     */
    public static Part<InputStreamSupplier> file(String name, String fileName, InputStreamSupplier supplier) {
        return new Part<>(name, fileName, supplier, new Outputor<InputStreamSupplier>() {

            @Override
            public void writeBody(InputStreamSupplier body, OutputStream out, Charset charset) throws IOException {
                IOUtils.copy(body.get(), out);
            }
        }, CONTENT_TYPE_BINARY);
    }

    /**
     * Create a file multi-part field, from byte array data.
     * This return a part equivalent to &lt;input type="file" /&gt; field in multi part form.
     */
    public static Part<byte[]> file(String name, String fileName, byte[] bytes) {
        return new Part<>(name, fileName, bytes, new Outputor<byte[]>() {
            @Override
            public void writeBody(byte[] body, OutputStream out, Charset charset) throws IOException {
                out.write(body);
            }
        }, CONTENT_TYPE_BINARY);
    }

    /**
     * Create a text multi-part field.
     * This return a part equivalent to &lt;input type="text" /&gt; field in multi part form.
     */
    public static Part<String> text(String name, String value) {
        return new Part<>(name, null, value, new Outputor<String>() {
            @Override
            public void writeBody(String body, OutputStream out, Charset charset) throws IOException {
                // just use charset of request to encode this part
                OutputStreamWriter writer = new OutputStreamWriter(out, charset);
                writer.write(body);
                writer.flush();
            }
        }, null);
    }

    /**
     * Create a (name, value) text multi-part field.
     * This return a part equivalent to &lt;input type="text" /&gt; field in multi part form.
     *
     * @deprecated use {@link #text(String, String)} instead.
     */
    @Deprecated
    public static Part<String> param(String name, String value) {
        return text(name, value);
    }

    /**
     * The part field name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * may be null if not exists
     */
    @Nullable
    public String getFileName() {
        return fileName;
    }

    /**
     * The part body
     */
    public T getBody() {
        return body;
    }

    /**
     * The content type.
     */
    @Nullable
    public String getContentType() {
        return contentType;
    }

    @Override
    public void writeTo(OutputStream output, Charset charset) throws IOException {
        // the charset is only for text part.
        // cause text part do not has content Type header, we just use the request header's charset, hope server can handle it.
        outputor.writeBody(body, output, charset);
    }

    /**
     * The charset of this part's content. Each part of MultiPart body can has it's own charset set.
     * If not set, the part is supposed to be binary content, without charset in content type.
     *
     * @return the charset of this part content. if not set, return null.
     */
    @Nullable
    public Charset getCharset() {
        return charset;
    }

    private interface Outputor<T> {
        void writeBody(T body, OutputStream out, Charset charset) throws IOException;
    }
}
