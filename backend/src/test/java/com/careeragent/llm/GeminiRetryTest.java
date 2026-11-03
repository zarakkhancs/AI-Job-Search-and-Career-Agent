package com.careeragent.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeminiRetryTest {
    private static final String OK =
            "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hi there\"}]}}]}";

    /** Scripted transport: hands out queued responses and records calls and sleeps. */
    private static final class Script {
        final Deque<GeminiClient.RawResponse> queue = new ArrayDeque<>();
        final List<Long> sleeps = new ArrayList<>();
        int calls;

        Script(GeminiClient.RawResponse... responses) {
            for (GeminiClient.RawResponse r : responses) {
                queue.add(r);
            }
        }

        GeminiClient client() {
            return new GeminiClient("key", "model", request -> {
                calls++;
                return queue.isEmpty() ? new GeminiClient.RawResponse(500, "") : queue.poll();
            }, sleeps::add);
        }
    }

    private static GeminiClient.RawResponse r(int status, String body) {
        return new GeminiClient.RawResponse(status, body);
    }

    @Test
    void succeedsOnFirstTryWithoutSleeping() throws Exception {
        Script s = new Script(r(200, OK));
        assertEquals("Hi there", s.client().generate("hello"));
        assertEquals(1, s.calls);
        assertTrue(s.sleeps.isEmpty());
    }

    @Test
    void retriesTemporaryFailureThenSucceeds() throws Exception {
        Script s = new Script(r(503, ""), r(200, OK));
        assertEquals("Hi there", s.client().generate("hello"));
        assertEquals(2, s.calls);
        assertEquals(1, s.sleeps.size());
    }

    @Test
    void givesUpAfterMaxAttempts() {
        Script s = new Script(r(503, ""), r(503, ""), r(503, ""));
        LLMException e = assertThrows(LLMException.class, () -> s.client().generate("hello"));
        assertEquals(3, s.calls);
        assertTrue(e.getMessage().contains("503"));
    }

    @Test
    void backoffDelaysIncrease() {
        Script s = new Script(r(429, ""), r(429, ""), r(429, ""));
        assertThrows(LLMException.class, () -> s.client().generate("hello"));
        assertEquals(List.of(1000L, 2000L), s.sleeps);
    }

    @Test
    void clientErrorsAreNotRetried() {
        Script s = new Script(r(404, ""));
        assertThrows(LLMException.class, () -> s.client().generate("hello"));
        assertEquals(1, s.calls);
        assertTrue(s.sleeps.isEmpty());
    }

    @Test
    void errorMessageFromGoogleIsIncluded() {
        String body = "{\"error\":{\"code\":404,\"message\":\"Model not found\",\"status\":\"NOT_FOUND\"}}";
        Script s = new Script(r(404, body));
        LLMException e = assertThrows(LLMException.class, () -> s.client().generate("hello"));
        assertTrue(e.getMessage().contains("HTTP 404"));
        assertTrue(e.getMessage().contains("Model not found"));
    }

    @Test
    void nonJsonErrorBodyStillReportsStatus() {
        assertEquals("Gemini API returned HTTP 503", GeminiClient.describeError(503, "<html>oops</html>"));
        assertEquals("Gemini API returned HTTP 500", GeminiClient.describeError(500, null));
    }

    @Test
    void networkFailureIsWrappedAndNotRetried() {
        int[] calls = {0};
        GeminiClient client = new GeminiClient("key", "model", request -> {
            calls[0]++;
            throw new IOException("no route");
        }, millis -> { });
        LLMException e = assertThrows(LLMException.class, () -> client.generate("hello"));
        assertTrue(e.getMessage().contains("no route"));
        assertEquals(1, calls[0]);
    }
}
