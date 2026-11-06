package com.careeragent.api;

import com.careeragent.AppContext;
import com.careeragent.alerts.Alert;
import com.careeragent.alerts.AlertSource;
import com.careeragent.alerts.Deadline;
import com.careeragent.documents.DocumentException;
import com.careeragent.extraction.ExtractionException;
import com.careeragent.interview.InterviewException;
import com.careeragent.interview.InterviewNotFoundException;
import com.careeragent.interview.InterviewSession;
import com.careeragent.llm.LLMException;
import com.careeragent.optimizer.OptimizerException;
import com.careeragent.prep.PrepException;
import com.careeragent.pipeline.InvalidTransitionException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
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
    record ExtractRequest(String text) {}
    record DeadlineRequest(String label, String date) {}
    record TailorRequest(String resume, String job) {}
    record StartInterviewRequest(String resume, String job, Integer count) {}
    record AnswerRequest(Integer questionId, String answer) {}
    record BulletRequest(String bullet, String job) {}
    record PrepRequest(String job, String resume) {}

    private final AppContext ctx;
    // AlertService, ApplicationService and the deadline list use plain collections,
    // so serialize access.
    private final Object lock = new Object();

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public Javalin start(int port) {
        return create().start(port);
    }

    /** Builds the app with all routes but does not start it (tests start it themselves). */
    public Javalin create() {
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

        // Deadlines
        app.get("/api/deadlines", c -> c.json(locked(() -> ctx.deadlines.stream()
                .sorted(Comparator.comparing(Deadline::date))
                .map(ApiServer::deadlineJson)
                .toList())));
        app.post("/api/deadlines", c -> {
            DeadlineRequest body = c.bodyAsClass(DeadlineRequest.class);
            require(body.label(), "label");
            require(body.date(), "date");
            Deadline deadline = new Deadline(body.label().trim(), parseDate(body.date()));
            locked(() -> {
                ctx.deadlines.add(deadline);
                return null;
            });
            c.status(201).json(deadlineJson(deadline));
        });
        // Scans deadlines and publishes SCHEDULER alerts for ones coming up soon.
        app.post("/api/deadlines/check", c -> c.json(Map.of("created", locked(() ->
                ctx.scheduler.check(List.copyOf(ctx.deadlines), LocalDate.now())))));

        // Analytics (deterministic metrics over the pipeline)
        app.get("/api/analytics", c -> c.json(locked(() -> ctx.metrics.compute())));

        // Matching
        app.post("/api/match", c -> {
            MatchRequest body = c.bodyAsClass(MatchRequest.class);
            if (body.resumeSkills() == null || body.jobSkills() == null) {
                throw new IllegalArgumentException("resumeSkills and jobSkills are required.");
            }
            c.json(ctx.matcher.match(body.resumeSkills(), body.jobSkills()));
        });

        // Skill extraction (LLM call, then each skill is verified against the source text)
        app.post("/api/extract", c -> {
            ExtractRequest body = c.bodyAsClass(ExtractRequest.class);
            require(body.text(), "text");
            c.json(ctx.extractor.extract(body.text()));
        });

        // Tailored resume summary and cover letter, with a factual check against the resume
        app.post("/api/documents/tailor", c -> {
            TailorRequest body = c.bodyAsClass(TailorRequest.class);
            require(body.resume(), "resume");
            require(body.job(), "job");
            c.json(ctx.documents.tailor(body.resume(), body.job()));
        });

        // Mock interview (LLM calls, so not under the lock)
        app.post("/api/interview/start", c -> {
            StartInterviewRequest body = c.bodyAsClass(StartInterviewRequest.class);
            require(body.resume(), "resume");
            require(body.job(), "job");
            int count = body.count() == null ? 5 : body.count();
            c.status(201).json(sessionJson(ctx.interviews.start(body.resume(), body.job(), count)));
        });
        app.get("/api/interview/{id}", c ->
                c.json(sessionJson(ctx.interviews.get(c.pathParam("id")))));
        app.post("/api/interview/{id}/answer", c -> {
            AnswerRequest body = c.bodyAsClass(AnswerRequest.class);
            if (body.questionId() == null) {
                throw new IllegalArgumentException("questionId is required.");
            }
            require(body.answer(), "answer");
            c.json(ctx.interviews.answer(c.pathParam("id"), body.questionId(), body.answer()));
        });
        app.post("/api/interview/{id}/finish", c ->
                c.json(ctx.interviews.finish(c.pathParam("id"))));

        // Bullet optimizer (LLM call, so not under the lock)
        app.post("/api/bullets/optimize", c -> {
            BulletRequest body = c.bodyAsClass(BulletRequest.class);
            require(body.bullet(), "bullet");
            c.json(ctx.bullets.optimize(body.bullet(), body.job()));
        });

        // Interview prep sheet (LLM call)
        app.post("/api/prep", c -> {
            PrepRequest body = c.bodyAsClass(PrepRequest.class);
            require(body.job(), "job");
            c.json(ctx.prep.generate(body.job(), body.resume()));
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
        app.exception(LLMException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(ExtractionException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(DocumentException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(InterviewNotFoundException.class, (e, c) ->
                c.status(404).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(InterviewException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(OptimizerException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(PrepException.class, (e, c) ->
                c.status(502).json(Map.of("error", String.valueOf(e.getMessage()))));
        app.exception(IllegalStateException.class, (e, c) ->
                c.status(409).json(Map.of("error", String.valueOf(e.getMessage()))));

        return app;
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

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("date must look like 2026-11-10.");
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

    private static Map<String, Object> deadlineJson(Deadline d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", d.label());
        m.put("date", d.date().toString());
        return m;
    }

    // Keeps the resume and job text out of the response.
    private static Map<String, Object> sessionJson(InterviewSession s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("status", s.getStatus().name());
        m.put("questions", s.getQuestions());
        m.put("feedback", s.getFeedback());
        return m;
    }
}