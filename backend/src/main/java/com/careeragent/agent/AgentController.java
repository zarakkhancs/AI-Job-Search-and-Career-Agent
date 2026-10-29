package com.careeragent.agent;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;

/** Coordinates plan, tool execution and final answer generation. */
public class AgentController {
    private final LLMClient llm;
    private final ToolRegistry tools;
    private final Planner planner;

    public AgentController(LLMClient llm, ToolRegistry tools) {
        this.llm = llm;
        this.tools = tools;
        this.planner = new Planner(llm, tools);
    }

    public String handle(String request) {
        try {
            PlanStep step = planner.plan(request);
            if (step.isDirectAnswer()) {
                return step.directAnswer();
            }
            Tool tool = tools.find(step.toolName()).orElseThrow();
            String result;
            try {
                result = tool.execute(step.args());
            } catch (RuntimeException e) {
                return "Tool '" + step.toolName() + "' failed: " + e.getMessage();
            }
            return llm.generate(String.join("\n",
                    "The user asked: " + request,
                    "The tool '" + step.toolName() + "' returned: " + result,
                    "Write a concise final answer using only the tool result. "
                            + "If the result does not contain the answer, say so."));
        } catch (PlanningException e) {
            return "I could not plan that request: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (LLMException e) {
            return "AI request failed: " + e.getMessage();
        }
    }
}
