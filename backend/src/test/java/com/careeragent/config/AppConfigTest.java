package com.careeragent.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

    @Test
    void parseIgnoresCommentsAndBlankLines() {
        Map<String, String> m = AppConfig.parse(List.of("# comment", "", "A=1", "B = 2"));
        assertEquals("1", m.get("A"));
        assertEquals("2", m.get("B"));
        assertEquals(2, m.size());
    }

    @Test
    void parseStripsQuotes() {
        Map<String, String> m = AppConfig.parse(List.of("LLM_API_KEY=\"abc123\""));
        assertEquals("abc123", m.get("LLM_API_KEY"));
    }

    @Test
    void getApiKeyReturnsKeyWhenPresent() {
        AppConfig cfg = new AppConfig(Map.of("LLM_API_KEY", "real-key"));
        assertEquals("real-key", cfg.getApiKey());
    }

    @Test
    void getApiKeyFailsWhenMissing() {
        AppConfig cfg = new AppConfig(Map.of());
        assertThrows(ConfigException.class, cfg::getApiKey);
    }

    @Test
    void getApiKeyFailsOnPlaceholder() {
        AppConfig cfg = new AppConfig(Map.of("LLM_API_KEY", "YOUR_LOCAL_API_KEY_HERE"));
        assertThrows(ConfigException.class, cfg::getApiKey);
    }

    @Test
    void environmentVariableOverridesFile(@TempDir Path dir) throws IOException {
        Path env = dir.resolve(".env");
        Files.writeString(env, "LLM_API_KEY=from-file\n");
        AppConfig cfg = AppConfig.load(env, Map.of("LLM_API_KEY", "from-env"));
        assertEquals("from-env", cfg.getApiKey());
    }

    @Test
    void loadWorksWhenFileDoesNotExist(@TempDir Path dir) throws IOException {
        AppConfig cfg = AppConfig.load(dir.resolve("missing.env"), Map.of());
        assertThrows(ConfigException.class, cfg::getApiKey);
    }
}
