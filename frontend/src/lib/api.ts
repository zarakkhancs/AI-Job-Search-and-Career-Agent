export type Alert = { id: number; message: string; source: string; read: boolean };
export type Deadline = { label: string; date: string };
export type Extraction = { verified: string[]; rejected: string[] };
export type MatchResult = { score: number; matched: string[]; missing: string[] };
export type Tailored = {
  summary: string;
  coverLetter: string;
  supportedSkills: string[];
  flaggedSkills: string[];
};
export type InterviewQuestion = { id: number; type: string; text: string };
export type InterviewSession = {
  id: string;
  status: string;
  questions: InterviewQuestion[];
  feedback: unknown[];
};
export type AnswerFeedback = {
  questionId: number;
  answer: string;
  score: number;
  feedback: string;
  flaggedClaims: string[];
};
export type InterviewSummary = {
  answered: number;
  total: number;
  averageScore: number;
  flaggedClaims: string[];
};

export type BulletVersion = { text: string; unsupportedFigures: string[] };
export type BulletResult = { original: string; versions: BulletVersion[] };
export type PrepSheet = { technical: string[]; behavioral: string[] };
export type ExportFile = { filename: string; content: string };
export type Application = { id: number; company: string; title: string; status: string };
export type Analytics = {
  total: number;
  submitted: number;
  byStage: Record<string, number>;
  interviewRate: number;
  offerRate: number;
  rejectionRate: number;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error((data && data.error) || `Request failed (${res.status})`);
  }
  return data as T;
}

const post = (body?: unknown): RequestInit => ({
  method: "POST",
  body: body === undefined ? undefined : JSON.stringify(body),
});

export const api = {
  health: () => request<{ status: string }>("/health"),

  getApplications: () => request<Application[]>("/applications"),
  addApplication: (company: string, title: string) =>
    request<Application>("/applications", post({ company, title })),
  moveApplication: (id: number, action: string) =>
    request<Application>(`/applications/${id}/move`, post({ action })),
  getAnalytics: () => request<Analytics>("/analytics"),

  optimizeBullet: (bullet: string, job: string) =>
    request<BulletResult>("/bullets/optimize", post({ bullet, job })),
  prepSheet: (job: string, resume: string) =>
    request<PrepSheet>("/prep", post({ job, resume })),

  getDeadlines: () => request<Deadline[]>("/deadlines"),
  addDeadline: (label: string, date: string) =>
    request<Deadline>("/deadlines", post({ label, date })),
  checkDeadlines: () => request<{ created: unknown }>("/deadlines/check", post()),

  getAlerts: () => request<Alert[]>("/alerts"),
  markAlertRead: (id: number) => request<{ status: string }>(`/alerts/${id}/read`, post()),
  markAllRead: () => request<{ marked: unknown }>("/alerts/read-all", post()),

  extractSkills: (text: string) => request<Extraction>("/extract", post({ text })),
  matchSkills: (resumeSkills: string[], jobSkills: string[]) =>
    request<MatchResult>("/match", post({ resumeSkills, jobSkills })),
  tailorDocuments: (resume: string, job: string) =>
    request<Tailored>("/documents/tailor", post({ resume, job })),
  exportPackage: (body: {
    label?: string;
    resume: string;
    summary: string;
    coverLetter: string;
    flaggedSkills: string[];
  }) => request<ExportFile>("/export", post(body)),

  startInterview: (resume: string, job: string, count: number) =>
    request<InterviewSession>("/interview/start", post({ resume, job, count })),
  answerQuestion: (id: string, questionId: number, answer: string) =>
    request<AnswerFeedback>(`/interview/${id}/answer`, post({ questionId, answer })),
  finishInterview: (id: string) =>
    request<InterviewSummary>(`/interview/${id}/finish`, post()),
};
