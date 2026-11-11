# AI Job Search and Career Agent - EECS 3311

A career assistant for software engineering students. It extracts skills from a resume, matches them to job postings, tailors cover letters, tracks applications through a pipeline, raises deadline alerts, generates interview prep, optimizes resume bullets, runs mock interviews and shows analytics. A Gemini-powered agent can plan and call these tools from one request. It has a Next.js GUI and a CLI, both on one Java backend.

## Stage 2 - how to run

Where things are: `backend/` (Java, package `com.careeragent`), `frontend/` (Next.js), `stage1/` (design documents), `stage2/` (report, UMLet diagrams, demo videos).

### Prerequisites

- JDK 25 and Maven 3.9 or newer
- Node.js 20 or newer (with npm)
- A Gemini API key from Google AI Studio (the free tier works)

### Configure the API key locally

Copy the example file and put your own key in the copy. The real file is ignored by git and must never be committed.

    copy .env.example .env

Open `.env` and set `LLM_API_KEY` to your key and `LLM_MODEL` to the Gemini model you want to use. The file belongs in the repository root. The automated tests use a mock LLM and need no key.

### Run the automated tests

    cd backend
    mvn test

### Run the GUI

Start the backend API (port 7070), then the web app, in two terminals.

    cd backend
    mvn -q exec:java "-Dexec.mainClass=com.careeragent.Main" "-Dexec.args=serve"

    cd frontend
    npm install
    npm run dev

Open http://localhost:3000. Pages: dashboard, resume, jobs, skills, tailor, pipeline, bullets, prep, interview, agent.

### Run the CLI

    cd backend
    mvn -q exec:java "-Dexec.mainClass=com.careeragent.Main"

Type `help` to list the commands (agent, alerts, ask, bullet, export, interview, job, prep, resume, stats, track).

### Example usage

In the GUI, open the Resume page, paste resume text or upload a PDF, then use Jobs to import a posting, Skills to see the match, and Tailor to generate a cover letter and export the package. In the CLI, the same features are available through the commands above.

### Notes

- Data (applications, alerts, deadlines, interview sessions) is kept in memory and resets when the backend restarts.
- The free Gemini tier sometimes returns 429 or 503 errors or non-JSON output; retrying usually works.
- Full Stage 2 report: `stage2/stage2-report.pdf`. Demo videos: `stage2/videos/`. UMLet diagrams: `stage2/diagrams/umlet/`.

---
## Stage 1 design documentation (original design; the Stage 2 report lists the changes)
Stage 1 (Software and Agent Design) documentation for the AI Job Search and Career Agent. The system acts as an intelligent career counselor for software engineering students. It has a Next.js/React/Tailwind web GUI and a CLI, a Java backend with PostgreSQL persistence, and uses **Gemini 3.1 Pro** (via the Gemini API) for AI features and agent planning.

## Stage 1 Report - Table of Contents

Please read the files in this order. Together they form the complete Stage 1 report.

| # | Section | File |
|---|---|---|
| 1 | Project overview (problem, users, agent, LLM, architecture) and feature specifications F01-F12 | [stage1/stage1_part1.md](./stage1/stage1_part1.md) |
| 2 | UML class diagram (Views A-D), pattern participant map, design principles | [stage1/diagrams/stage1_class_diagram.md](./stage1/diagrams/stage1_class_diagram.md) |
| 3 | UML use-case diagram and use-case descriptions UC-01 to UC-12 | [stage1/diagrams/stage1_use_cases.md](./stage1/diagrams/stage1_use_cases.md) |
| 4 | Sequence diagrams SD-01 to SD-13 | [stage1/diagrams/stage1_sequence_diagrams.md](./stage1/diagrams/stage1_sequence_diagrams.md) |
| 5 | Design pattern explanations (7 patterns) | [stage1/stage1_part2.md](./stage1/stage1_part2.md) |
| 6 | Feature-to-design traceability table | [stage1/stage1_part3.md](./stage1/stage1_part3.md) |
| 7 | Feature implementation explanations | [stage1/stage1_part4.md](./stage1/stage1_part4.md) |

All diagrams are UMLet diagrams. Sources (`.uxf`) and exported images (`.png`) are in [stage1/diagrams/umlet/](./stage1/diagrams/umlet/).

## Quick Summary

- **Features:** 12 (7 AI-based or hybrid, 5 deterministic), each with a use case, sequence diagram and classes in the traceability table.
- **Interfaces:** GUI (Next.js) and CLI (Command pattern).
- **Design patterns:** Strategy, Factory Method, State, Facade, Observer, Command, Adapter.
- **Agent behavior:** Planner, ToolManager with 6 tools, MemoryManager, ResponseValidator, and replanning on failure.
