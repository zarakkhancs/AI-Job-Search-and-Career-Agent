# 3.1 Feature-to-Design Traceability

| Feature | Description | Type | Related Use Case | Primary Classes | Key Methods | Design Pattern |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **F01 - Resume Import & Parsing** | Extracts structured data such as skills and experience from a PDF. | Functional | UC-01 | `IngestionService`, `PDFResumeExtractor` | `ingestResume()`, `extract()` | Strategy |
| **F02 - Job Posting Ingestion** | Imports job requirements from a URL or pasted text. | Functional | UC-02 | `IngestionService`, `WebJobScraper`, `PastedTextJobParser` | `ingestJob()`, `extract()` | Strategy |
| **F03 - Skill Gap & Match Analysis** | Cross-references resume data against a job to identify missing skills. | Core / AI | UC-03 | `AIEngineFacade`, `MatchPromptFactory`, `MatchAnalysisPrompt` | `analyzeSkillGap()`, `createPrompt()` | Facade, Factory Method |
| **F04 - Tailored Cover Letter** | Drafts a customized cover letter aligning with a job description. | Core / AI | UC-04 | `AIEngineFacade`, `CoverLetterPromptFactory`, `CoverLetterPrompt` | `generateCoverLetter()`, `createPrompt()` | Facade, Factory Method |
| **F05 - Pipeline State Management** | Moves an application between pipeline stages (e.g., Wishlist, Applied). | Core Logic | UC-05 | `JobApplication`, `ApplicationState`, `WishlistState` | `changeState()`, `handleTransition()` | State |
| **F06 - Deadline Tracking & Alerts** | Records deadlines and pushes alerts when a due date is within 48 hours. | UI / Event | UC-06 | `DeadlineSubject`, `DashboardObserver`, `NotificationAlertObserver` | `notifyObservers()`, `onDeadlineEvent()` | Observer |
| **F07 - Interview Prep Sheet** | Generates 5 technical and 3 behavioral questions tailored to a job. | Core / AI | UC-07 | `AIEngineFacade`, `PrepSheetPromptFactory`, `InterviewPrompt` | `generatePrepSheet()`, `createPrompt()` | Facade, Factory Method |
| **F08 - Bullet Point Optimizer** | Generates three improved versions of a resume bullet point. | Core / AI | UC-08 | `AIEngineFacade`, `BulletPromptFactory`, `BulletOptimizerPrompt` | `optimizeBullet()`, `createPrompt()` | Facade, Factory Method |
| **F09 - Search Analytics Dashboard** | Visualizes application volume, stages, and conversion rates. | Functional | UC-09 | `AnalyticsController`, `MetricsService` | `getDashboardMetrics()`, `calculateConversionRate()` | MVC (Service) |
| **F10 - Application Package Exporter** | Compiles the resume and cover letter into a downloadable document. | Functional | UC-10 | `ExportController`, `DocumentGenerator` | `exportPackage()`, `generatePDF()` | MVC (Controller) |
| **F11 - Career Agent Assistant** | Completes multi-step preparation tasks from natural language via AI planning. | Core / AI | UC-11 | `AgentController`, `Planner`, `ToolManager`, `Tool` | `handleRequest()`, `createPlan()`, `executeTool()` | Command, Facade |
| **F12 - Mock Interview Practice** | Provides practice answering questions with AI-generated feedback. | Core / AI | UC-12 | `AIEngineFacade`, `MockInterviewPromptFactory`, `MockInterviewSession` | `submitAnswer()`, `evaluateAnswer()` | Facade, Factory Method |

# 3.2 Feature Traceability Explanations

**F01 — Resume Import & Parsing**
*   **Related Use Case:** UC-01 Import Resume
*   **Related Sequence Diagram:** SD-01 Import Resume
*   **Classes involved:** `ResumeController`, `IngestionService`, `ExtractionStrategy`, `PDFResumeExtractor`, `AIEngineFacade`.
*   **Important methods:** `IngestionService.ingestResume()`, `PDFResumeExtractor.extract()`.
*   **Execution:** When a user uploads a PDF, the controller routes it to the `IngestionService`. The service utilizes the `PDFResumeExtractor` concrete strategy to pull raw text. This text is sent through the AI Facade for categorization via the Gemini API and saved to the database as structured data.

