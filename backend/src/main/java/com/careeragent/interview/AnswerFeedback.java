package com.careeragent.interview;

import java.util.List;

/** Feedback on one answer. flaggedClaims are job skills the answer claims but the resume lacks. */
public record AnswerFeedback(int questionId, String answer, int score, String feedback,
                             List<String> flaggedClaims) {
}