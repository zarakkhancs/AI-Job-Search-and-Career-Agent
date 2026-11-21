package com.careeragent;

import com.careeragent.alerts.ConsoleAlertObserver;
import com.careeragent.api.ApiServer;
import com.careeragent.cli.AgentCommand;
import com.careeragent.cli.AlertsCommand;
import com.careeragent.cli.AskCommand;
import com.careeragent.cli.BulletCommand;
import com.careeragent.cli.CliRunner;
import com.careeragent.cli.CommandRegistry;
import com.careeragent.cli.ExportCommand;
import com.careeragent.cli.HelpCommand;
import com.careeragent.cli.InterviewCommand;
import com.careeragent.cli.JobCommand;
import com.careeragent.cli.PrepCommand;
import com.careeragent.cli.ResumeCommand;
import com.careeragent.cli.StatsCommand;
import com.careeragent.cli.TrackCommand;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equalsIgnoreCase("serve")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 7070;
            new ApiServer(AppContext.load()).start(port);
            System.out.println("API running on http://localhost:" + port);
            return;
        }
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println(CliRunner.USAGE);
            return;
        }
        AppContext ctx = AppContext.load();
        ctx.alerts.subscribe(new ConsoleAlertObserver(System.out));
        CliRunner runner = new CliRunner(buildRegistry(ctx), ctx.agent::handle, System.in, System.out, System.err);
        int code = runner.run(args);
        if (code != 0) {
            System.exit(code);
        }
    }

    static CommandRegistry buildRegistry(AppContext ctx) {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new HelpCommand(registry));
        registry.register(new AskCommand(ctx.llm));
        registry.register(new AgentCommand(ctx.agent));
        registry.register(new AlertsCommand(ctx.alerts));
        registry.register(new TrackCommand(ctx.applications));
        registry.register(new StatsCommand(ctx.metrics));
        registry.register(new BulletCommand(ctx.bullets));
        registry.register(new PrepCommand(ctx.prep));
        registry.register(new ExportCommand(ctx.documents, ctx.exports, Path.of("exports")));
        registry.register(new InterviewCommand(ctx.interviews));
        registry.register(new JobCommand(ctx.jobs));
        registry.register(new ResumeCommand(ctx.resumes));
        return registry;
    }
}