package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.careeragent.AppContext;
import com.careeragent.llm.LLMClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class InterviewEndpointTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String QUESTIONS = "{\"questions\":["
            + "{\"type\":\"behavioral\",\"text\":\"Tell me about a team project.\"},"
            + "{\"type\":\"technical\",\"text\":\"Explain Docker.\"}]}";

    private static ApiServer server(String feedbackJson) {
        LLMClient llm = prompt -> {
            if (prompt.startsWith("Extract")) {
                return "[\"Java\",\"Docker\"]";
            }
            return prompt.startsWith("Write") ? QUESTIONS : feedbackJson;
        };
        return new ApiServer(AppContext.withLlm(llm));
    }

    private static final Map<String, Object> START = Map.of(
            "resume", "Wrote Java services.", "job", "Needs Java and Docker.", "count", 2);

    @Test
    void startAnswerAndFinishFlow() {
        JavalinTest.test(server("{\"score\":7,\"feedback\":\"Good.\"}").create(), (app, client) -> {
            Response start = client.post("/api/interview/start", START);
            assertEquals(201, start.code());
            JsonNode session = MAPPER.readTree(start.body().string());
            String id = session.get("id").asText();
            assertEquals(2, session.get("questions").size());

            Response ans = client.post("/api/interview/" + id + "/answer",
                    Map.of("questionId", 1, "answer", "I used Java and Docker."));
            assertEquals(200, ans.code());
            JsonNode fb = MAPPER.readTree(ans.body().string());
            assertEquals(7, fb.get("score").asInt());
            assertEquals("Docker", fb.get("flaggedClaims").get(0).asText());

            Response fin = client.post("/api/interview/" + id + "/finish", Map.of());
            assertEquals(200, fin.code());
            assertEquals(1, MAPPER.readTree(fin.body().string()).get("answered").asInt());

            Response late = client.post("/api/interview/" + id + "/answer",
                    Map.of("questionId", 2, "answer", "Too late."));
            assertEquals(409, late.code());
        });
    }

    @Test
    void unknownSessionReturns404() {
        JavalinTest.test(server("{}").create(), (app, client) ->
                assertEquals(404, client.get("/api/interview/nope").code()));
    }

    @Test
    void blankAnswerReturns400() {
        JavalinTest.test(server("{}").create(), (app, client) -> {
            JsonNode session = MAPPER.readTree(client.post("/api/interview/start", START).body().string());
            Response r = client.post("/api/interview/" + session.get("id").asText() + "/answer",
                    Map.of("questionId", 1, "answer", " "));
            assertEquals(400, r.code());
        });
    }

    @Test
    void startReturns502WhenModelOutputIsNotJson() {
        LLMClient llm = prompt -> "sorry";
        JavalinTest.test(new ApiServer(AppContext.withLlm(llm)).create(), (app, client) ->
                assertEquals(502, client.post("/api/interview/start", START).code()));
    }
}