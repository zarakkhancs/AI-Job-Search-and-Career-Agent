package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.interview.MockInterviewService;
import com.careeragent.llm.LLMClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InterviewCommandTest {
    private static final String QUESTIONS =
            "{\"questions\":[{\"type\":\"technical\",\"text\":\"Explain REST.\"},"
                    + "{\"type\":\"behavioral\",\"text\":\"Describe a team conflict.\"}]}";
    private static final String FEEDBACK = "{\"score\":7,\"feedback\":\"Clear answer.\"}";

    private static InterviewCommand command() {
        LLMClient llm = prompt -> {
            if (prompt.contains("mock interview questions")) {
                return QUESTIONS;
            }
            if (prompt.contains("giving feedback")) {
                return FEEDBACK;
            }
            return "[\"Java\"]";
        };
        return new InterviewCommand(new MockInterviewService(llm, new SkillExtractor(llm)));
    }

    private static List<String> files(Path temp) throws IOException {
        Path resume = temp.resolve("resume.txt");
        Path job = temp.resolve("job.txt");
        Files.writeString(resume, "Python developer with three years of experience.");
        Files.writeString(job, "Java developer needed.");
        return List.of(resume.toString(), job.toString());
    }

    @Test
    void fullInterviewFlowStartAnswerFinish(@TempDir Path temp) throws IOException {
        InterviewCommand cmd = command();
        List<String> f = files(temp);

        String started = cmd.execute(List.of("start", f.get(0), f.get(1), "2"));
        assertTrue(started.contains("Mock interview started (2 questions)."));
        assertTrue(started.contains("1. [technical] Explain REST."));
        assertTrue(started.contains("2. [behavioral] Describe a team conflict."));

        String answered = cmd.execute(List.of("answer", "1", "I", "used", "Java", "at", "Acme"));
        assertTrue(answered.contains("Score: 7/10"));
        assertTrue(answered.contains("Clear answer."));
        assertTrue(answered.contains("Warning: claims not backed by your resume: Java"));

        String finished = cmd.execute(List.of("finish"));
        assertTrue(finished.contains("Answered 1 of 2 questions."));
        assertTrue(finished.contains("Average score: 7.0/10"));
        assertTrue(finished.contains("Java"));
    }

    @Test
    void showsUsageWithoutArguments() {
        assertEquals(InterviewCommand.USAGE, command().execute(List.of()));
        assertEquals(InterviewCommand.USAGE, command().execute(List.of("bogus")));
    }

    @Test
    void answerAndFinishNeedAStartedInterview() {
        InterviewCommand cmd = command();

        assertEquals(InterviewCommand.NO_INTERVIEW, cmd.execute(List.of("answer", "1", "text")));
        assertEquals(InterviewCommand.NO_INTERVIEW, cmd.execute(List.of("finish")));
    }

    @Test
    void rejectsABadQuestionCountAndUnreadableFiles(@TempDir Path temp) throws IOException {
        InterviewCommand cmd = command();
        List<String> f = files(temp);

        assertEquals("Question count must be a whole number.",
                cmd.execute(List.of("start", f.get(0), f.get(1), "lots")));
        assertTrue(cmd.execute(List.of("start", "no-resume.txt", "no-job.txt")).startsWith("Could not read file"));
    }
}