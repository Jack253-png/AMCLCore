package com.mcreater.amclcore.util;

import com.mcreater.amclcore.exceptions.RequestException;
import lombok.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

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
    public enum Scheme {
        HTTP("http"),
        HTTPS("https"),
        TCP("tcp"),
        UDP("udp");
        private final String scheme;
        Scheme(String scheme) {
            this.scheme = scheme;
        }

        public String getScheme() {
            return scheme;
        }
    }
    private final HttpClient client;
    private final HttpRequestBase request;
    private final RequestConfig.Builder config = RequestConfig.custom();
    private final URIBuilder requestURI = new URIBuilder().setScheme("https");
    private boolean catchHttpError = false;
    private HttpClientWrapper(Method method) {
        client = HttpClients.createDefault();
        request = createUriRequest(method);
    }

    public static HttpClientWrapper createNew(Method method) {
        return new HttpClientWrapper(method);
    }

    public HttpClientWrapper connectTimeout(int timeout) {
        config.setConnectTimeout(timeout);
        return this;
    }

    public HttpClientWrapper socketTimeout(int timeout) {
        config.setSocketTimeout(timeout);
        return this;
    }

    public HttpClientWrapper connectionRequestTimeout(int timeout) {
        config.setConnectionRequestTimeout(timeout);
        return this;
    }

    public HttpClientWrapper requestURIScheme(Scheme scheme) {
        requestURI.setScheme(scheme.getScheme());
        return this;
    }

    public HttpClientWrapper requestURI(String host, String path) {
        requestURI.setHost(host).setPath(path);
        return this;
    }

    public HttpClientWrapper requestURI(String url) {
        Pair<String, String> parsed = NetUtils.parseToPair(url);
        return requestURI(parsed.getKey(), parsed.getValue());
    }

    public HttpClientWrapper requestURIPort(int port) {
        requestURI.setPort(port);
        return this;
    }

    public HttpClientWrapper requestURICharset(Charset charset) {
        requestURI.setCharset(charset);
        return this;
    }

    public HttpClientWrapper requestURIParam(String key, String value) {
        requestURI.addParameter(key, value);
        return this;
    }

    public HttpClientWrapper requestHeader(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    public HttpClientWrapper requestEntity(HttpEntity entity) {
        if (request instanceof HttpEntityEnclosingRequestBase) ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        return this;
    }

    public HttpClientWrapper requestEntityJson(String data) throws UnsupportedEncodingException {
        return requestEntity(
                HttpStringEntityWrapper.builder()
                        .content(data)
                        .build().toEntity()
        );
    }

    public <T> HttpClientWrapper requestEntityJson(T data) throws UnsupportedEncodingException {
        return requestEntityJson(GSON_PARSER.toJson(data));
    }

    public HttpClientWrapper requestOnCancelled(Cancellable cancellable) {
        request.setCancellable(cancellable);
        return this;
    }

    public HttpClientWrapper catchHttpExc(boolean catchHttpError) {
        this.catchHttpError = catchHttpError;
        return this;
    }

    public HttpEntity sendRequest() throws URISyntaxException, IOException {
        request.setURI(requestURI.build());
        request.setConfig(config.build());
        HttpResponse req = client.execute(request);
        if (req.getStatusLine().getStatusCode() > 399 && catchHttpError) throw new RequestException(req.getStatusLine(), req.getEntity());
        return req.getEntity();
    }

    public <T> T sendRequestAndReadJson(Class<T> clazz) throws URISyntaxException, IOException {
        return GSON_PARSER.fromJson(IOStreamUtil.readStream(sendRequest().getContent()), clazz);
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

    @Builder
    public static class HttpStringEntityWrapper {
        @Builder.Default
        private String content = "";
        @Builder.Default
        private String charset = "UTF-8";
        @Builder.Default
        private String contentType = "application/json";
        public StringEntity toEntity() throws UnsupportedEncodingException {
            StringEntity entity = new StringEntity(content);
            entity.setContentEncoding(content);
            entity.setContentType(contentType);
            return entity;
        }
    }
}
