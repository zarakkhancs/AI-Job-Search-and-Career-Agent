package com.careeragent;

import com.careeragent.alerts.ConsoleAlertObserver;
import com.careeragent.api.ApiServer;
import com.careeragent.cli.AgentCommand;
import com.careeragent.cli.AlertsCommand;
import com.careeragent.cli.AskCommand;
import com.careeragent.cli.CommandRegistry;
import com.careeragent.cli.HelpCommand;
import com.careeragent.cli.StatsCommand;
import com.careeragent.cli.TrackCommand;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equalsIgnoreCase("serve")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 7070;
            new ApiServer(AppContext.load()).start(port);
            System.out.println("API running on http://localhost:" + port);
            return;
        }
        CommandRegistry registry = buildRegistry();
        if (args.length > 0) {
            System.out.println(registry.dispatch(String.join(" ", args)));
            return;
        }
        System.out.println("AI Job Search and Career Agent. Type 'help' for commands, 'exit' to quit.");
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!in.hasNextLine()) {
                break;
            }
            String line = in.nextLine().trim();
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                break;
            }
            String out = registry.dispatch(line);
            if (!out.isEmpty()) {
                System.out.println(out);
            }
        }
    }

    static CommandRegistry buildRegistry() throws IOException {
        AppContext ctx = AppContext.load();
        ctx.alerts.subscribe(new ConsoleAlertObserver(System.out));

        CommandRegistry registry = new CommandRegistry();
        registry.register(new HelpCommand(registry));
        registry.register(new AskCommand(ctx.llm));
        registry.register(new AgentCommand(ctx.agent));
        registry.register(new AlertsCommand(ctx.alerts));
        registry.register(new TrackCommand(ctx.applications));
        registry.register(new StatsCommand(ctx.metrics));
        return registry;
    }
}