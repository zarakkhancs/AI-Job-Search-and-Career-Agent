package com.careeragent.resume;

import java.util.List;

/** An imported resume: its text plus the skills found in it (verified against the text). */
public record ParsedResume(int id, String filename, String text, List<String> skills,
                           List<String> rejectedSkills) {
}