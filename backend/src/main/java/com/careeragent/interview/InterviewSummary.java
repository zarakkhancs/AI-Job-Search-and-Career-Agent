package com.careeragent.interview;

import java.util.List;

public record InterviewSummary(int answered, int total, double averageScore,
                               List<String> flaggedClaims) {
}