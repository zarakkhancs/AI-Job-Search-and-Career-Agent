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
