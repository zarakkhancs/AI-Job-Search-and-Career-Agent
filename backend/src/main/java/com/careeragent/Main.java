package com.careeragent;

import com.careeragent.cli.AskCommand;
import com.careeragent.cli.CommandRegistry;
import com.careeragent.cli.HelpCommand;
import com.careeragent.config.AppConfig;
import com.careeragent.config.ConfigException;
import com.careeragent.llm.GeminiClient;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
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
        LLMClient llm;
        try {
            llm = new GeminiClient(config.getApiKey(), config.getModel());
        } catch (ConfigException e) {
            String message = e.getMessage();
            llm = prompt -> {
                throw new LLMException(message);
            };
        }
        CommandRegistry registry = new CommandRegistry();
        registry.register(new HelpCommand(registry));
        registry.register(new AskCommand(llm));
        registry.register(new com.careeragent.cli.AlertsCommand(new com.careeragent.alerts.AlertService()));
        return registry;
    }
}

