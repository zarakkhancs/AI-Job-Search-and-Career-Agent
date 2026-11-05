"use client";

import Link from "next/link";
import { useState } from "react";
import { api, type Extraction, type MatchResult } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-40 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

function Chips({ items, tone }: { items: string[]; tone: "green" | "red" | "gray" }) {
  const colors = {
    green: "bg-green-100 text-green-800",
    red: "bg-red-100 text-red-800",
    gray: "bg-gray-100 text-gray-700",
  }[tone];
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

export default function SkillsPage() {
  const [resume, setResume] = useState("");
  const [job, setJob] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [resumeSkills, setResumeSkills] = useState<Extraction | null>(null);
  const [jobSkills, setJobSkills] = useState<Extraction | null>(null);
  const [match, setMatch] = useState<MatchResult | null>(null);

  async function onRun() {
    setError("");
    setBusy(true);
    setMatch(null);
    try {
      const [r, j] = await Promise.all([api.extractSkills(resume), api.extractSkills(job)]);
      setResumeSkills(r);
      setJobSkills(j);
      setMatch(await api.matchSkills(r.verified, j.verified));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  const barColor =
    match === null ? "" : match.score >= 70 ? "bg-green-500" : match.score >= 40 ? "bg-amber-500" : "bg-red-500";

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Resume vs job match</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <section className={`${card} grid gap-4 md:grid-cols-2`}>
        <div>
          <h2 className="mb-2 text-sm font-semibold text-gray-900">Your resume</h2>
          <textarea className={area} placeholder="Paste your resume text..." value={resume} onChange={(e) => setResume(e.target.value)} />
        </div>
        <div>
          <h2 className="mb-2 text-sm font-semibold text-gray-900">Job posting</h2>
          <textarea className={area} placeholder="Paste the job description..." value={job} onChange={(e) => setJob(e.target.value)} />
        </div>
        <div className="md:col-span-2">
          <button className={btn} disabled={busy || !resume.trim() || !job.trim()} onClick={onRun}>
            {busy ? "Working..." : "Extract skills and match"}
          </button>
        </div>
      </section>

      {match && (
        <section className={card}>
          <div className="mb-2 flex items-baseline justify-between">
            <h2 className="text-lg font-semibold text-gray-900">Match score</h2>
            <span className="text-2xl font-bold text-gray-900">{match.score}%</span>
          </div>
          <div className="mb-5 h-3 w-full rounded-full bg-gray-100">
            <div className={`h-3 rounded-full ${barColor}`} style={{ width: `${match.score}%` }} />
          </div>
          <h3 className="mb-2 text-sm font-semibold text-gray-700">Matched</h3>
          <div className="mb-4">
            <Chips items={match.matched} tone="green" />
          </div>
          <h3 className="mb-2 text-sm font-semibold text-gray-700">Missing</h3>
          <Chips items={match.missing} tone="red" />
        </section>
      )}

      {(resumeSkills || jobSkills) && (
        <section className={`${card} grid gap-6 md:grid-cols-2`}>
          {[
            { title: "Resume skills", data: resumeSkills },
            { title: "Job skills", data: jobSkills },
          ].map(({ title, data }) => (
            <div key={title}>
              <h2 className="mb-2 text-sm font-semibold text-gray-900">{title}</h2>
              <p className="mb-1 text-xs text-gray-500">Verified against the text</p>
              <Chips items={data?.verified ?? []} tone="gray" />
              {data && data.rejected.length > 0 && (
                <>
                  <p className="mb-1 mt-3 text-xs text-gray-500">Rejected (not found in the text)</p>
                  <Chips items={data.rejected} tone="red" />
                </>
              )}
            </div>
          ))}
        </section>
      )}
    </main>
  );
}
