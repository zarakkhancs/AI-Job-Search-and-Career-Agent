package com.careeragent.prompts;

/** Concrete product: asks the model for technical and behavioral interview questions. */
public record PrepSheetPrompt(String job, String resume) implements Prompt {
    @Override
    public String name() {
        return "prep-sheet";
    }

    @Override
    public String text() {
        StringBuilder sb = new StringBuilder();
        sb.append("Write interview preparation questions for the job below:\n");
        sb.append("exactly 5 technical questions and exactly 3 behavioral questions.\n");
        if (!resume.isEmpty()) {
            sb.append("Tailor them to the candidate's resume where relevant.\n");
        }
        sb.append("Respond with a JSON object only: ");
        sb.append("{\"technical\":[\"...\"],\"behavioral\":[\"...\"]}\n");
        sb.append("Job posting:\n").append(job);
        if (!resume.isEmpty()) {
            sb.append("\nResume:\n").append(resume);
        }
        return sb.toString();
    }
}