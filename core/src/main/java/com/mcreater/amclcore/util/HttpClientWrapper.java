package com.mcreater.amclcore.util;

import com.mcreater.amclcore.exceptions.RequestException;
import lombok.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static HttpClientWrapper create(Method method) {
        return new HttpClientWrapper(method);
    }

    public HttpClientWrapper timeout(int timeout) {
        config.setConnectTimeout(timeout);
        return this;
    }

    public HttpClientWrapper socTimeout(int timeout) {
        config.setSocketTimeout(timeout);
        return this;
    }

    public HttpClientWrapper reqTimeout(int timeout) {
        config.setConnectionRequestTimeout(timeout);
        return this;
    }

    public HttpClientWrapper uriScheme(Scheme scheme) {
        requestURI.setScheme(scheme.getScheme());
        return this;
    }

    public HttpClientWrapper uri(String host, String path) {
        requestURI.setHost(host).setPath(path);
        return this;
    }

    public HttpClientWrapper uri(String url) {
        Pair<String, String> parsed = NetUtil.parseToPair(url);
        return uri(parsed.getKey(), parsed.getValue());
    }

    public HttpClientWrapper uriPort(int port) {
        requestURI.setPort(port);
        return this;
    }

    public HttpClientWrapper uriCharset(Charset charset) {
        requestURI.setCharset(charset);
        return this;
    }

    public HttpClientWrapper uriParam(String key, String value) {
        requestURI.addParameter(key, value);
        return this;
    }

    public HttpClientWrapper header(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    public HttpClientWrapper entity(HttpEntity entity) {
        if (request instanceof HttpEntityEnclosingRequestBase)
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        return this;
    }

    public HttpClientWrapper entityJson(String data) throws UnsupportedEncodingException {
        return entity(
                HttpStringEntityWrapper.builder()
                        .content(data)
                        .build().toEntity()
        );
    }

    public <T> HttpClientWrapper entityJson(T data) throws UnsupportedEncodingException {
        return entityJson(GSON_PARSER.toJson(data));
    }

    public HttpClientWrapper entityEncodedUrl(String data) throws UnsupportedEncodingException {
        return entity(
                HttpStringEntityWrapper.builder()
                        .contentType("application/x-www-form-urlencoded")
                        .content(data)
                        .build().toEntity()
        );
    }

    public HttpClientWrapper entityEncodedUrl(NameValuePair... pairs) throws UnsupportedEncodingException {
        return entityEncodedUrl(
                URLEncodedUtils.format(
                        Arrays.stream(pairs).collect(Collectors.toList()),
                        StandardCharsets.UTF_8
                )
        );
    }

    public HttpClientWrapper entityEncodedUrl(List<NameValuePair> pairs) throws UnsupportedEncodingException {
        return entityEncodedUrl(
                URLEncodedUtils.format(
                        pairs,
                        StandardCharsets.UTF_8
                )
        );
    }

    @SafeVarargs
    public final HttpClientWrapper entityEncodedURLEntry(Map.Entry<String, String>... pairs) throws UnsupportedEncodingException {
        return entityEncodedURLEntry(
                Arrays.stream(pairs).collect(Collectors.toList())
        );
    }

    public HttpClientWrapper entityEncodedURLEntry(List<Map.Entry<String, String>> pairs) throws UnsupportedEncodingException {
        return entityEncodedUrl(
                pairs.stream()
                        .map((Function<Map.Entry<String, String>, NameValuePair>) entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    public HttpClientWrapper entityEncodedURLEntry(Map<String, String> pairs) throws UnsupportedEncodingException {
        return entityEncodedURLEntry(
                new Vector<>(pairs.entrySet())
        );
    }

    public HttpClientWrapper cancel(Cancellable cancellable) {
        request.setCancellable(cancellable);
        return this;
    }

    public HttpClientWrapper cancel(boolean b) {
        return cancel(() -> b);
    }

    public HttpClientWrapper catchHttpExc(boolean catchHttpError) {
        this.catchHttpError = catchHttpError;
        return this;
    }

    public HttpEntity send() throws URISyntaxException, IOException {
        request.setURI(requestURI.build());
        request.setConfig(config.build());
        HttpResponse req = client.execute(request);
        if (req.getStatusLine().getStatusCode() > 399 && catchHttpError)
            throw new RequestException(req.getStatusLine(), req.getEntity());
        return req.getEntity();
    }

    public <T> T sendAndReadJson(Class<T> clazz) throws URISyntaxException, IOException {
        return GSON_PARSER.fromJson(IOStreamUtil.readStream(send().getContent()), clazz);
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
