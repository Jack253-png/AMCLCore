package com.mcreater.amclcore.util;

import com.mcreater.amclcore.nbtlib.common.io.ExceptionFunction;
import fi.iki.elonen.NanoHTTPD;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

public abstract class AbstractHttpServer extends NanoHTTPD {
    private final Logger LOGGER = LogManager.getLogger(AbstractHttpServer.class);
    private final Map<Route, ExceptionFunction<Map.Entry<IHTTPSession, Matcher>, Response, Exception>> routes = new HashMap<>();

    public AbstractHttpServer(int port) {
        this(null, port);
    }

    public AbstractHttpServer(String hostname, int port) {
        super(hostname, port);
    }

    public String getHost() {
        return (getHostname() == null ? "localhost" : getHostname()) + ":" + getListeningPort();
    }

    public void addRoute(Route route, ExceptionFunction<Map.Entry<IHTTPSession, Matcher>, Response, Exception> func) {
        routes.put(route, func);
    }

    public void addSessionRoute(Route route, ExceptionFunction<IHTTPSession, Response, Exception> func) {
        routes.put(route, a -> func.accept(a.getKey()));
    }

    public void addMatcherRoute(Route route, ExceptionFunction<Matcher, Response, Exception> func) {
        routes.put(route, a -> func.accept(a.getValue()));
    }

    @Override
    public Response serve(IHTTPSession session) {
        String pat = session.getUri();
        for (Map.Entry<Route, ExceptionFunction<Map.Entry<IHTTPSession, Matcher>, Response, Exception>> ent : routes.entrySet()) {
            Matcher mat = ent.getKey().pattern.matcher(session.getUri());
            if (mat.find() && ent.getKey().other.test(session)) {
                try {
                    Response rep = ent.getValue().accept(new ImmutablePair<>(session, mat));
                    Objects.requireNonNull(rep);
                    LOGGER.info(translatable("core.server.response.success", pat, rep.getStatus().getRequestStatus()).getText());
                    return rep;
                } catch (Exception e) {
                    LOGGER.error(translatable("core.server.response.internalerr", pat).getText(), e);
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"message\": \"Internal Error\"}");
                }
            }
        }
        LOGGER.warn(translatable("core.server.response.notfound", pat).getText());
        return super.serve(session);
    }

    protected Response ok(Object response) {
        return newFixedLengthResponse(Response.Status.OK, "text/json", GSON_PARSER.toJson(response));
    }

    protected Response notFound() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "404 not found");
    }

    protected Response noContent() {
        return newFixedLengthResponse(Response.Status.NO_CONTENT, MIME_HTML, "{}");
    }

    protected Response badRequest() {
        String.format("respoding with code %d", Response.Status.BAD_REQUEST.getRequestStatus());
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_HTML, "400 bad request");
    }

    protected Response internalError() {
        String.format("respoding with code %d", Response.Status.INTERNAL_ERROR.getRequestStatus());
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_HTML, "500 internal error");
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class Route {
        public static final Predicate<IHTTPSession> IS_GET = a -> a.getMethod() == Method.GET;
        public static final Predicate<IHTTPSession> IS_POST = a -> a.getMethod() == Method.POST;
        private final Pattern pattern;
        private final Predicate<IHTTPSession> other;

        public static Route create(Pattern patt, Predicate<IHTTPSession> other) {
            return new Route(patt, other);
        }

        public static Route create(Pattern patt) {
            return create(patt, a -> true);
        }
    }
}
