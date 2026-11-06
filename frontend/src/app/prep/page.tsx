"use client";

import Link from "next/link";
import { useState } from "react";
import { api, type PrepSheet } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const input = "w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

export default function PrepPage() {
  const [job, setJob] = useState("");
  const [resume, setResume] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sheet, setSheet] = useState<PrepSheet | null>(null);

  async function onGenerate() {
    setError("");
    setLoading(true);
    setSheet(null);
    try {
      setSheet(await api.prepSheet(job, resume));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Generation failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Interview prep sheet</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Home
        </Link>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <section className={card}>
        <label className="mb-1 block text-sm font-medium text-gray-900">Job posting</label>
        <textarea
          className={`${input} h-40`}
          placeholder="Paste a job description here..."
          value={job}
          onChange={(e) => setJob(e.target.value)}
        />
        <label className="mb-1 mt-3 block text-sm font-medium text-gray-900">
          Resume (optional, to tailor the questions)
        </label>
        <textarea
          className={`${input} h-32`}
          value={resume}
          onChange={(e) => setResume(e.target.value)}
        />
        <div className="mt-3">
          <button className={btn} disabled={loading || !job.trim()} onClick={onGenerate}>
            {loading ? "Generating..." : "Generate prep sheet"}
          </button>
        </div>
      </section>

      {sheet && (
        <section className={card}>
          <h2 className="mb-2 text-lg font-semibold text-gray-900">Technical questions</h2>
          <ol className="mb-5 list-decimal space-y-2 pl-5 text-sm text-gray-900">
            {sheet.technical.map((q, i) => (
              <li key={i}>{q}</li>
            ))}
          </ol>
          <h2 className="mb-2 text-lg font-semibold text-gray-900">Behavioral questions</h2>
          <ol className="list-decimal space-y-2 pl-5 text-sm text-gray-900">
            {sheet.behavioral.map((q, i) => (
              <li key={i}>{q}</li>
            ))}
          </ol>
        </section>
      )}
    </main>
  );
}