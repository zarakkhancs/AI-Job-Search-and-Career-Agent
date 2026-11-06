package com.careeragent.optimizer;

import com.careeragent.agent.Tool;
import com.careeragent.llm.LLMException;
import java.util.Map;

/** Agent tool wrapping BulletOptimizerService. */
public class OptimizeBulletTool implements Tool {
    private final BulletOptimizerService service;

    public OptimizeBulletTool(BulletOptimizerService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "optimize_bullet";
    }

    @Override
    public String description() {
        return "Rewrite a resume bullet point three ways and flag invented figures. Args: bullet, job (optional)";
    }

    @Override
    public String execute(Map<String, String> args) {
        String bullet = args.get("bullet");
        if (bullet == null || bullet.isBlank()) {
            throw new IllegalArgumentException("optimize_bullet requires a 'bullet' argument.");
        }
        try {
            return service.optimize(bullet, args.get("job")).describe();
        } catch (LLMException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        }
    }
}