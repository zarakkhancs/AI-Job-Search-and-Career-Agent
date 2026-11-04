package com.careeragent.documents;

import java.util.List;

/** A tailored summary and cover letter, plus which job skills were backed by the resume. */
public record TailoredDocument(String summary, String coverLetter,
                               List<String> supportedSkills, List<String> flaggedSkills) {
}