"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { api, type Analytics, type Application } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const btnSmall =
  "rounded border border-gray-300 px-2 py-1 text-xs text-gray-700 hover:bg-gray-100";
const input = "w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

const STAGES = ["SAVED", "APPLIED", "INTERVIEW", "OFFER", "REJECTED"];
const ACTIONS = ["apply", "interview", "offer", "reject"];

export default function PipelinePage() {
  const [apps, setApps] = useState<Application[]>([]);
  const [stats, setStats] = useState<Analytics | null>(null);
  const [company, setCompany] = useState("");
  const [title, setTitle] = useState("");
  const [error, setError] = useState("");

  const refresh = useCallback(async () => {
    try {
      const [a, s] = await Promise.all([api.getApplications(), api.getAnalytics()]);
      setApps(a);
      setStats(s);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not reach the backend.");
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function run(action: () => Promise<unknown>) {
    setError("");
    try {
      await action();
      await refresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Application pipeline</h1>
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
        <h2 className="mb-3 text-lg font-semibold text-gray-900">Analytics</h2>
        {stats === null || stats.total === 0 ? (
          <p className="text-sm text-gray-500">No applications yet. Add one below.</p>
        ) : (
          <>
            <p className="mb-3 text-sm text-gray-600">
              {stats.total} applications, {stats.submitted} submitted
            </p>
            <div className="mb-3 grid grid-cols-5 gap-2 text-center">
              {STAGES.map((s) => (
                <div key={s} className="rounded-lg bg-gray-50 p-2">
                  <div className="text-xl font-semibold text-gray-900">{stats.byStage[s] ?? 0}</div>
                  <div className="text-xs text-gray-500">{s}</div>
                </div>
              ))}
            </div>
            <p className="text-sm text-gray-700">
              Interview rate {stats.interviewRate}% | Offer rate {stats.offerRate}% | Rejection rate{" "}
              {stats.rejectionRate}%
            </p>
          </>
        )}
      </section>

      <section className={card}>
        <h2 className="mb-3 text-lg font-semibold text-gray-900">Add application</h2>
        <form
          className="flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            run(async () => {
              await api.addApplication(company, title);
              setCompany("");
              setTitle("");
            });
          }}
        >
          <input
            className={input}
            placeholder="Company"
            value={company}
            onChange={(e) => setCompany(e.target.value)}
          />
          <input
            className={input}
            placeholder="Job title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <button className={btn} type="submit">
            Add
          </button>
        </form>
      </section>

      <section className={card}>
        <h2 className="mb-3 text-lg font-semibold text-gray-900">Applications</h2>
        {apps.length === 0 ? (
          <p className="text-sm text-gray-500">Nothing tracked yet.</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {apps.map((a) => (
              <li key={a.id} className="flex items-center justify-between gap-3 py-2 text-sm">
                <span className="text-gray-900">
                  #{a.id} {a.company} - {a.title}{" "}
                  <span className="ml-2 rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-600">
                    {a.status}
                  </span>
                </span>
                <span className="flex gap-1">
                  {ACTIONS.map((act) => (
                    <button
                      key={act}
                      className={btnSmall}
                      onClick={() => run(() => api.moveApplication(a.id, act))}
                    >
                      {act}
                    </button>
                  ))}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}