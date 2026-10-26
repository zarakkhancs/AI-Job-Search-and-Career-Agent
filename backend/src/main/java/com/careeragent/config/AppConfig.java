package com.careeragent.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads local settings from a .env file, with real environment variables taking priority. */
public class AppConfig {
    public static final String API_KEY_NAME = "LLM_API_KEY";
    private static final String PLACEHOLDER = "YOUR_LOCAL_API_KEY_HERE";

    private final Map<String, String> values;

    public AppConfig(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static AppConfig load(Path envFile, Map<String, String> environment) throws IOException {
        Map<String, String> merged = new HashMap<>();
        if (Files.exists(envFile)) {
            merged.putAll(parse(Files.readAllLines(envFile)));
        }
        if (environment.containsKey(API_KEY_NAME)) {
            merged.put(API_KEY_NAME, environment.get(API_KEY_NAME));
        }
        return new AppConfig(merged);
    }

    static Map<String, String> parse(List<String> lines) {
        Map<String, String> result = new HashMap<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            result.put(key, value);
        }
        return result;
    }

    public String get(String key) {
        return values.get(key);
    }

    public String getApiKey() {
        String key = values.get(API_KEY_NAME);
        if (key == null || key.isBlank() || key.equals(PLACEHOLDER)) {
            throw new ConfigException(
                    "LLM_API_KEY is not set. Copy .env.example to .env and add your key.");
        }
        return key;
    }
}
