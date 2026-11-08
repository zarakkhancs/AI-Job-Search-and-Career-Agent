package com.careeragent.ingestion;

import java.util.regex.Pattern;

/** Turns an HTML page into readable plain text (deterministic, no libraries). */
final class HtmlText {
    private static final Pattern SCRIPTS = Pattern.compile("(?is)<(script|style|noscript)[^>]*>.*?</\\1>");
    private static final Pattern COMMENTS = Pattern.compile("(?s)<!--.*?-->");
    private static final Pattern BREAKS =
            Pattern.compile("(?i)<br\\s*/?>|</(p|div|li|ul|ol|h[1-6]|tr|section|article)>");
    private static final Pattern TAGS = Pattern.compile("<[^>]+>");

    private HtmlText() {
    }

    static String toText(String html) {
        String s = SCRIPTS.matcher(html == null ? "" : html).replaceAll(" ");
        s = COMMENTS.matcher(s).replaceAll(" ");
        s = BREAKS.matcher(s).replaceAll("\n");
        s = TAGS.matcher(s).replaceAll(" ");
        return normalize(decode(s));
    }

    /** Collapses runs of spaces, trims lines and keeps at most one blank line in a row. */
    static String normalize(String text) {
        StringBuilder sb = new StringBuilder();
        boolean lastBlank = true;
        for (String line : text.replace("\r", "").split("\n")) {
            String t = line.replaceAll("[ \\t\\u00a0]+", " ").trim();
            if (t.isEmpty()) {
                if (!lastBlank) {
                    sb.append("\n");
                    lastBlank = true;
                }
            } else {
                sb.append(t).append("\n");
                lastBlank = false;
            }
        }
        return sb.toString().trim();
    }

    private static String decode(String s) {
        return s.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replace("&rsquo;", "'")
                .replace("&amp;", "&");
    }
}