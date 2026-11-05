"use client";

import Link from "next/link";
import { useState } from "react";
import {
  api,
  type AnswerFeedback,
  type InterviewSession,
  type InterviewSummary,
} from "@/lib/api";

const card = "rounded-xl border border-gray-200 bg-white p-5 shadow-sm";
const btn =
  "rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50";
const area = "h-40 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900";

function scoreTone(score: number) {
  if (score >= 7) return "bg-green-100 text-green-800";
  if (score >= 4) return "bg-amber-100 text-amber-800";
  return "bg-red-100 text-red-800";
}

function Flagged({ items }: { items: string[] }) {
  if (items.length === 0) return null;
  return (
    <div className="mt-3 rounded-lg border border-amber-300 bg-amber-50 p-3 text-sm text-amber-900">
      <p className="mb-2 font-semibold">
        Factual check: your answer claims these skills, but they are not on your resume.
      </p>
      <div className="flex flex-wrap gap-2">
        {items.map((s) => (
          <span key={s} className="rounded-full bg-red-100 px-3 py-1 text-xs font-medium text-red-800">
            {s}
          </span>
        ))}
      </div>
    </div>
  );
}

export default function InterviewPage() {
  const [resume, setResume] = useState("");
  const [job, setJob] = useState("");
  const [count, setCount] = useState(3);
  const [session, setSession] = useState<InterviewSession | null>(null);
  const [index, setIndex] = useState(0);
  const [answer, setAnswer] = useState("");
  const [feedback, setFeedback] = useState<AnswerFeedback | null>(null);
  const [history, setHistory] = useState<AnswerFeedback[]>([]);
  const [summary, setSummary] = useState<InterviewSummary | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function guarded(action: () => Promise<void>) {
    setError("");
    setBusy(true);
    try {
      await action();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  const onStart = () =>
    guarded(async () => {
      const s = await api.startInterview(resume, job, count);
      setSession(s);
      setIndex(0);
      setAnswer("");
      setFeedback(null);
      setHistory([]);
      setSummary(null);
    });

  const onSubmit = () =>
    guarded(async () => {
      if (!session) return;
      const q = session.questions[index];
      const fb = await api.answerQuestion(session.id, q.id, answer);
      setFeedback(fb);
      setHistory((h) => [...h, fb]);
    });

  const onFinish = () =>
    guarded(async () => {
      if (!session) return;
      setSummary(await api.finishInterview(session.id));
    });

  function onNext() {
    if (!session) return;
    if (index + 1 < session.questions.length) {
      setIndex(index + 1);
      setAnswer("");
      setFeedback(null);
    } else {
      onFinish();
    }
  }

  function reset() {
    setSession(null);
    setSummary(null);
    setHistory([]);
    setFeedback(null);
    setAnswer("");
    setIndex(0);
    setError("");
  }

  const question = session ? session.questions[index] : null;
  const isLast = session ? index + 1 >= session.questions.length : false;

  return (
    <main className="mx-auto max-w-4xl space-y-6 p-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Mock interview</h1>
        <Link href="/" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </header>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      {!session && (
        <section className={`${card} grid gap-4 md:grid-cols-2`}>
          <div>
            <h2 className="mb-2 text-sm font-semibold text-gray-900">Your resume</h2>
            <textarea className={area} placeholder="Paste your resume text..." value={resume} onChange={(e) => setResume(e.target.value)} />
          </div>
          <div>
            <h2 className="mb-2 text-sm font-semibold text-gray-900">Job posting</h2>
            <textarea className={area} placeholder="Paste the job description..." value={job} onChange={(e) => setJob(e.target.value)} />
          </div>
          <div className="flex items-center gap-3 md:col-span-2">
            <label className="text-sm text-gray-700" htmlFor="count">
              Questions
            </label>
            <select
              id="count"
              className="rounded-lg border border-gray-300 px-2 py-1.5 text-sm text-gray-900"
              value={count}
              onChange={(e) => setCount(Number(e.target.value))}
            >
              {[2, 3, 5].map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
            <button className={btn} disabled={busy || !resume.trim() || !job.trim()} onClick={onStart}>
              {busy ? "Starting..." : "Start interview"}
            </button>
          </div>
        </section>
      )}

      {session && question && !summary && (
        <section className={card}>
          <div className="mb-3 flex items-center justify-between">
            <span className="text-sm text-gray-500">
              Question {index + 1} of {session.questions.length}
            </span>
            <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
              {question.type}
            </span>
          </div>
          <p className="mb-4 text-base font-medium text-gray-900">{question.text}</p>
          <textarea
            className={area}
            placeholder="Type your answer..."
            value={answer}
            disabled={feedback !== null}
            onChange={(e) => setAnswer(e.target.value)}
          />
          <div className="mt-3 flex gap-2">
            {feedback === null ? (
              <button className={btn} disabled={busy || !answer.trim()} onClick={onSubmit}>
                {busy ? "Scoring..." : "Submit answer"}
              </button>
            ) : (
              <button className={btn} disabled={busy} onClick={onNext}>
                {isLast ? "Finish interview" : "Next question"}
              </button>
            )}
          </div>

          {feedback && (
            <div className="mt-5 border-t border-gray-100 pt-4">
              <div className="mb-2 flex items-center gap-3">
                <span className={`rounded-full px-3 py-1 text-sm font-semibold ${scoreTone(feedback.score)}`}>
                  {feedback.score}/10
                </span>
                <span className="text-sm font-semibold text-gray-900">Feedback</span>
              </div>
              <p className="whitespace-pre-wrap text-sm text-gray-800">{feedback.feedback}</p>
              <Flagged items={feedback.flaggedClaims} />
            </div>
          )}
        </section>
      )}

      {session && summary && (
        <>
          <section className={card}>
            <h2 className="mb-3 text-lg font-semibold text-gray-900">Interview summary</h2>
            <div className="flex gap-8 text-sm text-gray-700">
              <p>
                Answered{" "}
                <span className="font-semibold text-gray-900">
                  {summary.answered} of {summary.total}
                </span>
              </p>
              <p>
                Average score{" "}
                <span className="font-semibold text-gray-900">{summary.averageScore.toFixed(1)}/10</span>
              </p>
            </div>
            <Flagged items={summary.flaggedClaims} />
            <div className="mt-4">
              <button className={btn} onClick={reset}>
                Start a new interview
              </button>
            </div>
          </section>

          {history.map((fb) => {
            const q = session.questions.find((x) => x.id === fb.questionId);
            return (
              <section key={fb.questionId} className={card}>
                <p className="mb-2 text-sm font-medium text-gray-900">{q?.text}</p>
                <p className="mb-2 whitespace-pre-wrap text-sm text-gray-600">Your answer: {fb.answer}</p>
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${scoreTone(fb.score)}`}>
                  {fb.score}/10
                </span>
                <p className="mt-2 whitespace-pre-wrap text-sm text-gray-800">{fb.feedback}</p>
              </section>
            );
          })}
        </>
      )}
    </main>
  );
}
