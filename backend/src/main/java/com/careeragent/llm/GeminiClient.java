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
import java.util.Set;

/** Adapter that makes the Gemini REST API look like an LLMClient, with retry on temporary failures. */
public class GeminiClient implements LLMClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Set<Integer> RETRYABLE = Set.of(429, 500, 502, 503, 504);
    static final int MAX_ATTEMPTS = 3;
    static final long BASE_DELAY_MS = 1000;
    private static final int MAX_ERROR_TEXT = 300;

    /** The raw HTTP outcome, so tests can script responses without a network. */
    record RawResponse(int status, String body) {
    }

    interface Transport {
        RawResponse send(HttpRequest request) throws IOException, InterruptedException;
    }

    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final String apiKey;
    private final String model;
    private final Transport transport;
    private final Sleeper sleeper;

    public GeminiClient(String apiKey, String model) {
        this(apiKey, model, httpTransport(), Thread::sleep);
    }

    GeminiClient(String apiKey, String model, Transport transport, Sleeper sleeper) {
        this.apiKey = apiKey;
        this.model = model;
        this.transport = transport;
        this.sleeper = sleeper;
    }

    private static Transport httpTransport() {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        return request -> {
            HttpResponse<String> r = http.send(request, HttpResponse.BodyHandlers.ofString());
            return new RawResponse(r.statusCode(), r.body());
        };
    }

    @Override
    public String generate(String prompt) throws LLMException {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMException("Prompt must not be empty.");
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + model + ":generateContent"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
                .build();
        try {
            RawResponse response = null;
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                response = transport.send(request);
                if (!RETRYABLE.contains(response.status()) || attempt == MAX_ATTEMPTS) {
                    break;
                }
                sleeper.sleep(BASE_DELAY_MS * attempt);
            }
            if (response.status() != 200) {
                throw new LLMException(describeError(response.status(), response.body()));
            }
            return parseResponse(response.body());
        } catch (IOException e) {
            throw new LLMException("Could not reach Gemini API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMException("Request was interrupted.", e);
        }
    }

    static String describeError(int status, String body) {
        String base = "Gemini API returned HTTP " + status;
        try {
            String message = MAPPER.readTree(body == null ? "" : body).path("error").path("message").asText("");
            if (!message.isBlank()) {
                String trimmed = message.length() > MAX_ERROR_TEXT
                        ? message.substring(0, MAX_ERROR_TEXT) + "..." : message;
                return base + ": " + trimmed;
            }
        } catch (JsonProcessingException e) {
            // Body was not JSON; fall back to the status code alone.
        }
        return base;
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
