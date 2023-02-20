package com.mcreater.amclcore.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

public class HttpClientWrapper {
    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        OPTIONS,
        HEAD,
        TRACE
    }
    private HttpClient client;
    private HttpRequestBase request;
    private RequestConfig.Builder config = RequestConfig.custom();
    public HttpClientWrapper(URIBuilder builder, Method method) {
        client = HttpClients.createDefault();
        request = createUriRequest(method);
    }

    public HttpClientWrapper connectTimeout(int timeout) {
        config.setConnectTimeout(timeout);
        return this;
    }

    public HttpClientWrapper socketTimeout(int timeout) {
        config.setSocketTimeout(timeout);
        return this;
    }

    public HttpClientWrapper ConnectionRequestTimeout(int timeout) {
        config.setConnectionRequestTimeout(timeout);
        return this;
    }

    private HttpRequestBase createUriRequest(Method method) {
        switch (method) {
            default:
            case GET:
                return new HttpGet();
            case POST:
                return new HttpPost();
            case PUT:
                return new HttpPut();
            case DELETE:
                return new HttpDelete();
            case PATCH:
                return new HttpPatch();
            case OPTIONS:
                return new HttpOptions();
            case HEAD:
                return new HttpHead();
            case TRACE:
                return new HttpTrace();
        }
    }
}
