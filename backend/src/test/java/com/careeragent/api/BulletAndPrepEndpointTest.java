package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.AppContext;
import com.careeragent.llm.MockLLMClient;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BulletAndPrepEndpointTest {
    private static io.javalin.Javalin appWith(String response) {
        return new ApiServer(AppContext.withLlm(new MockLLMClient(response))).create();
    }

    @Test
    void bulletEndpointReturnsVersions() {
        JavalinTest.test(appWith("{\"versions\":[\"Built a REST API\"]}"), (server, client) -> {
            var res = client.post("/api/bullets/optimize", Map.of("bullet", "Made an API"));
            assertEquals(200, res.code());
            assertTrue(res.body().string().contains("Built a REST API"));
        });
    }

    @Test
    void bulletEndpointRejectsBlankBullet() {
        JavalinTest.test(appWith("{}"), (server, client) -> {
            var res = client.post("/api/bullets/optimize", Map.of("bullet", "  "));
            assertEquals(400, res.code());
        });
    }

    @Test
    void bulletEndpointMapsBadModelOutputTo502() {
        JavalinTest.test(appWith("not json"), (server, client) -> {
            var res = client.post("/api/bullets/optimize", Map.of("bullet", "Made an API"));
            assertEquals(502, res.code());
        });
    }

    @Test
    void prepEndpointReturnsQuestions() {
        JavalinTest.test(appWith("{\"technical\":[\"What is REST?\"],\"behavioral\":[\"Tell me about teamwork.\"]}"),
                (server, client) -> {
                    var res = client.post("/api/prep", Map.of("job", "Backend developer"));
                    assertEquals(200, res.code());
                    assertTrue(res.body().string().contains("What is REST?"));
                });
    }

    @Test
    void prepEndpointRejectsBlankJob() {
        JavalinTest.test(appWith("{}"), (server, client) -> {
            var res = client.post("/api/prep", Map.of("job", " "));
            assertEquals(400, res.code());
        });
    }
}