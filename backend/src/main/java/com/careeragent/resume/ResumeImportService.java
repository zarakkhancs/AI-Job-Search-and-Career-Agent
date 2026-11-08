package com.careeragent.resume;

import com.careeragent.extraction.ExtractionResult;
import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMException;
import java.util.ArrayList;
import java.util.List;

/**
 * F01: imports a resume from a PDF (or pasted text), then extracts its skills with the AI
 * extractor. Every skill is verified against the resume text before it is accepted.
 */
public class ResumeImportService {
    static final int MIN_CHARS = 30;

    private final PdfTextExtractor pdf;
    private final SkillExtractor extractor;
    private final List<ParsedResume> resumes = new ArrayList<>();

    public ResumeImportService(PdfTextExtractor pdf, SkillExtractor extractor) {
        this.pdf = pdf;
        this.extractor = extractor;
    }

    public ParsedResume importPdf(String filename, byte[] bytes) throws LLMException {
        return save(filename, pdf.extract(bytes));
    }

    public ParsedResume importText(String name, String text) throws LLMException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Resume text must not be blank.");
        }
        return save(name, PdfTextExtractor.clean(text));
    }

    public List<ParsedResume> list() {
        synchronized (resumes) {
            return List.copyOf(resumes);
        }
    }

    public ParsedResume get(int id) {
        synchronized (resumes) {
            return resumes.stream().filter(r -> r.id() == id).findFirst()
                    .orElseThrow(() -> new ResumeException("No resume with id " + id + "."));
        }
    }

    private ParsedResume save(String name, String text) throws LLMException {
        if (text.length() < MIN_CHARS) {
            throw new ResumeException("Not enough text was found in the resume to read skills from.");
        }
        ExtractionResult result = extractor.extract(text);
        String label = name == null || name.isBlank() ? "resume" : name.trim();
        synchronized (resumes) {
            ParsedResume parsed = new ParsedResume(resumes.size() + 1, label, text,
                    List.copyOf(result.verified()), List.copyOf(result.rejected()));
            resumes.add(parsed);
            return parsed;
        }
    }
}