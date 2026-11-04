package com.careeragent.api;

import com.careeragent.AppContext;
import io.javalin.Javalin;
import java.util.Map;

/** HTTP layer for the Next.js GUI. Thin wrapper over the same services the CLI uses. */
public class ApiServer {
    private final AppContext ctx;

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public Javalin start(int port) {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors ->
                    cors.addRule(rule -> rule.allowHost("http://localhost:3000")));
        });

        app.get("/api/health", c -> c.json(Map.of("status", "ok")));

        app.exception(IllegalArgumentException.class, (e, c) ->
                c.status(400).json(Map.of("error", e.getMessage())));

        return app.start(port);
    }
}