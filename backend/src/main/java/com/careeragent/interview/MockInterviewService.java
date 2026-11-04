package com.careeragent.interview;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.extraction.SourceVerifier;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs mock interviews: the LLM writes questions and critiques answers, and a deterministic
 * factual check flags any job skill the candidate claims in an answer that is not on the resume.
 */
public class MockInterviewService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LLMClient llm;
    private final SkillExtractor extractor;
    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    public MockInterviewService(LLMClient llm, SkillExtractor extractor) {
        this.llm = llm;
        this.extractor = extractor;
    }

    public InterviewSession start(String resume, String job, int count) throws LLMException {
        requireText(resume, "Resume");
        requireText(job, "Job posting");
        if (count < 1 || count > 10) {
            throw new IllegalArgumentException("Question count must be between 1 and 10.");
        }
        JsonNode root = parse(llm.generate(questionsPrompt(resume, job, count)));
        JsonNode arr = root.path("questions");
        if (!arr.isArray() || arr.isEmpty()) {
            throw new InterviewException("Interview response had no questions.");
        }
        List<InterviewQuestion> questions = new ArrayList<>();
        int id = 1;
        for (JsonNode q : arr) {
            String text = q.path("text").asText("").trim();
            if (text.isEmpty()) {
                continue;
            }
            String type = q.path("type").asText("behavioral").trim().toLowerCase();
            questions.add(new InterviewQuestion(id++, type, text));
            if (questions.size() == count) {
                break;
            }
        }
        if (questions.isEmpty()) {
            throw new InterviewException("Interview response had no usable questions.");
        }
        InterviewSession session = new InterviewSession(
                UUID.randomUUID().toString(), resume, job, questions);
        sessions.put(session.getId(), session);
        return session;
    }

    public AnswerFeedback answer(String sessionId, int questionId, String answer) throws LLMException {
        requireText(answer, "Answer");
        InterviewSession session = get(sessionId);
        if (session.getStatus() == InterviewSession.Status.COMPLETED) {
            throw new IllegalStateException("Interview is already finished.");
        }
        InterviewQuestion question = session.findQuestion(questionId);

        JsonNode root = parse(llm.generate(feedbackPrompt(session, question, answer)));
        int rawScore = root.path("score").asInt(-1);
        if (rawScore < 0) {
            throw new InterviewException("Feedback response was missing 'score'.");
        }
        int score = Math.min(10, rawScore);
        String text = root.path("feedback").asText("").trim();
        if (text.isEmpty()) {
            throw new InterviewException("Feedback response was missing 'feedback'.");
        }

        List<String> flagged = new ArrayList<>();
        for (String skill : extractor.extract(session.getJob()).verified()) {
            if (SourceVerifier.isSupported(skill, answer)
                    && !SourceVerifier.isSupported(skill, session.getResume())) {
                flagged.add(skill);
            }
        }
        AnswerFeedback fb = new AnswerFeedback(questionId, answer, score, text, List.copyOf(flagged));
        session.putFeedback(fb);
        return fb;
    }

    public InterviewSummary finish(String sessionId) {
        InterviewSession session = get(sessionId);
        session.complete();
        List<AnswerFeedback> all = session.getFeedback();
        double avg = all.stream().mapToInt(AnswerFeedback::score).average().orElse(0.0);
        List<String> flagged = all.stream().flatMap(f -> f.flaggedClaims().stream()).distinct().toList();
        return new InterviewSummary(all.size(), session.getQuestions().size(), avg, flagged);
    }

    public InterviewSession get(String sessionId) {
        InterviewSession s = sessionId == null ? null : sessions.get(sessionId);
        if (s == null) {
            throw new InterviewNotFoundException("No interview session with id " + sessionId + ".");
        }
        return s;
    }

    String questionsPrompt(String resume, String job, int count) {
        return String.join("\n",
                "Write " + count + " mock interview questions for the job below, mixing behavioral",
                "and technical questions. Base them on the job posting and the candidate's resume.",
                "Respond with a JSON object only: {\"questions\":[{\"type\":\"behavioral|technical\",\"text\":\"...\"}]}",
                "Resume:", resume, "Job posting:", job);
    }

    String feedbackPrompt(InterviewSession session, InterviewQuestion q, String answer) {
        return String.join("\n",
                "You are an interviewer giving feedback on a candidate's answer.",
                "Score it from 0 to 10 and give 2-4 sentences of constructive feedback.",
                "Only refer to facts in the resume or in the answer. Do not invent experience,",
                "and point out any claim in the answer that the resume does not support.",
                "Respond with a JSON object only: {\"score\":7,\"feedback\":\"...\"}",
                "Resume:", session.getResume(),
                "Job posting:", session.getJob(),
                "Question (" + q.type() + "): " + q.text(),
                "Answer:", answer);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
    }

    private static JsonNode parse(String raw) {
        String text = raw == null ? "" : raw;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new InterviewException("Interview response contained no JSON object.");
        }
        try {
            return MAPPER.readTree(text.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new InterviewException("Interview response was not valid JSON.", e);
        }
    }
}