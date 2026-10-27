package com.careeragent.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Adapter that makes the Gemini REST API look like an LLMClient. */
public class GeminiClient implements LLMClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private final String apiKey;
    private final String model;
    private final HttpClient http;

    public GeminiClient(String apiKey, String model) {
        this(apiKey, model, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build());
    }

    GeminiClient(String apiKey, String model, HttpClient http) {
        this.apiKey = apiKey;
        this.model = model;
        this.http = http;
    }

    @Override
    public String generate(String prompt) throws LLMException {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMException("Prompt must not be empty.");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + model + ":generateContent"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LLMException("Gemini API returned HTTP " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (IOException e) {
            throw new LLMException("Could not reach Gemini API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMException("Request was interrupted.", e);
        }
    }

    static String buildRequestBody(String prompt) throws LLMException {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.putArray("contents").addObject().putArray("parts").addObject().put("text", prompt);
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new LLMException("Could not build request body.", e);
        }
    }

    static String parseResponse(String json) throws LLMException {
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text.isMissingNode() || text.asText().isBlank()) {
                throw new LLMException("Gemini response contained no text.");
            }
            return text.asText();
        } catch (JsonProcessingException e) {
            throw new LLMException("Gemini response was not valid JSON.", e);
        }
    }
}
