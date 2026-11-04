package com.careeragent.documents;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.extraction.SourceVerifier;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a tailored summary and cover letter with the LLM, then runs a deterministic
 * factual check: any job skill that appears in the output but not in the resume is flagged.
 */
public class TailoredDocumentService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LLMClient llm;
    private final SkillExtractor extractor;

    public TailoredDocumentService(LLMClient llm, SkillExtractor extractor) {
        this.llm = llm;
        this.extractor = extractor;
    }

    public TailoredDocument tailor(String resume, String job) throws LLMException {
        if (resume == null || resume.isBlank()) {
            throw new IllegalArgumentException("Resume text must not be blank.");
        }
        if (job == null || job.isBlank()) {
            throw new IllegalArgumentException("Job posting text must not be blank.");
        }
        List<String> jobSkills = extractor.extract(job).verified();
        JsonNode doc = parse(llm.generate(buildPrompt(resume, job)));
        String summary = field(doc, "summary");
        String coverLetter = field(doc, "coverLetter");
        String generated = summary + "\n" + coverLetter;

        List<String> supported = new ArrayList<>();
        List<String> flagged = new ArrayList<>();
        for (String skill : jobSkills) {
            if (!SourceVerifier.isSupported(skill, generated)) {
                continue;
            }
            if (SourceVerifier.isSupported(skill, resume)) {
                supported.add(skill);
            } else {
                flagged.add(skill);
            }
        }
        return new TailoredDocument(summary, coverLetter, List.copyOf(supported), List.copyOf(flagged));
    }

    String buildPrompt(String resume, String job) {
        return String.join("\n",
                "Write a tailored resume summary (3 sentences) and a short cover letter for the job below.",
                "Use ONLY facts that appear in the resume. Do not claim any skill, employer,",
                "degree or experience that the resume does not contain.",
                "Respond with a JSON object only, with string keys \"summary\" and \"coverLetter\".",
                "Resume:",
                resume,
                "Job posting:",
                job);
    }

    private static JsonNode parse(String raw) {
        String text = raw == null ? "" : raw;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new DocumentException("Document response contained no JSON object.");
        }
        try {
            return MAPPER.readTree(text.substring(start, end + 1));
        } catch (JsonProcessingException e) {
            throw new DocumentException("Document response was not valid JSON.", e);
        }
    }

    private static String field(JsonNode node, String name) {
        String value = node.path(name).asText("").trim();
        if (value.isEmpty()) {
            throw new DocumentException("Document response was missing '" + name + "'.");
        }
        return value;
    }
}