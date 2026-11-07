"use client";

import Link from "next/link";
import { useState } from "react";
import { api, type Tailored } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-40 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

function Chips({ items, tone }: { items: string[]; tone: "green" | "red" }) {
  const colors = tone === "green" ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800";
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

function CopyButton({ text }: { text: string }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      className="text-sm text-indigo-600 hover:underline"
      onClick={async () => {
        try {
          await navigator.clipboard.writeText(text);
          setCopied(true);
          setTimeout(() => setCopied(false), 1500);
        } catch {
          setCopied(false);
        }
      }}
    >
      {copied ? "Copied" : "Copy"}
    </button>
  );
}

export default function TailorPage() {
  const [resume, setResume] = useState("");
  const [job, setJob] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<Tailored | null>(null);
  const [label, setLabel] = useState("");
  const [exporting, setExporting] = useState(false);

  async function onExport() {
    if (!result) return;
    setError("");
    setExporting(true);
    try {
      const file = await api.exportPackage({
        label: label.trim() || undefined,
        resume,
        summary: result.summary,
        coverLetter: result.coverLetter,
        flaggedSkills: result.flaggedSkills,
      });
      const url = URL.createObjectURL(new Blob([file.content], { type: "text/plain;charset=utf-8" }));
      const link = document.createElement("a");
      link.href = url;
      link.download = file.filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setExporting(false);
    }
  }

  async function onGenerate() {
    setError("");
    setBusy(true);
    setResult(null);
    try {
      setResult(await api.tailorDocuments(resume, job));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Tailored documents</h1>
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
          <button className={btn} disabled={busy || !resume.trim() || !job.trim()} onClick={onGenerate}>
            {busy ? "Generating..." : "Generate summary and cover letter"}
          </button>
        </div>
      </section>

      {result && (
        <>
          {result.flaggedSkills.length > 0 && (
            <div className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-sm text-amber-900">
              <p className="mb-2 font-semibold">
                Factual check: these skills appear in the generated text but not in your resume. Remove
                them or add real experience before sending.
              </p>
              <Chips items={result.flaggedSkills} tone="red" />
            </div>
          )}

          <section className={card}>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">Tailored summary</h2>
              <CopyButton text={result.summary} />
            </div>
            <p className="whitespace-pre-wrap text-sm text-gray-800">{result.summary}</p>
          </section>

          <section className={card}>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-gray-900">Cover letter</h2>
              <CopyButton text={result.coverLetter} />
            </div>
            <p className="whitespace-pre-wrap text-sm text-gray-800">{result.coverLetter}</p>
          </section>

          <section className={card}>
            <h2 className="mb-2 text-sm font-semibold text-gray-900">Skills supported by your resume</h2>
            <Chips items={result.supportedSkills} tone="green" />
          </section>

          <section className={card}>
            <h2 className="mb-2 text-sm font-semibold text-gray-900">Export application package</h2>
            <p className="mb-3 text-sm text-gray-600">
              Download the summary, cover letter and resume as one text file. Any flagged skills are
              listed at the top for review before you send it.
            </p>
            <div className="flex flex-wrap items-center gap-3">
              <input
                className="w-64 rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
                placeholder="Label, e.g. Acme Backend Dev"
                value={label}
                onChange={(e) => setLabel(e.target.value)}
              />
              <button className={btn} disabled={exporting} onClick={onExport}>
                {exporting ? "Exporting..." : "Download package"}
              </button>
            </div>
          </section>
        </>
      )}
    </main>
  );
}
