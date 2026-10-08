# AI Job Search and Career Agent - EECS 3311 Stage 1

Stage 1 (Software and Agent Design) documentation for the AI Job Search and Career Agent. The system acts as an intelligent career counselor for software engineering students. It has a Next.js/React/Tailwind web GUI and a CLI, a Java backend with PostgreSQL persistence, and uses **Gemini 3.1 Pro** (via the Gemini API) for AI features and agent planning.

## Stage 1 Report - Table of Contents

Please read the files in this order. Together they form the complete Stage 1 report.

| # | Section | File |
|---|---|---|
| 1 | Project overview (problem, users, agent, LLM, architecture) and feature specifications F01-F12 | [stage1/stage1_part1.md](./stage1/stage1_part1.md) |
| 2 | UML class diagram (Views A-D), pattern participant map, design principles | [stage1/diagrams/stage1_class_diagram.md](./stage1/diagrams/stage1_class_diagram.md) |
| 3 | UML use-case diagram and use-case descriptions UC-01 to UC-12 | [stage1/diagrams/stage1_use_cases.md](./stage1/diagrams/stage1_use_cases.md) |
| 4 | Design pattern explanations (7 patterns) | [stage1/stage1_part2.md](./stage1/stage1_part2.md) |
| 5 | Sequence diagrams SD-01 to SD-13 | [stage1/diagrams/stage1_sequence_diagrams.md](./stage1/diagrams/stage1_sequence_diagrams.md) |
| 6 | Feature-to-design traceability table | [stage1/stage1_part3.md](./stage1/stage1_part3.md) |
| 7 | Feature implementation explanations | [stage1/stage1_part4.md](./stage1/stage1_part4.md) |

All diagrams are UMLet diagrams. Sources (`.uxf`) and exported images (`.png`) are in [stage1/diagrams/umlet/](./stage1/diagrams/umlet/).

## Quick Summary

- **Features:** 12 (7 AI-based or hybrid, 5 deterministic), each with a use case, sequence diagram and classes in the traceability table.
- **Interfaces:** GUI (Next.js) and CLI (Command pattern).
- **Design patterns:** Strategy, Factory Method, State, Facade, Observer, Command, Adapter.
- **Agent behavior:** Planner, ToolManager with 6 tools, MemoryManager, ResponseValidator, and replanning on failure.
