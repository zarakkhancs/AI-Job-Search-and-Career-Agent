package com.careeragent.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GeminiClientTest {

    @Test
    void requestBodyContainsPromptInGeminiShape() throws Exception {
        String body = GeminiClient.buildRequestBody("Hello \"world\"");
        assertTrue(body.contains("\"contents\""));
        assertTrue(body.contains("\"parts\""));
        assertTrue(body.contains("Hello \\\"world\\\""));
    }

    @Test
    void parseResponseExtractsText() throws Exception {
        String json = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hi there\"}]}}]}";
        assertEquals("Hi there", GeminiClient.parseResponse(json));
    }

    @Test
    void parseResponseFailsWhenNoCandidates() {
        assertThrows(LLMException.class, () -> GeminiClient.parseResponse("{\"candidates\":[]}"));
    }

    @Test
    void parseResponseFailsOnInvalidJson() {
        assertThrows(LLMException.class, () -> GeminiClient.parseResponse("not json"));
    }

    @Test
    void generateRejectsBlankPromptWithoutCallingNetwork() {
        GeminiClient client = new GeminiClient("key", "model");
        assertThrows(LLMException.class, () -> client.generate("   "));
    }
}
