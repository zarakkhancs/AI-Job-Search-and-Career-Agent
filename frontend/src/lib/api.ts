export type Alert = { id: number; message: string; source: string; read: boolean };
export type Deadline = { label: string; date: string };

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

  getDeadlines: () => request<Deadline[]>("/deadlines"),
  addDeadline: (label: string, date: string) =>
    request<Deadline>("/deadlines", post({ label, date })),
  checkDeadlines: () => request<{ created: unknown }>("/deadlines/check", post()),

  getAlerts: () => request<Alert[]>("/alerts"),
  markAlertRead: (id: number) => request<{ status: string }>(`/alerts/${id}/read`, post()),
  markAllRead: () => request<{ marked: unknown }>("/alerts/read-all", post()),

  extractSkills: (text: string) => request<unknown>("/extract", post({ text })),
};
