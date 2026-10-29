package com.careeragent.extraction;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Uses the LLM to extract skills, then verifies each one against the source text. */
public class SkillExtractor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LLMClient llm;

    public SkillExtractor(LLMClient llm) {
        this.llm = llm;
    }

    public ExtractionResult extract(String text) throws LLMException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank.");
        }
        String raw = llm.generate(buildPrompt(text));
        Set<String> claimed = parseSkills(raw);
        List<String> verified = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (String skill : claimed) {
            if (SourceVerifier.isSupported(skill, text)) {
                verified.add(skill);
            } else {
                rejected.add(skill);
            }
        }
        return new ExtractionResult(List.copyOf(verified), List.copyOf(rejected));
    }

    String buildPrompt(String text) {
        return String.join("\n",
                "Extract the technical and professional skills mentioned in the text below.",
                "Only list skills that are explicitly written in the text. Do not infer or add skills.",
                "Respond with a JSON array of strings only, for example: [\"Java\",\"SQL\"]",
                "Text:",
                text);
    }

    Set<String> parseSkills(String raw) {
        String json = raw == null ? "" : raw;
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new ExtractionException("Extractor response contained no JSON array.");
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(json.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new ExtractionException("Extractor response was not valid JSON.", e);
        }
        if (!root.isArray()) {
            throw new ExtractionException("Extractor response was not a JSON array.");
        }
        Set<String> skills = new LinkedHashSet<>();
        for (JsonNode n : root) {
            String s = n.asText("").trim();
            if (!s.isEmpty()) {
                skills.add(s);
            }
        }
        return skills;
    }
}
