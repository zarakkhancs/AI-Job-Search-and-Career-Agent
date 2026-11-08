"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, type Extraction, type JobPosting } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-32 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

function Chips({ items, tone }: { items: string[]; tone: "gray" | "red" }) {
  const colors = tone === "gray" ? "bg-gray-100 text-gray-700" : "bg-red-100 text-red-800";
  if (items.length === 0) return <p className="text-sm text-gray-400">None</p>;
  return (
    <div className="flex flex-wrap gap-2">
      {items.map((s) => (
        <span key={s} className={`rounded-full px-3 py-1 text-xs font-medium ${colors}`}>
          {s}
        </span>
      ))}
    </div>
  );
}

export default function JobsPage() {
  const [input, setInput] = useState("");
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [skills, setSkills] = useState<Record<number, Extraction>>({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api.getJobs().then(setJobs).catch(() => {});
  }, []);

  async function onImport() {
    setError("");
    setBusy(true);
    try {
      const posting = await api.addJob(input);
      setJobs((j) => [...j, posting]);
      setInput("");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  async function onSkills(job: JobPosting) {
    setError("");
    setBusy(true);
    try {
      const result = await api.extractSkills(job.text);
      setSkills((s) => ({ ...s, [job.id]: result }));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Job postings</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <section className={`${card} space-y-3`}>
        <h2 className="text-sm font-semibold text-gray-900">Import a job posting</h2>
        <p className="text-sm text-gray-600">
          Paste a link to the posting, or paste the job description text. Some sites block automatic
          downloads; if a link does not work, paste the text instead.
        </p>
        <textarea
          className={area}
          placeholder="https://... or the job description text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
        />
        <button className={btn} disabled={busy || !input.trim()} onClick={onImport}>
          {busy ? "Working..." : "Import job posting"}
        </button>
      </section>

      {jobs.length === 0 && <p className="text-sm text-gray-500">No job postings imported yet.</p>}

      {jobs
        .slice()
        .reverse()
        .map((job) => (
          <section key={job.id} className={`${card} space-y-3`}>
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-gray-900">
                #{job.id} {job.source}
              </h2>
              <span className="text-xs text-gray-500">{job.text.length} characters</span>
            </div>
            <p className="max-h-40 overflow-auto whitespace-pre-wrap text-sm text-gray-800">{job.text}</p>
            <button
              className="text-sm text-indigo-600 hover:underline disabled:opacity-50"
              disabled={busy}
              onClick={() => onSkills(job)}
            >
              Show required skills
            </button>
            {skills[job.id] && (
              <div className="space-y-2">
                <Chips items={skills[job.id].verified} tone="gray" />
                {skills[job.id].rejected.length > 0 && (
                  <>
                    <p className="text-xs text-gray-500">Rejected (not found in the text)</p>
                    <Chips items={skills[job.id].rejected} tone="red" />
                  </>
                )}
              </div>
            )}
          </section>
        ))}
    </main>
  );
}