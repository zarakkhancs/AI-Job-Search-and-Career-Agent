"use client";

import Link from "next/link";
import { useState } from "react";
import { api } from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-28 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

type Turn = { request: string; answer: string };

const examples = [
  "What skills does this job need? Job: Java backend developer, REST APIs, SQL and Docker.",
  "Give me interview prep for a junior Python developer role that uses Django and PostgreSQL.",
  "Improve this resume bullet for a Java developer job: Worked on backend code for the team.",
];

export default function AgentPage() {
  const [input, setInput] = useState("");
  const [turns, setTurns] = useState<Turn[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function onSend() {
    const text = input.trim();
    if (!text) return;
    setError("");
    setBusy(true);
    try {
      const res = await api.askAgent(text);
      setTurns((t) => [...t, { request: text, answer: res.answer }]);
      setInput("");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Career agent</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </header>

      <p className="text-sm text-gray-600">
        Ask in plain English. The agent plans which tool to use (skill extraction, job matching,
        tailored documents, bullet optimizer or prep sheet), runs it, and answers from the real tool
        result.
      </p>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <section className={`${card} space-y-3`}>
        <textarea
          className={area}
          placeholder="Ask the career agent..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
        />
        <div className="flex flex-wrap items-center gap-2">
          <button className={btn} disabled={busy || !input.trim()} onClick={onSend}>
            {busy ? "Thinking..." : "Ask agent"}
          </button>
          {examples.map((ex) => (
            <button
              key={ex}
              className="rounded-full border border-gray-300 px-3 py-1 text-xs text-gray-700 hover:bg-gray-50"
              onClick={() => setInput(ex)}
            >
              {ex.slice(0, 38)}...
            </button>
          ))}
        </div>
      </section>

      {turns
        .slice()
        .reverse()
        .map((t, i) => (
          <section key={turns.length - i} className={`${card} space-y-2`}>
            <p className="text-sm font-semibold text-gray-900">You</p>
            <p className="whitespace-pre-wrap text-sm text-gray-800">{t.request}</p>
            <p className="pt-2 text-sm font-semibold text-gray-900">Agent</p>
            <p className="whitespace-pre-wrap text-sm text-gray-800">{t.answer}</p>
          </section>
        ))}
    </main>
  );
}