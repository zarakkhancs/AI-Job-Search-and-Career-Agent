package com.careeragent.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class MockInterviewServiceTest {
    private static final String RESUME = "Wrote Java services at a campus startup.";
    private static final String JOB = "Needs Java and Docker.";
    private static final String QUESTIONS = "{\"questions\":["
            + "{\"type\":\"behavioral\",\"text\":\"Tell me about a team project.\"},"
            + "{\"type\":\"technical\",\"text\":\"What is a REST API?\"},"
            + "{\"type\":\"technical\",\"text\":\"Explain Docker.\"}]}";

    private static MockInterviewService service(String feedbackJson) {
        LLMClient llm = prompt -> {
            if (prompt.startsWith("Extract")) {
                return "[\"Java\",\"Docker\"]";
            }
            return prompt.startsWith("Write") ? QUESTIONS : feedbackJson;
        };
        return new MockInterviewService(llm, new SkillExtractor(llm));
    }

    @Test
    void startCapsQuestionsAtCountAndNumbersThem() throws Exception {
        InterviewSession s = service("{}").start(RESUME, JOB, 2);
        assertEquals(2, s.getQuestions().size());
        assertEquals(1, s.getQuestions().get(0).id());
        assertEquals("technical", s.getQuestions().get(1).type());
        assertEquals(InterviewSession.Status.IN_PROGRESS, s.getStatus());
    }

    @Test
    void answerFlagsASkillTheResumeDoesNotContain() throws Exception {
        MockInterviewService svc = service("{\"score\":7,\"feedback\":\"Good structure.\"}");
        InterviewSession s = svc.start(RESUME, JOB, 2);
        AnswerFeedback fb = svc.answer(s.getId(), 1, "I used Java and Docker every day.");
        assertEquals(7, fb.score());
        assertEquals(List.of("Docker"), fb.flaggedClaims());
    }

    @Test
    void answerWithinTheResumeFlagsNothing() throws Exception {
        MockInterviewService svc = service("{\"score\":8,\"feedback\":\"Nice.\"}");
        InterviewSession s = svc.start(RESUME, JOB, 2);
        AnswerFeedback fb = svc.answer(s.getId(), 1, "I wrote Java services.");
        assertTrue(fb.flaggedClaims().isEmpty());
    }

    @Test
    void scoreIsClampedToTen() throws Exception {
        MockInterviewService svc = service("{\"score\":15,\"feedback\":\"Great.\"}");
        InterviewSession s = svc.start(RESUME, JOB, 1);
        assertEquals(10, svc.answer(s.getId(), 1, "Answer.").score());
    }

    @Test
    void finishSummarisesAnswersAndFlags() throws Exception {
        MockInterviewService svc = service("{\"score\":8,\"feedback\":\"Nice.\"}");
        InterviewSession s = svc.start(RESUME, JOB, 2);
        svc.answer(s.getId(), 1, "I used Docker.");
        InterviewSummary sum = svc.finish(s.getId());
        assertEquals(1, sum.answered());
        assertEquals(2, sum.total());
        assertEquals(8.0, sum.averageScore());
        assertEquals(List.of("Docker"), sum.flaggedClaims());
    }

    @Test
    void cannotAnswerAfterFinish() throws Exception {
        MockInterviewService svc = service("{\"score\":5,\"feedback\":\"Ok.\"}");
        InterviewSession s = svc.start(RESUME, JOB, 1);
        svc.finish(s.getId());
        assertThrows(IllegalStateException.class, () -> svc.answer(s.getId(), 1, "Late answer."));
    }

    @Test
    void unknownSessionAndQuestionAreNotFound() throws Exception {
        MockInterviewService svc = service("{\"score\":5,\"feedback\":\"Ok.\"}");
        assertThrows(InterviewNotFoundException.class, () -> svc.get("nope"));
        InterviewSession s = svc.start(RESUME, JOB, 1);
        assertThrows(InterviewNotFoundException.class, () -> svc.answer(s.getId(), 99, "Answer."));
    }

    @Test
    void rejectsBadInputAndBadModelOutput() throws Exception {
        MockInterviewService svc = service("not json");
        assertThrows(IllegalArgumentException.class, () -> svc.start(" ", JOB, 2));
        assertThrows(IllegalArgumentException.class, () -> svc.start(RESUME, JOB, 0));
        InterviewSession s = svc.start(RESUME, JOB, 1);
        assertThrows(IllegalArgumentException.class, () -> svc.answer(s.getId(), 1, " "));
        assertThrows(InterviewException.class, () -> svc.answer(s.getId(), 1, "Answer."));
    }
}