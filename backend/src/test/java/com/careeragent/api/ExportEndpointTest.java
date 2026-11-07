package com.careeragent.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.AppContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.JavalinTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExportEndpointTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void exportReturnsFilenameAndContent() {
        AppContext ctx = AppContext.withLlm(prompt -> "[]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            Map<String, Object> body = Map.of(
                    "label", "Acme",
                    "resume", "My resume",
                    "summary", "My summary",
                    "coverLetter", "My letter",
                    "flaggedSkills", List.of("Java"));

            var res = client.post("/api/export", body);

            assertEquals(200, res.code());
            JsonNode json = MAPPER.readTree(res.body().string());
            assertEquals("application-package-acme.txt", json.get("filename").asText());
            String content = json.get("content").asText();
            assertTrue(content.contains("COVER LETTER"));
            assertTrue(content.contains("REVIEW BEFORE SENDING"));
        });
    }

    @Test
    void exportRejectsMissingResume() {
        AppContext ctx = AppContext.withLlm(prompt -> "[]");
        JavalinTest.test(new ApiServer(ctx).create(), (server, client) -> {
            var res = client.post("/api/export", Map.of("summary", "S", "coverLetter", "C"));

            assertEquals(400, res.code());
        });
    }
}