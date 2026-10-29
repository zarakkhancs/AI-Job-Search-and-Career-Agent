package com.careeragent.extraction;

import java.util.List;

/** Skills the LLM claimed, split into those found in the source text and those that were not. */
public record ExtractionResult(List<String> verified, List<String> rejected) {
}
