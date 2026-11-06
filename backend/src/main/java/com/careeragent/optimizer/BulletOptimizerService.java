package com.careeragent.optimizer;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.careeragent.prompts.Prompt;
import com.careeragent.prompts.PromptFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * F08: the LLM rewrites a resume bullet (prompt built by a PromptFactory), then a deterministic
 * check flags any number in a rewrite that does not appear in the original bullet.
 */
public class BulletOptimizerService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern FIGURE = Pattern.compile("\\d+(?:[.,]\\d+)*%?");

    private final LLMClient llm;
    private final PromptFactory factory;

    public BulletOptimizerService(LLMClient llm, PromptFactory factory) {
        this.llm = llm;
        this.factory = factory;
    }

    public BulletResult optimize(String bullet, String job) throws LLMException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("bullet", bullet);
        inputs.put("job", job);
        Prompt prompt = factory.create(inputs);

        JsonNode arr = parse(llm.generate(prompt.text())).path("versions");
        if (!arr.isArray()) {
            throw new OptimizerException("Optimizer response was missing 'versions'.");
        }
        Set<String> allowed = new HashSet<>();
        for (String f : figures(bullet)) {
            allowed.add(stripPercent(f));
        }
        List<BulletVersion> versions = new ArrayList<>();
        for (JsonNode n : arr) {
            String text = n.asText("").trim();
            if (text.isEmpty()) {
                continue;
            }
            List<String> unsupported = new ArrayList<>();
            for (String f : figures(text)) {
                if (!allowed.contains(stripPercent(f))) {
                    unsupported.add(f);
                }
            }
            versions.add(new BulletVersion(text, List.copyOf(unsupported)));
            if (versions.size() == 3) {
                break;
            }
        }
        if (versions.isEmpty()) {
            throw new OptimizerException("Optimizer response had no usable versions.");
        }
        return new BulletResult(bullet.trim(), List.copyOf(versions));
    }

    static Set<String> figures(String text) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = FIGURE.matcher(text);
        while (m.find()) {
            out.add(m.group());
        }
        return out;
    }

    private static String stripPercent(String figure) {
        return figure.endsWith("%") ? figure.substring(0, figure.length() - 1) : figure;
    }

    private static JsonNode parse(String raw) {
        String text = raw == null ? "" : raw;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new OptimizerException("Optimizer response contained no JSON object.");
        }
        try {
            return MAPPER.readTree(text.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new OptimizerException("Optimizer response was not valid JSON.", e);
        }
    }
}