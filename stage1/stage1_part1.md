# 1. Project Overview

## 1.1.1 Problem and Motivation
Software engineering students and early-career developers face a severe logistical bottleneck during recruitment cycles. Applying to hundreds of co-ops and full-time roles requires manually tracking application statuses, extracting requirements from job descriptions, and tailoring resumes to specific postings. Existing tools are purely deterministic and cannot assist with the qualitative, time-consuming tasks of adapting unstructured applicant materials to diverse job requirements.

## 1.1.2 Target Users
The primary users are university/college students, particularly those in computer science and engineering, who are actively applying for internships, co-ops, or new-grad software engineering roles.

## 1.1.3 Agent Description
The AI Job Search and Career Agent is an AI agent-based software system that acts as an intelligent career counselor. Users can interact with the system via a web GUI or a CLI to upload their base resume, ingest job postings via URL or text, and manage applications in a Kanban-style dashboard. The embedded AI agent evaluates the semantic match between the user's experience and job requirements, rewrites resume bullet points, identifies skill gaps, conducts interactive mock interviews, and dynamically plans and executes multi-step job preparation tasks.

## 1.1.4 AI/LLM Models and Appropriateness
An AI agent is appropriate for this problem because analyzing unstructured resume text against diverse job descriptions requires semantic understanding and reasoning, which cannot be achieved through traditional deterministic string matching. The system will integrate with a Large Language Model (Gemini API) to handle natural language processing, dynamic tool planning, and generative reasoning tasks.

## 1.1.5 Overall Architecture
The system features a modern web-based Graphical User Interface (GUI) built with React, Next.js, and Tailwind CSS, alongside a Command Line Interface (CLI) utilizing the Command pattern for text-based interactions. Both interfaces communicate with a robust Java backend. The backend enforces strict object-oriented design principles to handle deterministic business logic and state management, utilizing PostgreSQL for data persistence. For AI features, the backend utilizes an Adapter pattern to communicate securely with the LLM API, routing multi-step natural language requests through an intelligent Planner and executing specialized backend tools.

## 1.2.1 Feature Specification

**F01 - Resume Import & Parsing**
*   **Description:** Extracts structured data such as skills, work experience, and education from a user-uploaded resume.
*   **User Interaction:** The user clicks "Upload Resume" in the GUI or uses the CLI to submit a PDF file.
*   **Input:** A PDF resume file.
*   **Output:** A structured data object containing extracted resume fields.
*   **AI Involvement:** Hybrid. Deterministic code extracts raw text; the LLM categorizes the text into data fields.
*   **Expected Workflow:** The backend extracts raw text and sends a prompt to the Gemini API. The backend receives the structured response, saves it to PostgreSQL, and returns it for review.
*   **Error/Alternative Cases:** If the PDF is unreadable, the system displays an error message prompting manual entry.

**F02 - Job Posting Ingestion**
*   **Description:** Imports job requirements, responsibilities, and company details from a provided URL or pasted text.
*   **User Interaction:** The user pastes a URL or text into the GUI or CLI and clicks "Import Job".
*   **Input:** A URL string or raw text block.
*   **Output:** A saved job posting entity, displayed as a new Kanban card.
*   **AI Involvement:** Deterministic. The backend scrapes the URL or processes the text.
*   **Expected Workflow:** The backend parses the text to identify core fields, saves the data, and updates the board.
*   **Error/Alternative Cases:** If a URL cannot be scraped due to bot protection, the system requests raw text instead.

**F03 - Skill Gap & Match Analysis**
*   **Description:** Cross-references parsed resume data against the job posting to identify missing qualifications.
*   **User Interaction:** The user clicks "Run Match Analysis" on a job card.
*   **Input:** Parsed resume data and job posting text.
*   **Output:** A percentage match score and a list of missing key skills.
*   **AI Involvement:** AI-based. The LLM evaluates alignment using semantic comparison.
*   **Expected Workflow:** The backend constructs a prompt requesting a match score, queries the API, and displays the analysis.
*   **Error/Alternative Cases:** If the API times out, the backend logs the error and prompts the user to retry.

**F04 - Tailored Cover Letter Generator**
*   **Description:** Drafts a customized cover letter by aligning the user's background with the specific job description.
*   **User Interaction:** The user clicks "Generate Cover Letter" on a job card and optionally inputs a tone.
*   **Input:** Parsed resume data, the job description, and tone preferences.
*   **Output:** A formatted text block containing the generated cover letter.
*   **AI Involvement:** AI-based. The LLM writes the narrative content.
*   **Expected Workflow:** The backend packages the profile and job into a prompt, queries the LLM, saves the text, and presents it in the GUI.
*   **Error/Alternative Cases:** If no resume exists, the system blocks the request and prompts profile completion.

