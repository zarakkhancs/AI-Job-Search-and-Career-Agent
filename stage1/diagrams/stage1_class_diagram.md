# 2.1 Class Diagram

The class diagram is split into four views so every class, attribute and method stays readable. Views are connected by shared class names: a class shown as an empty box in one view is fully defined in another.

| View | Contents |
|---|---|
| A | Presentation (GUI, CLI), controllers, services, export, analytics |
| B | Agent layer and AI subsystem (Planner, Tools, Memory, Facade, Prompt Factory, LLM Adapter) |
| C | Domain model, State pattern, Observer pattern, repositories |
| D | Ingestion (Strategy pattern) |

Persistence note: every `*Repository` interface has a PostgreSQL implementation (for example `PostgresJobApplicationRepository`), omitted for readability.

---

## View A: Presentation, Controllers and Services

![class_diagram_view_A_presentation_controllers_services](umlet/png/class_diagram_view_A_presentation_controllers_services.png)

*UMLet source: [`class_diagram_view_A_presentation_controllers_services.uxf`](umlet/class_diagram_view_A_presentation_controllers_services.uxf)*

---

## View B: Agent Layer and AI Subsystem

![class_diagram_view_B_agent_layer_ai_subsystem](umlet/png/class_diagram_view_B_agent_layer_ai_subsystem.png)

*UMLet source: [`class_diagram_view_B_agent_layer_ai_subsystem.uxf`](umlet/class_diagram_view_B_agent_layer_ai_subsystem.uxf)*

---

## View C: Domain Model, State Pattern, Observer Pattern, Repositories

![class_diagram_view_C_domain_state_observer_repositories](umlet/png/class_diagram_view_C_domain_state_observer_repositories.png)

*UMLet source: [`class_diagram_view_C_domain_state_observer_repositories.uxf`](umlet/class_diagram_view_C_domain_state_observer_repositories.uxf)*

---

## View D: Ingestion (Strategy Pattern)

![class_diagram_view_D_ingestion_strategy](umlet/png/class_diagram_view_D_ingestion_strategy.png)

*UMLet source: [`class_diagram_view_D_ingestion_strategy.uxf`](umlet/class_diagram_view_D_ingestion_strategy.uxf)*

---

## 2.1.1 Design Patterns: Participant Map

| # | Pattern | Context / Creator / Subject | Abstraction | Concrete classes |
|---|---|---|---|---|
| 1 | Strategy | `IngestionService` | `ExtractionStrategy` | `PDFResumeExtractor`, `WebJobScraper`, `PastedTextJobParser` |
| 2 | Factory Method | `PromptFactory` (creator) | `LLMPrompt` (product) | 7 concrete factories; `MatchAnalysisPrompt`, `CoverLetterPrompt`, `InterviewPrompt`, `BulletOptimizerPrompt`, `ResumeParsePrompt`, `PlanningPrompt`, `MockInterviewPrompt` |
| 3 | State | `JobApplication` | `ApplicationState` | `WishlistState`, `AppliedState`, `InterviewingState`, `OfferState`, `RejectedState` |
| 4 | Facade | `AIEngineFacade` | (single entry point) | Hides the `PromptFactory` hierarchy, `LLMClient`, `JSONResponseParser`, `ResponseValidator` |
| 5 | Observer | `DeadlineSubject` | `DeadlineObserver` | `DashboardObserver`, `NotificationAlertObserver` (triggered by `DeadlineScheduler`) |
| 6 | Command | `CommandInvoker` | `Command` | `ImportResumeCommand`, `ImportJobCommand`, `AnalyzeMatchCommand`, `GenerateCoverLetterCommand`, `RunAgentCommand`, `GeneratePrepSheetCommand`, `OptimizeBulletCommand`, `MovePipelineCommand`, `ViewAnalyticsCommand`, `ExportPackageCommand`, `StartMockInterviewCommand` (receivers are the controllers) |
| 7 | Adapter | `GeminiClient` (object adapter) | `LLMClient` (target) | Holds the external `GeminiRestAPI` (adaptee) |

## 2.1.2 Design Principles Demonstrated

- **Dependency inversion:** `AIEngineFacade` depends on the `LLMClient` interface, not on Gemini. Services depend on `*Repository` interfaces, not on PostgreSQL.
- **Separation of concerns:** controllers handle requests, services hold use-case logic, states only validate transitions, repositories only persist, the facade only talks to the AI subsystem.
- **Open/Closed:** a new input type means a new `ExtractionStrategy`, a new AI feature means a new `PromptFactory` plus `LLMPrompt` pair, a new pipeline stage means a new `ApplicationState`, and a new agent capability means a new `Tool`. None of these changes existing classes.
- **Testability:** `LLMClient` can be mocked, so deterministic components can be unit tested without calling Gemini. `ResponseValidator` and `ToolManager` give the agent explicit points where behavior can be checked.
