package com.careeragent.prep;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.careeragent.prompts.Prompt;
import com.careeragent.prompts.PromptFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** F07: generates up to 5 technical and 3 behavioral interview questions for a job posting. */
public class PrepSheetService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static final int MAX_TECHNICAL = 5;
    static final int MAX_BEHAVIORAL = 3;

    private final LLMClient llm;
    private final PromptFactory factory;

    public PrepSheetService(LLMClient llm, PromptFactory factory) {
        this.llm = llm;
        this.factory = factory;
    }

    public PrepSheet generate(String job, String resume) throws LLMException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("job", job);
        inputs.put("resume", resume);
        Prompt prompt = factory.create(inputs);

        JsonNode root = parse(llm.generate(prompt.text()));
        List<String> technical = questions(root.path("technical"), MAX_TECHNICAL);
        List<String> behavioral = questions(root.path("behavioral"), MAX_BEHAVIORAL);
        if (technical.isEmpty() || behavioral.isEmpty()) {
            throw new PrepException("Prep response needs both technical and behavioral questions.");
        }
        return new PrepSheet(List.copyOf(technical), List.copyOf(behavioral));
    }

    private static List<String> questions(JsonNode arr, int max) {
        List<String> out = new ArrayList<>();
        if (!arr.isArray()) {
            return out;
        }
        for (JsonNode n : arr) {
            String q = n.isTextual()
                    ? n.asText().trim()
                    : n.path("text").asText(n.path("question").asText("")).trim();
            if (!q.isEmpty()) {
                out.add(q);
            }
            if (out.size() == max) {
                break;
            }
        }
        return out;
    }

    private static JsonNode parse(String raw) {
        String text = raw == null ? "" : raw;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new PrepException("Prep response contained no JSON object.");
        }
        try {
            return MAPPER.readTree(text.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new PrepException("Prep response was not valid JSON.", e);
        }
    }
}