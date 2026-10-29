package com.careeragent.agent;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;

/** Asks the LLM to choose a tool (or answer directly) and validates the decision. */
public class Planner {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LLMClient llm;
    private final ToolRegistry tools;

    public Planner(LLMClient llm, ToolRegistry tools) {
        this.llm = llm;
        this.tools = tools;
    }

    public PlanStep plan(String request) throws LLMException {
        if (request == null || request.isBlank()) {
            throw new IllegalArgumentException("Request must not be blank.");
        }
        return parse(llm.generate(buildPrompt(request)));
    }

    String buildPrompt(String request) {
        return String.join("\n",
                "You are the planner for a job search and career assistant.",
                "Choose exactly one tool for the user's request, or answer directly if no tool is needed.",
                "Available tools:",
                tools.describeAll(),
                "Respond with JSON only, in one of these two forms:",
                "{\"tool\":\"<tool name>\",\"args\":{\"<arg>\":\"<value>\"}}",
                "{\"tool\":\"none\",\"answer\":\"<your answer>\"}",
                "User request: " + request);
    }

    PlanStep parse(String raw) {
        String json = raw == null ? "" : raw;
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new PlanningException("Planner response contained no JSON object.");
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(json.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new PlanningException("Planner response was not valid JSON.", e);
        }
        String tool = root.path("tool").asText("");
        if (tool.isBlank()) {
            throw new PlanningException("Planner response had no tool field.");
        }
        if (tool.equals("none")) {
            String answer = root.path("answer").asText("");
            if (answer.isBlank()) {
                throw new PlanningException("Planner chose no tool but gave no answer.");
            }
            return PlanStep.answer(answer);
        }
        if (tools.find(tool).isEmpty()) {
            throw new PlanningException("Planner chose unknown tool: " + tool);
        }
        Map<String, String> args = new LinkedHashMap<>();
        JsonNode argsNode = root.path("args");
        argsNode.fieldNames().forEachRemaining(name -> args.put(name, argsNode.get(name).asText()));
        return PlanStep.toolCall(tool, args);
    }
}
