package net.dongliu.requests.body;

import java.io.InputStream;

/**
 * Which can provider a input stream.
 */
public interface InputStreamSupplier {
    /**
     * Return a InputStream.
     * Every call to this method, should return a new InputStream, all InputStreams returned should contains the same data.
     *
     * @return A InputStream
     */
    InputStream get();
}
