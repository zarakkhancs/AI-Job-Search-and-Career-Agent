package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.AppContext;
import com.careeragent.llm.MockLLMClient;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnalyticsEndpointTest {
    @Test
    void analyticsReflectsTrackedApplications() {
        var app = new ApiServer(AppContext.withLlm(new MockLLMClient())).create();
        JavalinTest.test(app, (server, client) -> {
            var created = client.post("/api/applications", Map.of("company", "Acme", "title", "Dev"));
            assertEquals(201, created.code());
            var res = client.get("/api/analytics");
            assertEquals(200, res.code());
            String body = res.body().string();
            assertTrue(body.contains("\"total\":1"), body);
            assertTrue(body.contains("\"SAVED\":1"), body);
        });
    }
}