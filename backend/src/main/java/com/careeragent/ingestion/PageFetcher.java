package com.careeragent.ingestion;

/** Downloads a web page as raw text (HTML). Replaceable in tests. */
public interface PageFetcher {
    String fetch(String url);
}