"use client";

import Link from "next/link";
import { useState } from "react";
import { api, type BulletResult } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const input = "w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

export default function BulletsPage() {
  const [bullet, setBullet] = useState("");
  const [job, setJob] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<BulletResult | null>(null);

  async function onOptimize() {
    setError("");
    setLoading(true);
    setResult(null);
    try {
      setResult(await api.optimizeBullet(bullet, job));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Optimization failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Resume bullet optimizer</h1>
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
        <label className="mb-1 block text-sm font-medium text-gray-900">Resume bullet</label>
        <textarea
          className={`${input} h-24`}
          placeholder="e.g. Built a REST API for the team"
          value={bullet}
          onChange={(e) => setBullet(e.target.value)}
        />
        <label className="mb-1 mt-3 block text-sm font-medium text-gray-900">
          Job posting (optional, to tailor the wording)
        </label>
        <textarea
          className={`${input} h-32`}
          value={job}
          onChange={(e) => setJob(e.target.value)}
        />
        <div className="mt-3">
          <button className={btn} disabled={loading || !bullet.trim()} onClick={onOptimize}>
            {loading ? "Optimizing..." : "Optimize bullet"}
          </button>
        </div>
      </section>

      {result && (
        <section className={card}>
          <h2 className="mb-3 text-lg font-semibold text-gray-900">Improved versions</h2>
          <p className="mb-3 text-sm text-gray-500">Original: {result.original}</p>
          <ol className="space-y-3">
            {result.versions.map((v, i) => (
              <li key={i} className="rounded-lg bg-gray-50 p-3 text-sm text-gray-900">
                {v.text}
                {v.unsupportedFigures.length > 0 && (
                  <div className="mt-2 rounded border border-amber-200 bg-amber-50 p-2 text-xs text-amber-800">
                    Figures not in your original bullet (verify before using):{" "}
                    {v.unsupportedFigures.join(", ")}
                  </div>
                )}
              </li>
            ))}
          </ol>
        </section>
      )}
    </main>
  );
}