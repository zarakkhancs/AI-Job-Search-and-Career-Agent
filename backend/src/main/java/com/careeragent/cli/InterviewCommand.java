package com.careeragent.cli;

import com.careeragent.interview.AnswerFeedback;
import com.careeragent.interview.InterviewQuestion;
import com.careeragent.interview.InterviewSession;
import com.careeragent.interview.InterviewSummary;
import com.careeragent.interview.MockInterviewService;
import com.careeragent.llm.LLMException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * CLI for F12 (mock interview practice). One interview runs at a time:
 * start it from a resume file and a job file, answer questions one by one, then finish.
 */
public class InterviewCommand implements Command {
    static final int DEFAULT_QUESTIONS = 3;
    static final String USAGE = String.join("\n",
            "Usage:",
            "  interview start <resume-file> <job-file> [count]",
            "  interview answer <question-number> <your answer>",
            "  interview finish");
    static final String NO_INTERVIEW =
            "No interview in progress. Start one with: interview start <resume-file> <job-file> [count]";

    private final MockInterviewService service;
    private String sessionId;

    public InterviewCommand(MockInterviewService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "interview";
    }

    @Override
    public String description() {
        return "Practice a mock interview: interview start|answer|finish (run 'interview' for usage)";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        return switch (args.get(0).toLowerCase(Locale.ROOT)) {
            case "start" -> start(args);
            case "answer" -> answer(args);
            case "finish" -> finish();
            default -> USAGE;
        };
    }

    private String start(List<String> args) {
        if (args.size() < 3) {
            return USAGE;
        }
        int count = DEFAULT_QUESTIONS;
        if (args.size() > 3) {
            try {
                count = Integer.parseInt(args.get(3));
            } catch (NumberFormatException e) {
                return "Question count must be a whole number.";
            }
        }
        String resume;
        String job;
        try {
            resume = Files.readString(Path.of(args.get(1)));
            job = Files.readString(Path.of(args.get(2)));
        } catch (IOException e) {
            return "Could not read file: " + e.getMessage();
        }
        try {
            InterviewSession session = service.start(resume, job, count);
            sessionId = session.getId();
            StringBuilder sb = new StringBuilder("Mock interview started (" + session.getQuestions().size() + " questions).\n");
            for (InterviewQuestion q : session.getQuestions()) {
                sb.append(q.id()).append(". [").append(q.type()).append("] ").append(q.text()).append("\n");
            }
            sb.append("Answer with: interview answer <question-number> <your answer>");
            return sb.toString();
        } catch (LLMException e) {
            return "The AI request failed: " + e.getMessage();
        } catch (RuntimeException e) {
            return "Could not start the interview: " + e.getMessage();
        }
    }

    private String answer(List<String> args) {
        if (sessionId == null) {
            return NO_INTERVIEW;
        }
        if (args.size() < 3) {
            return USAGE;
        }
        int questionId;
        try {
            questionId = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            return "Question number must be a whole number.";
        }
        String text = String.join(" ", args.subList(2, args.size()));
        try {
            AnswerFeedback fb = service.answer(sessionId, questionId, text);
            StringBuilder sb = new StringBuilder();
            sb.append("Score: ").append(fb.score()).append("/10\n");
            sb.append("Feedback: ").append(fb.feedback());
            if (!fb.flaggedClaims().isEmpty()) {
                sb.append("\nWarning: claims not backed by your resume: ")
                        .append(String.join(", ", fb.flaggedClaims()));
            }
            return sb.toString();
        } catch (LLMException e) {
            return "The AI request failed: " + e.getMessage();
        } catch (RuntimeException e) {
            return "Could not score the answer: " + e.getMessage();
        }
    }

    private String finish() {
        if (sessionId == null) {
            return NO_INTERVIEW;
        }
        try {
            InterviewSummary s = service.finish(sessionId);
            sessionId = null;
            StringBuilder sb = new StringBuilder();
            sb.append("Interview finished. Answered ").append(s.answered()).append(" of ")
                    .append(s.total()).append(" questions.\n");
            sb.append(String.format(Locale.ROOT, "Average score: %.1f/10", s.averageScore()));
            if (!s.flaggedClaims().isEmpty()) {
                sb.append("\nClaims not backed by your resume: ").append(String.join(", ", s.flaggedClaims()));
            }
            return sb.toString();
        } catch (RuntimeException e) {
            return "Could not finish the interview: " + e.getMessage();
        }
    }
}