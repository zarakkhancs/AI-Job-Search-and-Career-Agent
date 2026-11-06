package com.careeragent.prompts;

/** Concrete product: asks the model for three truthful rewrites of one resume bullet. */
public record BulletOptimizerPrompt(String bullet, String job) implements Prompt {
    @Override
    public String name() {
        return "bullet-optimizer";
    }

    @Override
    public String text() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rewrite the resume bullet point below as THREE improved versions.\n");
        sb.append("Start each with a strong action verb and keep it concise and specific.\n");
        sb.append("Use ONLY facts that appear in the original bullet. Do not invent numbers,\n");
        sb.append("percentages, tools, employers or results.\n");
        if (!job.isEmpty()) {
            sb.append("Where it is truthful, use wording that fits the job posting below.\n");
        }
        sb.append("Respond with a JSON object only: {\"versions\":[\"...\",\"...\",\"...\"]}\n");
        sb.append("Bullet:\n").append(bullet);
        if (!job.isEmpty()) {
            sb.append("\nJob posting:\n").append(job);
        }
        return sb.toString();
    }
}