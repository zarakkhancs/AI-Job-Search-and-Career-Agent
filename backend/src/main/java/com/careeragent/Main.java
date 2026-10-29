package com.careeragent;

import com.careeragent.agent.AgentController;
import com.careeragent.agent.ToolRegistry;
import com.careeragent.alerts.AlertService;
import com.careeragent.alerts.ConsoleAlertObserver;
import com.careeragent.cli.AgentCommand;
import com.careeragent.cli.AlertsCommand;
import com.careeragent.cli.AskCommand;
import com.careeragent.cli.CommandRegistry;
import com.careeragent.cli.HelpCommand;
import com.careeragent.config.AppConfig;
import com.careeragent.config.ConfigException;
import com.careeragent.extraction.ExtractSkillsTool;
import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.GeminiClient;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.careeragent.matching.JobMatcher;
import com.careeragent.matching.KeywordOverlapStrategy;
import com.careeragent.matching.MatchJobTool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
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
        Path envFile = Files.exists(Path.of(".env")) ? Path.of(".env") : Path.of("..", ".env");
        AppConfig config = AppConfig.load(envFile, System.getenv());
        LLMClient chosen;
        try {
            chosen = new GeminiClient(config.getApiKey(), config.getModel());
        } catch (ConfigException e) {
            String message = e.getMessage();
            chosen = prompt -> {
                throw new LLMException(message);
            };
        }
        final LLMClient llm = chosen;

        AlertService alerts = new AlertService();
        alerts.subscribe(new ConsoleAlertObserver(System.out));

        ToolRegistry tools = new ToolRegistry();
        tools.register(new ExtractSkillsTool(new SkillExtractor(llm)));
        tools.register(new MatchJobTool(new JobMatcher(new KeywordOverlapStrategy())));

        CommandRegistry registry = new CommandRegistry();
        registry.register(new HelpCommand(registry));
        registry.register(new AskCommand(llm));
        registry.register(new AgentCommand(new AgentController(llm, tools)));
        registry.register(new AlertsCommand(alerts));
        return registry;
    }
}
