package com.careeragent.api;

import com.careeragent.AppContext;
import com.careeragent.alerts.Alert;
import com.careeragent.alerts.AlertSource;
import com.careeragent.pipeline.InvalidTransitionException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** HTTP layer for the Next.js GUI. Thin wrapper over the same services the CLI uses. */
public class ApiServer {
    record NewApplication(String company, String title) {}
    record MoveRequest(String action) {}
    record MatchRequest(List<String> resumeSkills, List<String> jobSkills) {}
    record AskRequest(String request) {}

    private final AppContext ctx;
    // AlertService and ApplicationService use plain collections, so serialize access.
    private final Object lock = new Object();

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public Javalin start(int port) {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors ->
                    cors.addRule(rule -> rule.allowHost("http://localhost:3000")));
        });

        app.get("/api/health", c -> c.json(Map.of("status", "ok")));

        // Applications
        app.get("/api/applications", c -> {
            String status = c.queryParam("status");
            c.json(locked(() -> status == null || status.isBlank()
                    ? ctx.applications.list() : ctx.applications.list(status)));
        });
        app.post("/api/applications", c -> {
            NewApplication body = c.bodyAsClass(NewApplication.class);
            require(body.company(), "company");
            require(body.title(), "title");
            Object created = locked(() -> {
                int id = ctx.applications.add(body.company().trim(), body.title().trim());
                return ctx.applications.list().stream().filter(v -> v.id() == id).findFirst().orElseThrow();
            });
            c.status(201).json(created);
        });
        app.post("/api/applications/{id}/move", c -> {
            int id = parseId(c);
            MoveRequest body = c.bodyAsClass(MoveRequest.class);
            c.json(locked(() -> ctx.applications.move(id, body.action())));
        });

        // Alerts (unread only; ?source=DEADLINE style filter uses the AlertSource enum name)
        app.get("/api/alerts", c -> {
            String source = c.queryParam("source");
            List<Alert> alerts = locked(() -> {
                if (source == null || source.isBlank()) {
                    return ctx.alerts.unread();
                }
                return ctx.alerts.unread(parseSource(source));
            });
            c.json(alerts.stream().map(ApiServer::alertJson).toList());
        });
        app.post("/api/alerts/{id}/read", c -> {
            int id = parseId(c);
            locked(() -> {
                ctx.alerts.markRead(id);
                return null;
            });
            c.json(Map.of("status", "ok"));
        });
        app.post("/api/alerts/read-all", c ->
                c.json(Map.of("marked", locked(() -> ctx.alerts.markAllRead()))));

        // Matching
        app.post("/api/match", c -> {
            MatchRequest body = c.bodyAsClass(MatchRequest.class);
            if (body.resumeSkills() == null || body.jobSkills() == null) {
                throw new IllegalArgumentException("resumeSkills and jobSkills are required.");
            }
            c.json(ctx.matcher.match(body.resumeSkills(), body.jobSkills()));
        });

        // Agent (calls the LLM, so it is not under the lock)
        app.post("/api/agent/ask", c -> {
            AskRequest body = c.bodyAsClass(AskRequest.class);
            require(body.request(), "request");
            c.json(Map.of("answer", ctx.agent.handle(body.request())));
        });

        app.exception(IllegalArgumentException.class, (e, c) ->
                c.status(400).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(InvalidTransitionException.class, (e, c) ->
                c.status(409).json(Map.of("error", String.valueOf(e.getMessage()))));

        return app.start(port);
    }

    private <T> T locked(Supplier<T> action) {
        synchronized (lock) {
            return action.get();
        }
    }

    private static int parseId(Context c) {
        try {
            return Integer.parseInt(c.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id must be a number.");
        }
    }

    private static AlertSource parseSource(String value) {
        try {
            return AlertSource.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown alert source '" + value + "'.");
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
    }

    private static Map<String, Object> alertJson(Alert a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.id());
        m.put("message", a.message());
        m.put("source", a.source().name());
        m.put("read", a.isRead());
        return m;
    }
}