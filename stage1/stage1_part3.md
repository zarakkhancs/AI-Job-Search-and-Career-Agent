# 1.3 Feature-to-Design Traceability

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
