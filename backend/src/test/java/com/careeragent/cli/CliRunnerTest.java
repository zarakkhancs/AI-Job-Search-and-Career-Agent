package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliRunnerTest {
    @TempDir
    Path dir;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final List<String> agentRequests = new ArrayList<>();

    private static CommandRegistry registry() {
        CommandRegistry r = new CommandRegistry();
        r.register(new Command() {
            public String name() { return "echo"; }
            public String description() { return "Echo args"; }
            public String execute(List<String> args) { return String.join("|", args); }
        });
        return r;
    }

    private CliRunner runner(String stdin, Function<String, String> agent) {
        return new CliRunner(registry(), agent, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBytes, true, StandardCharsets.UTF_8),
                new PrintStream(errBytes, true, StandardCharsets.UTF_8));
    }

    private CliRunner runner(String stdin) {
        return runner(stdin, request -> {
            agentRequests.add(request);
            return "agent says hi";
        });
    }

    private String out() { return outBytes.toString(StandardCharsets.UTF_8); }

    private String err() { return errBytes.toString(StandardCharsets.UTF_8); }

    private Path write(String name, String text) throws IOException {
        return Files.writeString(dir.resolve(name), text, StandardCharsets.UTF_8);
    }

    @Test
    void helpPrintsUsageWithOptions() {
        assertEquals(0, runner("").run(new String[] {"--help"}));
        assertTrue(out().contains("--input"));
        assertTrue(out().contains("--script"));
    }

    @Test
    void unknownOptionReturnsTwoAndExplains() {
        assertEquals(2, runner("").run(new String[] {"--bogus"}));
        assertTrue(err().contains("Unknown option: --bogus"));
    }

    @Test
    void plainArgumentsRunOneCommand() {
        assertEquals(0, runner("").run(new String[] {"echo", "a", "b"}));
        assertTrue(out().contains("a|b"));
    }

    @Test
    void inputFileIsSentToTheAgentAsOneRequest() throws IOException {
        Path file = write("task.txt", "Match my skills\nto this job\n");
        assertEquals(0, runner("").run(new String[] {"--input", file.toString()}));
        assertEquals(List.of("Match my skills\nto this job"), agentRequests);
        assertTrue(out().contains("agent says hi"));
    }

    @Test
    void inputDashReadsStandardInput() {
        assertEquals(0, runner("hello agent").run(new String[] {"-i", "-"}));
        assertEquals(List.of("hello agent"), agentRequests);
    }

    @Test
    void missingInputFileReturnsTwo() {
        assertEquals(2, runner("").run(new String[] {"--input", dir.resolve("none.txt").toString()}));
        assertTrue(err().contains("Cannot read"));
    }

    @Test
    void inputWithoutFileNameReturnsTwo() {
        assertEquals(2, runner("").run(new String[] {"--input"}));
        assertTrue(err().contains("Missing file name"));
    }

    @Test
    void emptyInputFileReturnsTwoAndDoesNotCallTheAgent() throws IOException {
        Path file = write("empty.txt", "  \n\n");
        assertEquals(2, runner("").run(new String[] {"--input", file.toString()}));
        assertTrue(agentRequests.isEmpty());
    }

    @Test
    void extraArgumentsAfterTheFileReturnTwo() throws IOException {
        Path file = write("task.txt", "hi");
        assertEquals(2, runner("").run(new String[] {"--input", file.toString(), "more"}));
    }

    @Test
    void agentFailureReturnsOneWithMessage() throws IOException {
        Path file = write("task.txt", "hi");
        int code = runner("", request -> { throw new IllegalStateException("model is down"); })
                .run(new String[] {"--input", file.toString()});
        assertEquals(1, code);
        assertTrue(err().contains("model is down"));
    }

    @Test
    void scriptRunsEachCommandAndSkipsBlankLinesAndComments() throws IOException {
        Path file = write("session.txt", "# a comment\n\necho one\n  echo two  \n");
        assertEquals(0, runner("").run(new String[] {"--script", file.toString()}));
        String o = out();
        assertTrue(o.contains("> echo one"));
        assertTrue(o.contains("one"));
        assertTrue(o.contains("> echo two"));
        assertFalse(o.contains("a comment"));
    }

    @Test
    void scriptStopsAtExit() throws IOException {
        Path file = write("session.txt", "echo first\nexit\necho second\n");
        assertEquals(0, runner("").run(new String[] {"-s", file.toString()}));
        assertTrue(out().contains("first"));
        assertFalse(out().contains("second"));
    }

    @Test
    void scriptCanComeFromStandardInput() {
        assertEquals(0, runner("echo piped\n").run(new String[] {"--script", "-"}));
        assertTrue(out().contains("piped"));
    }

    @Test
    void interactiveSessionRunsCommandsUntilExit() {
        assertEquals(0, runner("echo hi\nexit\necho after\n").run(new String[0]));
        assertTrue(out().contains("hi"));
        assertFalse(out().contains("after"));
    }
}