**F05 - Application Pipeline State Management**
*   **Description:** Manages the lifecycle of a job application by moving it across different statuses.
*   **User Interaction:** The user drags and drops a job card between Kanban columns.
*   **Input:** The job card ID and the new status column.
*   **Output:** An updated state in the database and a visually refreshed Kanban board.
*   **AI Involvement:** Deterministic. Transitions are handled strictly by state validation logic.
*   **Expected Workflow:** The backend validates the transition, updates the PostgreSQL record, and returns a success response.
*   **Error/Alternative Cases:** If the transition is illegal, the GUI reverts the card to its original column.

**F06 - Deadline Tracking & Notification Alerts**
*   **Description:** Monitors application windows to alert the user of impending deadlines.
*   **User Interaction:** The user sets a deadline date on a job card.
*   **Input:** A date and time.
*   **Output:** A visual alert indicator on the dashboard for overdue or urgent tasks.
*   **AI Involvement:** Deterministic. Time calculations use conditional logic.
*   **Expected Workflow:** A background scheduler checks the database. Deadlines within 48 hours trigger an alert pushed to the GUI.
*   **Error/Alternative Cases:** If an invalid date is entered, the GUI prevents submission.

**F07 - Interview Prep Sheet Generator**
*   **Description:** Generates a targeted list of technical and behavioral interview questions.
*   **User Interaction:** The user clicks "Generate Prep Sheet" within an active job application.
*   **Input:** The target job description.
*   **Output:** A document containing 5 technical and 3 behavioral questions.
*   **AI Involvement:** AI-based. The LLM predicts likely interview topics based on the job posting.
*   **Expected Workflow:** The backend sends the job posting to the API, stores the returned questions, and displays them.
*   **Error/Alternative Cases:** If the job description lacks detail, the AI defaults to standard behavioral questions and notifies the user.

**F08 - Resume Bullet Point Optimizer**
*   **Description:** Rewrites a specific experience bullet point to better align with a targeted job posting.
*   **User Interaction:** The user highlights a bullet point and clicks "Optimize for this Job".
*   **Input:** A bullet point string and the job description.
*   **Output:** Three AI-generated variations of the original bullet point.
*   **AI Involvement:** AI-based. The LLM performs contextual rewriting.
*   **Expected Workflow:** The backend sends the bullet and job context to the LLM. The user selects one of the three returned options to save.
*   **Error/Alternative Cases:** If the text lacks context, the system prompts the user for more specific details.

**F09 - Job Search Analytics Dashboard**
*   **Description:** Visualizes application success rates, interview conversions, and total volume.
*   **User Interaction:** The user navigates to the "Analytics" tab.
*   **Input:** Historical application data from the database.
*   **Output:** Visual charts displaying application metrics.
*   **AI Involvement:** Deterministic. Calculations rely on SQL aggregations.
*   **Expected Workflow:** The backend executes aggregation queries to calculate totals and rates, returning formatted JSON for rendering.
*   **Error/Alternative Cases:** If history is empty, the GUI displays placeholder charts.

**F10 - Application Package Exporter**
*   **Description:** Compiles the optimized resume and cover letter into a downloadable format.
*   **User Interaction:** The user clicks "Export Application Package".
*   **Input:** Finalized resume data and the generated cover letter text.
*   **Output:** A downloadable PDF containing the application documents.
*   **AI Involvement:** Deterministic. The system formats existing strings into a document layout.
*   **Expected Workflow:** The backend retrieves text, applies a template, converts to PDF, and serves the file for download.
*   **Error/Alternative Cases:** If PDF generation fails, the system provides a plain text fallback.

**F11 - Career Agent Assistant**
*   **Description:** Completes multi-step job preparation tasks from a single natural-language request.
*   **User Interaction:** The user types a natural-language request (e.g., "Prepare me for the Shopify role") in the GUI or CLI.
*   **Input:** A natural-language string and optional job context.
*   **Output:** Executed application artifacts (cover letters, prep sheets) and a summarized response.
*   **AI Involvement:** AI-based. The LLM acts as a planner to break the request into actionable steps.
*   **Expected Workflow:** The backend `Planner` translates the prompt into a sequence of tool calls. The `ToolManager` executes these calls (e.g., `MatchAnalysisTool`, `CoverLetterTool`) and returns the aggregated results.
*   **Error/Alternative Cases:** If the request is ambiguous, the agent asks a clarifying question instead of executing a flawed plan.

**F12 - Mock Interview Practice**
*   **Description:** Provides interactive practice for interview questions with AI-generated feedback.
*   **User Interaction:** The user clicks "Start Mock Interview" and types answers to provided questions.
*   **Input:** The user's typed answers and the interview question context.
*   **Output:** Graded feedback and suggestions for improvement on each answer.
*   **AI Involvement:** AI-based. The LLM evaluates the strength and accuracy of the user's answers.
*   **Expected Workflow:** The backend loads a session, sends the user's answer to the LLM for evaluation, and displays the critique before presenting the next question.
*   **Error/Alternative Cases:** If the user submits an empty answer, the GUI prompts them to answer or skip the question.
