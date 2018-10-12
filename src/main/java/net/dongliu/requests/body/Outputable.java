package net.dongliu.requests.body;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Interface to writeTo body to output.
 * This interface should be functional interface.
 */
interface Outputable {
    /**
     * Get a input stream, to get content of body.
     *
     * @param output  the output stream to writeTo to
     * @param charset the charset to use
     */
    void writeTo(OutputStream output, Charset charset) throws IOException;
}