**F02 — Job Posting Ingestion**
*   **Related Use Case:** UC-02 Import Job Posting
*   **Related Sequence Diagram:** SD-02 Import Job Posting
*   **Classes involved:** `JobController`, `IngestionService`, `WebJobScraper`, `PastedTextJobParser`.
*   **Important methods:** `IngestionService.ingestJob()`, `WebJobScraper.extract()`, `PastedTextJobParser.extract()`.
*   **Execution:** When the user submits a URL or text, `ingestJob()` evaluates the input source. The context selects the appropriate strategy (`WebJobScraper` or `PastedTextJobParser`) to deterministically extract the core fields. The structured entity is saved as a new Kanban card in the database.

**F03 — Skill Gap & Match Analysis**
*   **Related Use Case:** UC-03 Analyze Skill Gap and Match
*   **Related Sequence Diagram:** SD-03 Analyze Skill Gap and Match
*   **Classes involved:** `AIFeatureController`, `AIEngineFacade`, `MatchPromptFactory`, `GeminiClient`.
*   **Important methods:** `AIEngineFacade.analyzeSkillGap()`, `MatchPromptFactory.createPrompt()`.
*   **Execution:** The frontend requests a match analysis via `AIFeatureController`. The request hits the `AIEngineFacade`, which delegates prompt construction to `MatchPromptFactory`. The formatted prompt is transmitted via the `GeminiClient` adapter, which parses the JSON response into a `MatchResult`.

**F04 — Tailored Cover Letter Generator**
*   **Related Use Case:** UC-04 Generate Cover Letter
*   **Related Sequence Diagram:** SD-04 Generate Cover Letter
*   **Classes involved:** `AIFeatureController`, `AIEngineFacade`, `CoverLetterPromptFactory`, `GeminiClient`.
*   **Important methods:** `AIEngineFacade.generateCoverLetter()`, `CoverLetterPromptFactory.createPrompt()`.
*   **Execution:** Selecting generate sends job constraints to `AIFeatureController`. The `AIEngineFacade` requests a `CoverLetterPrompt` from the factory. The context is passed through the `GeminiClient` adapter to the LLM. The result is serialized into a `CoverLetter` object and returned to the UI.

**F05 — Application Pipeline State Management**
*   **Related Use Case:** UC-05 Manage Application Pipeline
*   **Related Sequence Diagram:** SD-05 Manage Application Pipeline
*   **Classes involved:** `ApplicationService`, `JobApplication`, `ApplicationState`, `WishlistState`, `AppliedState`.
*   **Important methods:** `JobApplication.changeState()`, `ApplicationState.handleTransition()`.
*   **Execution:** Moving a card triggers `ApplicationService.changeStage()`. The `JobApplication` context class delegates validation logic to its current `ApplicationState` via `handleTransition()`. If valid, the new state is assigned and persisted to the PostgreSQL database.

**F06 — Deadline Tracking & Notification Alerts**
*   **Related Use Case:** UC-06 Track Deadlines and Receive Alerts
*   **Related Sequence Diagram:** SD-06 Track Deadlines and Receive Alerts
*   **Classes involved:** `DeadlineScheduler`, `DeadlineSubject`, `DashboardObserver`, `NotificationAlertObserver`.
*   **Important methods:** `DeadlineSubject.checkDeadlines()`, `DeadlineObserver.onDeadlineEvent()`.
*   **Execution:** The `DeadlineScheduler` periodically triggers `checkDeadlines()`. If the subject detects dates within a 48-hour threshold, it constructs a `DeadlineEvent` and pushes it via `notifyObservers()`. Registered UI observers receive the event and render alert banners immediately.

