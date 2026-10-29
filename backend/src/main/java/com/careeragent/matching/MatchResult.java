package com.careeragent.matching;

import java.util.List;

/** Outcome of comparing a candidate's skills with a job's required skills. */
public record MatchResult(int score, List<String> matched, List<String> missing) {
}
