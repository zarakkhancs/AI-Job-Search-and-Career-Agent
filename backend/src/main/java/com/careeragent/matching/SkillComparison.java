package com.careeragent.matching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Deterministic matched/missing computation shared by all strategies. */
final class SkillComparison {
    record Result(List<String> matched, List<String> missing) {
    }

    private SkillComparison() {
    }

    static Result compare(List<String> resume, List<String> job) {
        Set<String> have = new HashSet<>();
        if (resume != null) {
            for (String s : resume) {
                if (s != null && !s.isBlank()) {
                    have.add(s.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        Map<String, String> required = new LinkedHashMap<>();
        if (job != null) {
            for (String s : job) {
                if (s != null && !s.isBlank()) {
                    required.putIfAbsent(s.trim().toLowerCase(Locale.ROOT), s.trim());
                }
            }
        }
        if (required.isEmpty()) {
            throw new IllegalArgumentException("Job skills must not be empty.");
        }
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        required.forEach((key, original) -> (have.contains(key) ? matched : missing).add(original));
        return new Result(List.copyOf(matched), List.copyOf(missing));
    }
}
