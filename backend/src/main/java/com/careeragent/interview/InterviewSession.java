package com.careeragent.interview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory state of one mock interview. */
public class InterviewSession {
    public enum Status { IN_PROGRESS, COMPLETED }

    private final String id;
    private final String resume;
    private final String job;
    private final List<InterviewQuestion> questions;
    private final Map<Integer, AnswerFeedback> feedback = new LinkedHashMap<>();
    private Status status = Status.IN_PROGRESS;

    public InterviewSession(String id, String resume, String job, List<InterviewQuestion> questions) {
        this.id = id;
        this.resume = resume;
        this.job = job;
        this.questions = List.copyOf(questions);
    }

    public String getId() { return id; }
    public String getResume() { return resume; }
    public String getJob() { return job; }
    public List<InterviewQuestion> getQuestions() { return questions; }
    public synchronized Status getStatus() { return status; }
    public synchronized void complete() { status = Status.COMPLETED; }

    public synchronized void putFeedback(AnswerFeedback f) {
        feedback.put(f.questionId(), f);
    }

    public synchronized List<AnswerFeedback> getFeedback() {
        return new ArrayList<>(feedback.values());
    }

    public InterviewQuestion findQuestion(int questionId) {
        return questions.stream().filter(q -> q.id() == questionId).findFirst()
                .orElseThrow(() -> new InterviewNotFoundException("No question with id " + questionId + "."));
    }
}