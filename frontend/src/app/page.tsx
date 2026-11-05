"use client";

import { useCallback, useEffect, useState } from "react";
import { api, type Alert, type Deadline } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const input = "w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

export default function Home() {
  const [online, setOnline] = useState<boolean | null>(null);
  const [error, setError] = useState("");
  const [deadlines, setDeadlines] = useState<Deadline[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [label, setLabel] = useState("");
  const [date, setDate] = useState("");
  const [text, setText] = useState("");
  const [extracting, setExtracting] = useState(false);
  const [extracted, setExtracted] = useState<unknown>(null);

  const refresh = useCallback(async () => {
    try {
      const [d, a] = await Promise.all([api.getDeadlines(), api.getAlerts()]);
      setDeadlines(d);
      setAlerts(a);
      setOnline(true);
    } catch (e) {
      setOnline(false);
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

  async function onExtract() {
    setError("");
    setExtracting(true);
    setExtracted(null);
    try {
      setExtracted(await api.extractSkills(text));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Extraction failed.");
    } finally {
      setExtracting(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">AI Job Search and Career Agent</h1>
        <span
          className={`rounded-full px-3 py-1 text-xs font-medium ${
            online === null
              ? "bg-gray-100 text-gray-600"
              : online
                ? "bg-green-100 text-green-700"
                : "bg-red-100 text-red-700"
          }`}
        >
          {online === null ? "Connecting..." : online ? "Backend online" : "Backend offline"}
        </span>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <section className={card}>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Deadlines</h2>
          <button className={btn} onClick={() => run(api.checkDeadlines)}>
            Check upcoming
          </button>
        </div>
        <form
          className="mb-4 flex gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            run(async () => {
              await api.addDeadline(label, date);
              setLabel("");
              setDate("");
            });
          }}
        >
          <input
            className={input}
            placeholder="Label (e.g. Shopify co-op application)"
            value={label}
            onChange={(e) => setLabel(e.target.value)}
          />
          <input
            className={`${input} w-44`}
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <button className={btn} type="submit">
            Add
          </button>
        </form>
        {deadlines.length === 0 ? (
          <p className="text-sm text-gray-500">No deadlines yet.</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {deadlines.map((d, i) => (
              <li key={`${d.label}-${d.date}-${i}`} className="flex justify-between py-2 text-sm">
                <span className="text-gray-900">{d.label}</span>
                <span className="text-gray-500">{d.date}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className={card}>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">Unread alerts</h2>
          <button className={btn} disabled={alerts.length === 0} onClick={() => run(api.markAllRead)}>
            Mark all read
          </button>
        </div>
        {alerts.length === 0 ? (
          <p className="text-sm text-gray-500">No unread alerts.</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {alerts.map((a) => (
              <li key={a.id} className="flex items-center justify-between gap-3 py-2 text-sm">
                <span>
                  <span className="mr-2 rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-600">
                    {a.source}
                  </span>
                  <span className="text-gray-900">{a.message}</span>
                </span>
                <button className={btn} onClick={() => run(() => api.markAlertRead(a.id))}>
                  Read
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className={card}>
        <h2 className="mb-3 text-lg font-semibold text-gray-900">Extract skills from a job posting</h2>
        <textarea
          className={`${input} h-40`}
          placeholder="Paste a job description here..."
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <div className="mt-3">
          <button className={btn} disabled={extracting || !text.trim()} onClick={onExtract}>
            {extracting ? "Extracting..." : "Extract skills"}
          </button>
        </div>
        {extracted !== null && (
          <pre className="mt-4 overflow-x-auto rounded-lg bg-gray-50 p-3 text-xs text-gray-800">
            {JSON.stringify(extracted, null, 2)}
          </pre>
        )}
      </section>
    </main>
  );
}
