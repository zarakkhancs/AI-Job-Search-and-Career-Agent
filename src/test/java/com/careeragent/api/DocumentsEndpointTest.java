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

class DocumentsEndpointTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ApiServer server(String documentJson) {
        LLMClient llm = prompt -> prompt.startsWith("Extract") ? "[\"Java\",\"Docker\"]" : documentJson;
        return new ApiServer(AppContext.withLlm(llm));
    }

    @Test
    void tailorReturnsDocumentWithFlaggedSkills() {
        String doc = "{\"summary\":\"Java developer.\",\"coverLetter\":\"I also know Docker.\"}";
        JavalinTest.test(server(doc).create(), (app, client) -> {
            Response r = client.post("/api/documents/tailor",
                    Map.of("resume", "Wrote Java services.", "job", "Needs Java and Docker."));
            assertEquals(200, r.code());
            JsonNode body = MAPPER.readTree(r.body().string());
            assertEquals("Java", body.get("supportedSkills").get(0).asText());
            assertEquals("Docker", body.get("flaggedSkills").get(0).asText());
        });
    }

    @Test
    void tailorRejectsBlankResume() {
        JavalinTest.test(server("{}").create(), (app, client) -> {
            Response r = client.post("/api/documents/tailor", Map.of("resume", " ", "job", "Needs Java."));
            assertEquals(400, r.code());
        });
    }

    @Test
    void tailorReturns502WhenModelOutputIsNotJson() {
        JavalinTest.test(server("sorry").create(), (app, client) -> {
            Response r = client.post("/api/documents/tailor",
                    Map.of("resume", "Wrote Java services.", "job", "Needs Java and Docker."));
            assertEquals(502, r.code());
        });
    }
}