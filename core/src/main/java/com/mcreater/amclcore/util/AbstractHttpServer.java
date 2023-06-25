package com.mcreater.amclcore.util;

import com.mcreater.amclcore.nbtlib.common.io.ExceptionFunction;
import fi.iki.elonen.NanoHTTPD;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHttpServer extends NanoHTTPD {
    private final Map<Route, ExceptionFunction<Map.Entry<IHTTPSession, Matcher>, Response, Exception>> routes = new HashMap<>();

    public AbstractHttpServer(int port) {
        super(port);
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
        for (Map.Entry<Route, ExceptionFunction<Map.Entry<IHTTPSession, Matcher>, Response, Exception>> ent : routes.entrySet()) {
            Matcher mat = ent.getKey().pattern.matcher(session.getUri());
            if (mat.find()) {
                try {
                    return ent.getValue().accept(new ImmutablePair<>(session, mat));
                } catch (Exception e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"message\": \"Internal Error\"}");
                }
            }
        }

        return super.serve(session);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class Route {
        private final Pattern pattern;
        private final Predicate<IHTTPSession> other;

        public static Route create(Pattern patt, Predicate<IHTTPSession> other) {
            return new Route(patt, other);
        }
    }
}
