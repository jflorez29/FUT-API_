package net.dongliu.requests.body;

import net.dongliu.requests.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Request body, which get data from file.
 *
 * @author Liu Dong
 */
class FileRequestBody extends RequestBody<File> {
    private static final long serialVersionUID = -1902920038280221251L;

    FileRequestBody(File body) {
        super(body, ContentTypes.probeContentType(body), false);
    }

    @Override
    public void writeBody(OutputStream os, Charset charset) throws IOException {
        IOUtils.copy(new FileInputStream(getBody()), os);
    }
}
