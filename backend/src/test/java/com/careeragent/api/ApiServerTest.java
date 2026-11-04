package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.AppContext;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.JavalinTest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class ApiServerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ApiServer serverWith(LLMClient llm) {
        return new ApiServer(AppContext.withLlm(llm));
    }

    private static JsonNode json(Response r) throws IOException {
        return MAPPER.readTree(r.body().string());
    }

    @Test
    void healthReturnsOk() {
        JavalinTest.test(serverWith(p -> "[]").create(), (server, client) -> {
            Response r = client.get("/api/health");
            assertEquals(200, r.code());
            assertEquals("ok", json(r).get("status").asText());
        });
    }

    @Test
    void extractSplitsVerifiedFromRejectedSkills() {
        // The "LLM" claims Rust, which is not in the source text, so it must be rejected.
        LLMClient llm = p -> "[\"Java\",\"SQL\",\"Rust\"]";
        JavalinTest.test(serverWith(llm).create(), (server, client) -> {
            Response r = client.post("/api/extract", Map.of("text", "We need Java and SQL experience."));
            assertEquals(200, r.code());
            JsonNode body = json(r);
            assertEquals(2, body.get("verified").size());
            assertEquals("Java", body.get("verified").get(0).asText());
            assertEquals(1, body.get("rejected").size());
            assertEquals("Rust", body.get("rejected").get(0).asText());
        });
    }

    @Test
    void extractRejectsBlankText() {
        JavalinTest.test(serverWith(p -> "[]").create(), (server, client) -> {
            Response r = client.post("/api/extract", Map.of("text", "  "));
            assertEquals(400, r.code());
        });
    }

    @Test
    void extractReturns502WhenLlmOutputIsNotJson() {
        JavalinTest.test(serverWith(p -> "sorry, no idea").create(), (server, client) -> {
            Response r = client.post("/api/extract", Map.of("text", "Java developer"));
            assertEquals(502, r.code());
        });
    }

    @Test
    void extractReturns502WhenLlmFails() {
        LLMClient llm = p -> {
            throw new LLMException("model unavailable");
        };
        JavalinTest.test(serverWith(llm).create(), (server, client) -> {
            Response r = client.post("/api/extract", Map.of("text", "Java developer"));
            assertEquals(502, r.code());
            assertTrue(json(r).get("error").asText().contains("model unavailable"));
        });
    }

    @Test
    void deadlinesCanBeAddedAndListedInDateOrder() {
        JavalinTest.test(serverWith(p -> "[]").create(), (server, client) -> {
            assertEquals(201, client.post("/api/deadlines",
                    Map.of("label", "Later", "date", "2027-01-20")).code());
            assertEquals(201, client.post("/api/deadlines",
                    Map.of("label", "Sooner", "date", "2027-01-05")).code());
            JsonNode list = json(client.get("/api/deadlines"));
            assertEquals(2, list.size());
            assertEquals("Sooner", list.get(0).get("label").asText());
        });
    }

    @Test
    void deadlineWithBadDateIsRejected() {
        JavalinTest.test(serverWith(p -> "[]").create(), (server, client) -> {
            Response r = client.post("/api/deadlines", Map.of("label", "X", "date", "next friday"));
            assertEquals(400, r.code());
        });
    }

    @Test
    void deadlineCheckCreatesOneSchedulerAlertAndNotADuplicate() {
        JavalinTest.test(serverWith(p -> "[]").create(), (server, client) -> {
            String tomorrow = LocalDate.now().plusDays(1).toString();
            client.post("/api/deadlines", Map.of("label", "Submit Stage 2", "date", tomorrow));

            assertEquals(1, json(client.post("/api/deadlines/check", Map.of())).get("created").asInt());
            assertEquals(0, json(client.post("/api/deadlines/check", Map.of())).get("created").asInt());

            JsonNode alerts = json(client.get("/api/alerts?source=SCHEDULER"));
            assertEquals(1, alerts.size());
            assertTrue(alerts.get(0).get("message").asText().contains("Submit Stage 2"));
        });
    }
}