**F07 — Interview Prep Sheet Generator**
*   **Related Use Case:** UC-07 Generate Interview Prep Sheet
*   **Related Sequence Diagram:** SD-07 Generate Interview Prep Sheet
*   **Classes involved:** `AIFeatureController`, `AIEngineFacade`, `PrepSheetPromptFactory`, `GeminiClient`.
*   **Important methods:** `AIEngineFacade.generatePrepSheet()`, `PrepSheetPromptFactory.createPrompt()`.
*   **Execution:** Requesting interview materials triggers the facade to task `PrepSheetPromptFactory` with constructing an `InterviewPrompt`. This prompt queries the LLM via the client adapter to produce customized technical questions, which are parsed into a `PrepSheet` domain object.

**F08 — Resume Bullet Point Optimizer**
*   **Related Use Case:** UC-08 Optimize Resume Bullet Point
*   **Related Sequence Diagram:** SD-08 Optimize Resume Bullet Point
*   **Classes involved:** `AIFeatureController`, `AIEngineFacade`, `BulletPromptFactory`, `BulletOptimizerPrompt`.
*   **Important methods:** `AIEngineFacade.optimizeBullet()`, `BulletPromptFactory.createPrompt()`.
*   **Execution:** Highlighting a bullet point invokes `optimizeBullet()`. The facade delegates to the factory to build a context-aware prompt using the original text. The LLM processes this prompt and returns three optimized variations for the user to evaluate and save.

**F09 — Job Search Analytics Dashboard**
*   **Related Use Case:** UC-09 View Job Search Analytics
*   **Related Sequence Diagram:** SD-09 View Job Search Analytics
*   **Classes involved:** `AnalyticsController`, `MetricsService`.
*   **Important methods:** `AnalyticsController.getDashboardMetrics()`, `MetricsService.countByStage()`, `MetricsService.calculateConversionRate()`.
*   **Execution:** Navigating to the analytics view calls `getDashboardMetrics()`. The `MetricsService` executes optimized SQL queries against PostgreSQL to aggregate pipeline volumes and conversion rates, returning structured JSON data required to render GUI charts.

**F10 — Application Package Exporter**
*   **Related Use Case:** UC-10 Export Application Package
*   **Related Sequence Diagram:** SD-10 Export Application Package
*   **Classes involved:** `ExportController`, `DocumentGenerator`.
*   **Important methods:** `ExportController.exportPackage()`, `DocumentGenerator.generatePDF()`.
*   **Execution:** Selecting export triggers the `ExportController` to pull finalized resume and cover letter data. It hands this payload to `DocumentGenerator.generatePDF()`, which maps the text to standard layouts and streams the compiled file to the UI.

**F11 — Career Agent Assistant**
*   **Related Use Case:** UC-11 Run Career Agent Assistant
*   **Related Sequence Diagram:** SD-11 Run Career Agent Assistant
*   **Classes involved:** `AgentController`, `Planner`, `ToolManager`, `Tool`, `AIEngineFacade`.
*   **Important methods:** `AgentController.handleRequest()`, `Planner.createPlan()`, `ToolManager.executeTool()`.
*   **Execution:** Submitting a natural language prompt invokes `handleRequest()`. The `Planner` utilizes the AI facade to generate a structured `Plan` containing executable steps. The controller iterates through each `PlanStep`, calling `ToolManager.executeTool()` to trigger specific backend capabilities (e.g., `MatchAnalysisTool`), accumulating the results for the user.

**F12 — Mock Interview Practice**
*   **Related Use Case:** UC-12 Practice Mock Interview
*   **Related Sequence Diagram:** SD-12 Practice Mock Interview
*   **Classes involved:** `AIFeatureController`, `MockInterviewSession`, `AIEngineFacade`, `MockInterviewPromptFactory`.
*   **Important methods:** `AIFeatureController.submitAnswer()`, `AIEngineFacade.evaluateAnswer()`, `MockInterviewSession.recordAnswer()`.
*   **Execution:** A `MockInterviewSession` statefully manages questions. When a user submits an answer, the `AIEngineFacade` uses `MockInterviewPromptFactory` to inject the context into a prompt. The LLM returns a structured critique, which is parsed into `AnswerFeedback`, stored via `recordAnswer()`, and rendered alongside the next question.
