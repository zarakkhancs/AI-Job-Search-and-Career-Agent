package com.careeragent.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Command-line front end. Runs one command, a task file sent to the agent, a script of commands,
 * standard input, or an interactive session. Kept separate from Main so it can be tested.
 */
public class CliRunner {
    public static final String USAGE = String.join("\n",
            "AI Job Search and Career Agent - command line",
            "",
            "Usage:",
            "  java -jar career-agent.jar                     interactive session ('help', 'exit')",
            "  java -jar career-agent.jar <command> [args]    run one command, for example: track list",
            "  java -jar career-agent.jar --input <file|->    send the whole file (or stdin) to the agent as one request",
            "  java -jar career-agent.jar --script <file|->   run one command per line (blank lines and # comments skipped)",
            "  java -jar career-agent.jar serve [port]        start the HTTP API for the GUI (default port 7070)",
            "  java -jar career-agent.jar --help              show this help",
            "",
            "Short forms: -i for --input, -s for --script, -h for --help. Use - as the file name to read stdin.",
            "The Gemini key is read from .env (see the README).",
            "Exit codes: 0 success, 1 AI or runtime failure, 2 usage or file error.");

    private final CommandRegistry registry;
    private final Function<String, String> agent;
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    public CliRunner(CommandRegistry registry, Function<String, String> agent,
                     InputStream in, PrintStream out, PrintStream err) {
        this.registry = registry;
        this.agent = agent;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    /** Runs the CLI for the given arguments and returns the process exit code. */
    public int run(String[] args) {
        if (args.length == 0) {
            interactive();
            return 0;
        }
        String first = args[0];
        switch (first) {
            case "--help":
            case "-h":
                out.println(USAGE);
                return 0;
            case "--input":
            case "-i":
                return fromFile(args, true);
            case "--script":
            case "-s":
                return fromFile(args, false);
            default:
                if (first.startsWith("-") && first.length() > 1) {
                    err.println("Unknown option: " + first);
                    err.println(USAGE);
                    return 2;
                }
                out.println(registry.dispatch(String.join(" ", args)));
                return 0;
        }
    }

    private int fromFile(String[] args, boolean asAgentTask) {
        if (args.length < 2) {
            err.println("Missing file name after " + args[0] + " (use - for standard input).");
            return 2;
        }
        if (args.length > 2) {
            err.println("Unexpected extra arguments after " + args[1] + ".");
            return 2;
        }
        String text = readText(args[1]);
        if (text == null) {
            return 2;
        }
        if (text.isBlank()) {
            err.println("Input is empty: " + args[1]);
            return 2;
        }
        try {
            if (asAgentTask) {
                out.println(agent.apply(text.strip()));
            } else {
                runScript(text);
            }
            return 0;
        } catch (RuntimeException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void runScript(String text) {
        for (String raw : text.split("\\R")) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                return;
            }
            out.println("> " + line);
            String result = registry.dispatch(line);
            if (!result.isEmpty()) {
                out.println(result);
            }
        }
    }

    private String readText(String name) {
        try {
            if (name.equals("-")) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            return Files.readString(Path.of(name), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            err.println("Cannot read " + name + ": " + e.getMessage());
            return null;
        }
    }

    private void interactive() {
        out.println("AI Job Search and Career Agent. Type 'help' for commands, 'exit' to quit.");
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);
        while (true) {
            out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                break;
            }
            String result = registry.dispatch(line);
            if (!result.isEmpty()) {
                out.println(result);
            }
        }
    }
}