package com.mcreater.amclcore.exceptions;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RequestException extends IOException {
    private final StatusLine line;
    private final HttpEntity entity;
    public RequestException(StatusLine line, HttpEntity entity) {
        this.line = line;
        this.entity = entity;
    }

    public String getMessage() {
        try {
            return String.format("Http/Https request failed, server returned code %d, reason %s, entity result: %s", line.getStatusCode(), line.getReasonPhrase(), EntityUtils.toString(entity));
        } catch (IOException e) {
            return "Failed to process exception message";
        }
    }
}
