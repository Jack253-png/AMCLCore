package com.mcreater.amclcore.exceptions.io;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;

/**
 * Used for {@link com.mcreater.amclcore.util.HttpClientWrapper}
 */
public class RequestException extends IOException {
    private final StatusLine line;
    private final HttpEntity entity;

    public RequestException(StatusLine line, HttpEntity entity) {
        this.line = line;
        this.entity = entity;
    }

    public String getMessage() {
        try {
            return translatable("core.exceptions.req_exc", line.getStatusCode(), line.getReasonPhrase(), EntityUtils.toString(entity)).getText();
        } catch (IOException e) {
            return translatable("core.exceptions.req_exc", line.getStatusCode(), line.getReasonPhrase(), "<unknown entity>").getText();
        }
    }
}
