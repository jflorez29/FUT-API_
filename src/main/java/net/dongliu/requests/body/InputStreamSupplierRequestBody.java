package net.dongliu.requests.body;

import net.dongliu.requests.utils.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static net.dongliu.requests.HttpHeaders.CONTENT_TYPE_BINARY;

/**
 * @author Liu Dong
 */
class InputStreamSupplierRequestBody extends RequestBody<InputStreamSupplier> {
    private static final long serialVersionUID = -2463504912342237751L;

    InputStreamSupplierRequestBody(InputStreamSupplier body) {
        super(body, CONTENT_TYPE_BINARY, false);
    }

    @Override
    public void writeBody(OutputStream os, Charset charset) throws IOException {
        IOUtils.copy(getBody().get(), os);
    }
}
