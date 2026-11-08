"use client";

import Link from "next/link";
import { useState } from "react";
import { api, type ParsedResume } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-32 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

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

export default function ResumePage() {
  const [file, setFile] = useState<File | null>(null);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<ParsedResume | null>(null);

  async function run(action: () => Promise<ParsedResume>) {
    setError("");
    setBusy(true);
    try {
      setResult(await action());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Import resume</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <section className={`${card} space-y-3`}>
        <h2 className="text-sm font-semibold text-gray-900">Upload a PDF</h2>
        <input
          type="file"
          accept=".pdf,application/pdf"
          className="block text-sm text-gray-900"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        <button className={btn} disabled={busy || !file} onClick={() => file && run(() => api.uploadResume(file))}>
          {busy ? "Reading..." : "Import PDF"}
        </button>
      </section>

      <section className={`${card} space-y-3`}>
        <h2 className="text-sm font-semibold text-gray-900">Or paste the resume text</h2>
        <textarea className={area} placeholder="Paste your resume text..." value={text} onChange={(e) => setText(e.target.value)} />
        <button
          className={btn}
          disabled={busy || !text.trim()}
          onClick={() => run(() => api.importResumeText("pasted resume", text))}
        >
          {busy ? "Reading..." : "Import pasted text"}
        </button>
      </section>

      {result && (
        <section className={`${card} space-y-3`}>
          <h2 className="text-lg font-semibold text-gray-900">
            #{result.id} {result.filename}
          </h2>
          <div>
            <h3 className="mb-2 text-sm font-semibold text-gray-700">Skills found (verified against the text)</h3>
            <Chips items={result.skills} tone="green" />
          </div>
          {result.rejectedSkills.length > 0 && (
            <div>
              <h3 className="mb-2 text-sm font-semibold text-gray-700">Rejected (not found in the text)</h3>
              <Chips items={result.rejectedSkills} tone="red" />
            </div>
          )}
          <div>
            <h3 className="mb-2 text-sm font-semibold text-gray-700">Extracted text</h3>
            <p className="max-h-64 overflow-auto whitespace-pre-wrap text-sm text-gray-800">{result.text}</p>
          </div>
        </section>
      )}
    </main>
  );
}