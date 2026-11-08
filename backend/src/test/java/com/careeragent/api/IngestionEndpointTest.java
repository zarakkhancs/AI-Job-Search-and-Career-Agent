package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.AppContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IngestionEndpointTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void importsAndListsJobPostings() {
        AppContext ctx = AppContext.withLlm(prompt -> "[]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            var created = client.post("/api/jobs",
                    Map.of("input", "Backend developer needed. Java, SQL and REST experience required."));

            assertEquals(200, created.code());
            JsonNode json = MAPPER.readTree(created.body().string());
            assertEquals(1, json.get("id").asInt());
            assertEquals("pasted text", json.get("source").asText());

            var list = client.get("/api/jobs");
            assertEquals(1, MAPPER.readTree(list.body().string()).size());
            assertEquals(200, client.get("/api/jobs/1").code());
        });
    }

    @Test
    void rejectsBadJobInput() {
        AppContext ctx = AppContext.withLlm(prompt -> "[]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            assertEquals(400, client.post("/api/jobs", Map.of("input", "  ")).code());
            assertEquals(400, client.post("/api/jobs", Map.of("input", "too short")).code());
            assertEquals(400, client.post("/api/jobs", Map.of("input", "http://localhost:1/job")).code());
            assertEquals(400, client.get("/api/jobs/9").code());
        });
    }

    @Test
    void importsResumeTextAndListsSkills() {
        AppContext ctx = AppContext.withLlm(prompt -> "[\"Java\"]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            var res = client.post("/api/resumes",
                    Map.of("name", "me.txt", "text", "Java developer with experience in backend systems."));

            assertEquals(200, res.code());
            JsonNode json = MAPPER.readTree(res.body().string());
            assertEquals("me.txt", json.get("filename").asText());
            assertTrue(json.get("skills").toString().contains("Java"));
            assertEquals(1, MAPPER.readTree(client.get("/api/resumes").body().string()).size());
        });
    }

    @Test
    void rejectsResumeWithoutText() {
        AppContext ctx = AppContext.withLlm(prompt -> "[]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            assertEquals(400, client.post("/api/resumes", Map.of("name", "x")).code());
            assertEquals(400, client.post("/api/resumes", Map.of("name", "x", "text", "short")).code());
        });
    }
}