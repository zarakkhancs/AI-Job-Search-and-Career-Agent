package com.careeragent.extraction;

import java.util.regex.Pattern;

/** Deterministic guard: a claim is supported only if it literally appears in the source text. */
public final class SourceVerifier {
    private SourceVerifier() {
    }

    public static boolean isSupported(String claim, String source) {
        if (claim == null || claim.isBlank() || source == null) {
            return false;
        }
        Pattern p = Pattern.compile(
                "(?<![\\p{L}\\p{N}+#])" + Pattern.quote(claim.trim()) + "(?![\\p{L}\\p{N}+#])",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return p.matcher(source).find();
    }
}
