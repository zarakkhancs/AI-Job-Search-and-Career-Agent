package com.careeragent.cli;

import com.careeragent.llm.LLMException;
import com.careeragent.optimizer.BulletOptimizerService;
import com.careeragent.optimizer.OptimizerException;
import java.util.List;

/** CLI for F08: bullet <text> [| <job posting text>]. */
public class BulletCommand implements Command {
    static final String USAGE = "Usage: bullet <resume bullet text> [| <job posting text>]";

    private final BulletOptimizerService service;

    public BulletCommand(BulletOptimizerService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "bullet";
    }

    @Override
    public String description() {
        return "Rewrite a resume bullet three ways: bullet <text> [| <job posting>]";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        String joined = String.join(" ", args);
        String bullet = joined;
        String job = null;
        if (joined.contains("|")) {
            String[] parts = joined.split("\\|", 2);
            bullet = parts[0].trim();
            job = parts[1].trim();
        }
        if (bullet.isEmpty()) {
            return USAGE;
        }
        try {
            return service.optimize(bullet, job).describe();
        } catch (LLMException | OptimizerException e) {
            return "Bullet optimizer failed: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}