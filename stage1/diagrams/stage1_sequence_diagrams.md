# 2.3 Sequence Diagrams

Every diagram uses the classes and methods defined in the class diagram (Section 2.1) and follows the main and alternative flows of the use cases (Section 2.2). There is one diagram per feature (SD-01 to SD-12, matching F01 to F12) plus SD-13, which shows how the CLI reaches the same controllers through the Command pattern.

**Notation.** Solid arrows are calls, dashed arrows are return values. `alt / else` is an alternative flow, `opt` is an optional step, `loop` is repetition, and `break` is an exception flow that ends the scenario. Flow labels such as (3a) refer to the alternative flows in the use-case descriptions.

| Diagram | Feature | Use case | Patterns visible |
|---|---|---|---|
| SD-01 Import Resume | F01 | UC-01 | Strategy, Facade, Factory Method, Adapter |
| SD-02 Import Job Posting | F02 | UC-02 | Strategy |
| SD-03 Analyze Skill Gap and Match | F03 | UC-03 | Facade, Factory Method, Adapter |
| SD-04 Generate Cover Letter | F04 | UC-04 | Facade, Factory Method, Adapter |
| SD-05 Manage Application Pipeline | F05 | UC-05 | State |
| SD-06 Track Deadlines and Receive Alerts | F06 | UC-06 | Observer |
| SD-07 Generate Interview Prep Sheet | F07 | UC-07 | Facade, Factory Method, Adapter |
| SD-08 Optimize Resume Bullet Point | F08 | UC-08 | Facade, Factory Method, Adapter |
| SD-09 View Job Search Analytics | F09 | UC-09 | none (deterministic) |
| SD-10 Export Application Package | F10 | UC-10 | none (deterministic) |
| SD-11 Run Career Agent Assistant | F11 | UC-11 | Facade, Factory Method, Adapter |
| SD-12 Practice Mock Interview | F12 | UC-12 | Facade, Factory Method, Adapter |
| SD-13 Run a Feature from the CLI | all CLI features | UC-01, 02, 03, 04, 05, 07, 08, 09, 10, 11, 12 | Command |

---

## SD-01: Import Resume (F01, UC-01)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant RC as ResumeController
    participant IS as IngestionService
    participant PDF as PDFResumeExtractor
    participant AF as AIEngineFacade
    participant RPF as ResumeParsePromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser
    participant RR as ResumeRepository

    Seeker->>UI: select PDF and click "Upload Resume"
    UI->>RC: uploadResume(file, userId)
    RC->>IS: ingestResume(file, userId)
    IS->>IS: selectStrategy(source)
    IS->>PDF: supports(source)
    PDF-->>IS: true or false

    break no strategy supports the file type (3a)
        IS-->>RC: UnsupportedInputException
        RC-->>UI: error
        UI-->>Seeker: "Invalid File Type - Please upload a PDF"
    end

    IS->>PDF: extract(source)
    break PDF unreadable or text extraction fails (4a)
        PDF-->>IS: ExtractionException
        IS-->>RC: ExtractionException
        RC-->>UI: error
        UI-->>Seeker: ask for another file or manual entry
    end
    PDF-->>IS: ExtractionResult(rawText)

    IS->>AF: parseResume(rawText)
    AF->>RPF: createPrompt(context)
    RPF-->>AF: ResumeParsePrompt
    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)

    break Gemini times out, fails or returns malformed data (5a)
        G-->>GC: error, timeout or invalid JSON
        GC-->>AF: LLMException
        AF-->>IS: AIServiceException (logged)
        IS->>RR: save(Resume with rawText only)
        IS-->>RC: parsing failed, raw text kept
        RC-->>UI: error
        UI-->>Seeker: "Structured parsing failed - retry"
    end

    G-->>GC: structured JSON text
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, schema)
    RV-->>AF: ValidationResult(valid)
    AF->>JP: parseResume(json)
    JP-->>AF: Resume
    AF-->>IS: Resume
    IS->>RR: save(resume)
    RR-->>IS: saved
    IS-->>RC: Resume
    RC-->>UI: Resume
    UI-->>Seeker: display parsed profile for review
```

---

## SD-02: Import Job Posting (F02, UC-02)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant JC as JobController
    participant IS as IngestionService
    participant WS as WebJobScraper
    participant PT as PastedTextJobParser
    participant JB as Job Board Website
    participant JR as JobApplicationRepository

    Seeker->>UI: paste URL or text and click "Import Job"
    UI->>JC: importJob(source, userId)

    break input is empty (2a)
        JC-->>UI: ValidationError
        UI-->>Seeker: "Enter a URL or paste the job text"
    end

    JC->>IS: ingestJob(source, userId)
    IS->>IS: selectStrategy(source)

    alt source is a URL
        IS->>WS: extract(source)
        WS->>JB: HTTP GET(url)
        break page blocked, unreachable or not found (4a)
            JB-->>WS: 403, 404 or timeout
            WS-->>IS: ScrapeException
            IS-->>JC: ScrapeException
            JC-->>UI: error
            UI-->>Seeker: offer "Enter Job Text Manually" (UC-02a)
            Note over Seeker,PT: Seeker pastes the description and the flow restarts at step 2 with a text source
        end
        JB-->>WS: HTML page
        WS-->>IS: ExtractionResult(title, company, description)
    else source is pasted text
        IS->>PT: extract(source)
        PT-->>IS: ExtractionResult(title, company, description)
    end

    break title or description cannot be extracted (5a)
        IS-->>JC: ExtractionException
        JC-->>UI: error
        UI-->>Seeker: ask for the complete job description
    end

    IS->>IS: build JobPosting and JobApplication (WishlistState)
    IS->>JR: save(application)
    JR-->>IS: saved
    IS-->>JC: JobApplication
    JC-->>UI: JobApplication
    UI-->>Seeker: new card in the Wishlist column
```

---

## SD-03: Analyze Skill Gap and Match (F03, UC-03)

This diagram shows the full AI pipeline (controller, facade, prompt factory, Gemini adapter, validation and retry). SD-04, SD-07, SD-08 and SD-12 reuse the same pipeline with their own factory and prompt.

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AIFeatureController
    participant JR as JobApplicationRepository
    participant RR as ResumeRepository
    participant AF as AIEngineFacade
    participant MPF as MatchPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser

    Seeker->>UI: click "Run Match Analysis"
    UI->>AC: analyzeMatch(applicationId)
    AC->>JR: findById(applicationId)
    JR-->>AC: JobApplication (with JobPosting)
    AC->>RR: findByUser(userId)
    RR-->>AC: Resume

    break no resume or no job posting (3a)
        AC-->>UI: error
        UI-->>Seeker: ask to complete the missing step
    end

    AC->>AF: analyzeSkillGap(resume, job)
    AF->>MPF: createPrompt(context)
    MPF-->>AF: MatchAnalysisPrompt
    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)

    break Gemini times out or fails (5a)
        G-->>GC: error or timeout
        GC-->>AF: LLMException
        AF-->>AC: AIServiceException (logged)
        AC-->>UI: error
        UI-->>Seeker: "Please retry later" (nothing saved)
    end

    G-->>GC: JSON text
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, schema)
    RV-->>AF: ValidationResult

    opt response is malformed (6a)
        AF->>GC: generate(prompt) retry once
        GC->>G: generateContent(jsonBody)
        G-->>GC: JSON text
        GC-->>AF: LLMResponse
        AF->>RV: validateLLMOutput(rawText, schema)
        RV-->>AF: ValidationResult
        break still invalid, continue as 5a
            AF-->>AC: AIServiceException
            AC-->>UI: error
            UI-->>Seeker: "Please retry later"
        end
    end

    AF->>JP: parseMatchResult(json)
    JP-->>AF: MatchResult
    AF-->>AC: MatchResult
    AC->>JR: save(application with MatchResult)
    AC-->>UI: MatchResult
    UI-->>Seeker: show score and missing skills on the job card
```

---

## SD-04: Generate Cover Letter (F04, UC-04)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AIFeatureController
    participant JR as JobApplicationRepository
    participant RR as ResumeRepository
    participant AF as AIEngineFacade
    participant CPF as CoverLetterPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser

    Seeker->>UI: click "Generate Cover Letter" (optional tone)
    UI->>AC: generateCoverLetter(applicationId, tone)
    AC->>JR: findById(applicationId)
    JR-->>AC: JobApplication (with JobPosting)
    AC->>RR: findByUser(userId)
    RR-->>AC: Resume

    break no resume uploaded (3a)
        AC-->>UI: error
        UI-->>Seeker: prompt to import a resume first (UC-01)
    end

    AC->>AF: generateCoverLetter(resume, job, tone)
    AF->>CPF: createPrompt(context)
    CPF-->>AF: CoverLetterPrompt
    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)

    break Gemini times out or fails (4a)
        G-->>GC: error or timeout
        GC-->>AF: LLMException
        AF-->>AC: AIServiceException (logged)
        AC-->>UI: error
        UI-->>Seeker: "Service Unavailable - Please Retry" (nothing saved)
    end

    G-->>GC: JSON text
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, schema)
    RV-->>AF: ValidationResult

    opt response is malformed (5a)
        AF->>GC: generate(prompt) retry once
        GC-->>AF: LLMResponse
        AF->>RV: validateLLMOutput(rawText, schema)
        RV-->>AF: ValidationResult
        break still invalid, continue as 4a
            AF-->>AC: AIServiceException
            AC-->>UI: error
            UI-->>Seeker: "Service Unavailable - Please Retry"
        end
    end

    AF->>JP: parseCoverLetter(json)
    JP-->>AF: CoverLetter
    AF-->>AC: CoverLetter
    AC->>JR: save(application with CoverLetter)
    AC-->>UI: CoverLetter
    UI-->>Seeker: show letter in editable view
```

---

## SD-05: Manage Application Pipeline (F05, UC-05)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as ApplicationController
    participant AS as ApplicationService
    participant JR as JobApplicationRepository
    participant JA as JobApplication
    participant ST as ApplicationState

    Seeker->>UI: drag card to another column
    UI->>AC: changeStage(applicationId, newStage)
    AC->>AS: changeStage(applicationId, newStage)
    AS->>JR: findById(applicationId)
    JR-->>AS: JobApplication
    AS->>JA: changeState(target)
    JA->>ST: handleTransition(app, target)
    Note over JA,ST: ST is the current state, for example WishlistState or AppliedState

    alt transition is allowed
        ST-->>JA: new state assigned to the application
        JA-->>AS: done
        AS->>JR: save(application)
        alt database update succeeds
            JR-->>AS: saved
            AS-->>AC: JobApplication
            AC-->>UI: JobApplication
            UI-->>Seeker: board refreshed
        else database update fails (6a)
            JR-->>AS: PersistenceException
            AS-->>AC: error
            AC-->>UI: error
            UI-->>Seeker: card returns to original column, synchronization error shown
        end
    else invalid transition (5a)
        ST-->>JA: InvalidTransitionException
        JA-->>AS: InvalidTransitionException
        AS-->>AC: error
        AC-->>UI: error
        UI-->>Seeker: card returns to original column with a message
    end
```

---

## SD-06: Track Deadlines and Receive Alerts (F06, UC-06)

Part A is user-triggered (setting a deadline). Part B is timer-triggered and uses the Observer pattern.

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as ApplicationController
    participant AS as ApplicationService
    participant JR as JobApplicationRepository
    participant JA as JobApplication
    participant DSch as DeadlineScheduler
    participant DS as DeadlineSubject
    participant DO as DashboardObserver
    participant NO as NotificationAlertObserver
    participant NR as NotificationRepository

    rect rgb(235, 245, 255)
    Note over Seeker,JA: Part A - set a deadline
    Seeker->>UI: choose date and time on a job card
    UI->>UI: validate date
    break invalid date or format (2a)
        UI-->>Seeker: ask for a valid date
    end
    UI->>AC: setDeadline(applicationId, dueAt)
    AC->>AS: setDeadline(applicationId, dueAt)
    AS->>JR: findById(applicationId)
    JR-->>AS: JobApplication
    AS->>JA: setDeadline(dueAt)
    AS->>JR: save(application)
    JR-->>AS: saved
    AS-->>AC: JobApplication
    AC-->>UI: JobApplication
    UI-->>Seeker: deadline shown on the card
    end

    rect rgb(255, 245, 230)
    Note over DSch,NR: Part B - periodic check (Observer pattern)
    loop every intervalMinutes
        DSch->>DSch: runCheck()
        DSch->>DS: checkDeadlines()
        DS->>JR: findDueBefore(now + thresholdHours)
        JR-->>DS: List[JobApplication]
        opt at least one deadline within 48 hours or overdue (5a otherwise no events)
            loop for each application
                DS->>DS: create DeadlineEvent
                DS->>DS: notifyObservers(event)
                DS->>DO: onDeadlineEvent(event)
                DO->>UI: push alert via SSE
                DS->>NO: onDeadlineEvent(event)
                NO->>NR: save(Notification)
            end
        end
    end
    UI-->>Seeker: priority alert on the card and banner on the dashboard
    end

    opt dashboard was closed when the alert fired (6a)
        Seeker->>UI: open dashboard later
        UI->>AC: getUnreadNotifications(userId)
        AC->>AS: getUnreadNotifications(userId)
        AS->>NR: findUnread(userId)
        NR-->>AS: List[Notification]
        AS-->>AC: List[Notification]
        AC-->>UI: List[Notification]
        UI-->>Seeker: alerts shown on load
    end
```

---

## SD-07: Generate Interview Prep Sheet (F07, UC-07)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AIFeatureController
    participant JR as JobApplicationRepository
    participant AF as AIEngineFacade
    participant PPF as PrepSheetPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser

    Seeker->>UI: click "Generate Prep Sheet"
    UI->>AC: generatePrepSheet(applicationId)
    AC->>JR: findById(applicationId)
    JR-->>AC: JobApplication (with JobPosting)
    AC->>AF: generatePrepSheet(job)
    AF->>PPF: createPrompt(context)

    alt job description has enough detail
        PPF-->>AF: InterviewPrompt (5 technical + 3 behavioral)
    else description too short for technical questions (4a)
        PPF-->>AF: InterviewPrompt (standard behavioral questions, fallback)
    end

    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)

    break Gemini times out or fails (4b)
        G-->>GC: error or timeout
        GC-->>AF: LLMException
        AF-->>AC: AIServiceException (logged)
        AC-->>UI: error
        UI-->>Seeker: "Please retry later"
    end

    G-->>GC: JSON text
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, schema)
    RV-->>AF: ValidationResult

    opt response is malformed (5a)
        AF->>GC: generate(prompt) retry once
        GC-->>AF: LLMResponse
        AF->>RV: validateLLMOutput(rawText, schema)
        RV-->>AF: ValidationResult
        break still invalid, continue as 4b
            AF-->>AC: AIServiceException
            AC-->>UI: error
            UI-->>Seeker: "Please retry later"
        end
    end

    AF->>JP: parsePrepSheet(json)
    JP-->>AF: PrepSheet (with InterviewQuestions, usedFallback flag)
    AF-->>AC: PrepSheet
    AC->>JR: save(application with PrepSheet)
    AC-->>UI: PrepSheet
    UI-->>Seeker: show questions in the prep view

    opt PrepSheet.usedFallback is true
        UI-->>Seeker: notify that standard behavioral questions were used
    end
```

---

## SD-08: Optimize Resume Bullet Point (F08, UC-08)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AIFeatureController
    participant JR as JobApplicationRepository
    participant RR as ResumeRepository
    participant AF as AIEngineFacade
    participant BPF as BulletPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser
    participant RC as ResumeController

    Seeker->>UI: highlight a bullet and click "Optimize for this Job"
    UI->>AC: optimizeBullet(bulletId, applicationId)
    AC->>RR: findByUser(userId)
    RR-->>AC: Resume (contains the BulletPoint)
    AC->>JR: findById(applicationId)
    JR-->>AC: JobApplication (with JobPosting)

    break bullet too short or lacks context (3a)
        AC-->>UI: NeedMoreDetail
        UI-->>Seeker: ask for more specific details (no AI call made)
    end

    AC->>AF: optimizeBullet(bullet, job)
    AF->>BPF: createPrompt(context)
    BPF-->>AF: BulletOptimizerPrompt
    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)

    break Gemini times out or fails (4a)
        G-->>GC: error or timeout
        GC-->>AF: LLMException
        AF-->>AC: AIServiceException (logged)
        AC-->>UI: error
        UI-->>Seeker: "Please retry later"
    end

    G-->>GC: JSON text
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, schema)
    RV-->>AF: ValidationResult
    AF->>JP: parseBullets(json)
    JP-->>AF: List[BulletSuggestion] (3 items)
    AF-->>AC: List[BulletSuggestion]
    AC-->>UI: List[BulletSuggestion]
    UI-->>Seeker: show three suggestions

    alt Seeker selects one suggestion
        Seeker->>UI: choose a suggestion
        UI->>RC: replaceBullet(bulletId, suggestedText)
        RC->>RR: save(resume with updated bullet)
        RR-->>RC: saved
        RC-->>UI: Resume
        UI-->>Seeker: bullet text replaced
    else Seeker rejects all suggestions (7a)
        UI-->>Seeker: original bullet kept, nothing saved
    end
```

---

## SD-09: View Job Search Analytics (F09, UC-09)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AN as AnalyticsController
    participant MS as MetricsService
    participant JR as JobApplicationRepository

    Seeker->>UI: open the "Analytics" tab
    UI->>AN: getDashboardMetrics(userId)
    AN->>MS: countByStage(userId)
    MS->>JR: findByUser(userId)

    break database query fails (3a)
        JR-->>MS: PersistenceException
        MS-->>AN: error
        AN-->>UI: error
        UI-->>Seeker: error message, no charts
    end

    JR-->>MS: List[JobApplication]
    MS-->>AN: Map of counts per stage
    AN->>MS: calculateConversionRate(userId)
    MS-->>AN: conversion rate
    AN->>MS: volumeOverTime(userId)
    MS-->>AN: List[DataPoint]
    AN->>AN: assemble DashboardMetrics
    AN-->>UI: DashboardMetrics

    alt totalApplications is 0 (4a)
        UI-->>Seeker: empty placeholder charts and a prompt to add the first job
    else data exists
        UI-->>Seeker: bar graph and conversion funnel
    end
```

---

## SD-10: Export Application Package (F10, UC-10)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant EC as ExportController
    participant JR as JobApplicationRepository
    participant RR as ResumeRepository
    participant DG as DocumentGenerator

    Seeker->>UI: click "Export Application Package"
    UI->>EC: exportPackage(applicationId, "pdf")
    EC->>JR: findById(applicationId)
    JR-->>EC: JobApplication (with CoverLetters)
    EC->>RR: findByUser(userId)
    RR-->>EC: Resume

    break no cover letter exists (3a)
        EC-->>UI: error
        UI-->>Seeker: prompt to generate a cover letter first (UC-04)
    end

    EC->>DG: generatePDF(resume, latest CoverLetter)

    alt PDF generation succeeds
        DG-->>EC: ExportFile (pdf)
        EC-->>UI: ExportFile
        UI-->>Seeker: download starts
    else PDF generation fails (4a, Export Plain Text Copy)
        DG-->>EC: DocumentGenerationException
        EC->>DG: generatePlainText(resume, latest CoverLetter)
        DG-->>EC: ExportFile (text)
        EC-->>UI: ExportFile and fallback notice
        UI-->>Seeker: text copy downloads with an explanation
    end
```

---

## SD-11: Run Career Agent Assistant (F11, UC-11)

This is the central agent diagram: memory recall, planning through the LLM, validated tool execution, replanning on failure, and memory storage.

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AgentController
    participant MM as MemoryManager
    participant MR as MemoryRepository
    participant PL as Planner
    participant TM as ToolManager
    participant AF as AIEngineFacade
    participant PPF as PlanningPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser
    participant PN as Plan
    participant T as Tool (e.g. MatchAnalysisTool)

    Seeker->>UI: "Get me ready for the Shopify job"
    UI->>AC: handleRequest(userId, message)
    AC->>MM: recall(userId, message)
    MM->>MR: findByUser(userId)
    MR-->>MM: List[MemoryEntry]
    MM-->>AC: relevant MemoryEntries

    AC->>PL: createPlan(message, context)
    PL->>TM: getAvailableTools()
    TM-->>PL: List[ToolInfo]
    PL->>AF: planSteps(request, tools)
    AF->>PPF: createPrompt(context)
    PPF-->>AF: PlanningPrompt
    AF->>GC: generate(prompt)
    GC->>G: generateContent(jsonBody)
    G-->>GC: JSON plan
    GC-->>AF: LLMResponse
    AF->>RV: validateLLMOutput(rawText, planSchema)
    RV-->>AF: ValidationResult

    opt plan is invalid (4a)
        PL->>AF: planSteps(request, tools) replan once
        AF-->>PL: second attempt
        break still invalid
            PL-->>AC: planning failed
            AC-->>UI: AgentResponse (could not plan this request)
            UI-->>Seeker: explanation
        end
    end

    AF->>JP: parsePlan(json)
    JP-->>AF: Plan
    AF-->>PL: Plan
    PL-->>AC: Plan

    AC->>PN: needsClarification()
    break request is ambiguous, no job can be identified (2a)
        AC-->>UI: AgentResponse (clarifying question)
        UI-->>Seeker: asks which job
    end

    AC->>AC: executePlan(plan)
    loop until plan.isComplete()
        AC->>PN: nextStep()
        PN-->>AC: PlanStep(toolName, arguments)
        AC->>RV: validateToolArgs(tool, args)
        RV-->>AC: ValidationResult

        opt arguments fail validation (5a)
            AC->>PL: replan(plan, step)
            PL-->>AC: revised Plan
            break arguments still invalid
                AC-->>UI: AgentResponse (asks for the missing information)
                UI-->>Seeker: asks for missing details
            end
        end

        AC->>TM: executeTool(name, args)
        TM->>T: execute(args)
        Note over T,G: the tool calls AIEngineFacade and follows SD-03, SD-04 or SD-07
        T-->>TM: ToolResult
        TM-->>AC: ToolResult

        opt ToolResult.success is false (5b)
            AC->>PL: replan(plan, failedStep)
            PL-->>AC: revised Plan
            break agent cannot recover
                AC->>MM: saveInteraction(userId, entry)
                AC-->>UI: AgentResponse (completed steps, failed steps, no invented results)
                UI-->>Seeker: report of what was done and what failed
            end
        end

        AC->>AC: store ToolResult in PlanStep
    end

    AC->>MM: saveInteraction(userId, entry)
    MM->>MR: save(userId, entry)
    AC-->>UI: AgentResponse (summary of results)
    UI-->>Seeker: display summary
```

---

## SD-12: Practice Mock Interview (F12, UC-12)

```mermaid
sequenceDiagram
    autonumber
    actor Seeker as Job Seeker
    participant UI as KanbanDashboardUI
    participant AC as AIFeatureController
    participant JR as JobApplicationRepository
    participant MS as MockInterviewSession
    participant AF as AIEngineFacade
    participant MPF as MockInterviewPromptFactory
    participant GC as GeminiClient
    participant G as Gemini API
    participant RV as ResponseValidator
    participant JP as JSONResponseParser

    Seeker->>UI: click "Start Mock Interview"
    UI->>AC: startMockInterview(applicationId)
    AC->>JR: findById(applicationId)
    JR-->>AC: JobApplication
    opt application has no PrepSheet yet
        AC->>AF: generatePrepSheet(job)
        AF-->>AC: PrepSheet (see SD-07)
    end
    AC->>MS: create session from PrepSheet questions
    AC->>MS: nextQuestion()
    MS-->>AC: InterviewQuestion
    AC-->>UI: MockInterviewSession and first question
    UI-->>Seeker: show first question

    loop until the questions run out or the Seeker ends the session (8a)
        Seeker->>UI: type an answer
        break answer is empty (4a)
            UI-->>Seeker: ask to answer or skip
        end
        UI->>AC: submitAnswer(sessionId, answer)
        AC->>AF: evaluateAnswer(question, answer)
        AF->>MPF: createPrompt(context)
        MPF-->>AF: MockInterviewPrompt
        AF->>GC: generate(prompt)
        GC->>G: generateContent(jsonBody)

        break Gemini times out or fails (6a)
            G-->>GC: error or timeout
            GC-->>AF: LLMException
            AF-->>AC: AIServiceException (logged)
            Note over AC,MS: the answer stays in the session so feedback can be requested again
            AC-->>UI: error
            UI-->>Seeker: offer to retry the feedback request
        end

        G-->>GC: JSON text
        GC-->>AF: LLMResponse
        AF->>RV: validateLLMOutput(rawText, schema)
        RV-->>AF: ValidationResult
        AF->>JP: parseFeedback(json)
        JP-->>AF: AnswerFeedback
        AF-->>AC: AnswerFeedback
        AC->>MS: recordAnswer(answer, feedback)
        AC->>MS: nextQuestion()
        MS-->>AC: InterviewQuestion or none left
        AC->>JR: save(application with session)
        AC-->>UI: AnswerFeedback and next question
        UI-->>Seeker: show feedback and next question
    end

    Note over Seeker,JR: ending early (8a) needs no extra call because the session is saved after every answer
```

---

## SD-13: Run a Feature from the CLI (Command pattern)

The CLI reaches the same controllers as the GUI. Each CLI request becomes a `Command` object (receiver: a controller) that the `CommandInvoker` executes and records. The example is `GenerateCoverLetterCommand`; the other ten commands (`ImportResumeCommand`, `ImportJobCommand`, `AnalyzeMatchCommand`, `RunAgentCommand`, `GeneratePrepSheetCommand`, `OptimizeBulletCommand`, `MovePipelineCommand`, `ViewAnalyticsCommand`, `ExportPackageCommand`, `StartMockInterviewCommand`) follow the same structure and differ only in their receiver controller.

```mermaid
sequenceDiagram
    autonumber
    actor User as Job Seeker (CLI)
    participant CLI as CLIApp
    participant CI as CommandInvoker
    participant GCC as GenerateCoverLetterCommand
    participant AC as AIFeatureController

    User->>CLI: main(["cover-letter", "--app", "42", "--tone", "formal"])
    CLI->>CLI: parse(args)

    break unknown command or missing arguments
        CLI-->>User: usage message
    end

    CLI->>GCC: create(applicationId, tone)
    CLI->>CI: execute(command)
    CI->>CI: add command to history
    CI->>GCC: execute()
    GCC->>AC: generateCoverLetter(applicationId, tone)
    Note over AC: the controller continues exactly as in SD-04 (facade, prompt factory, Gemini, save)

    alt generation succeeds
        AC-->>GCC: CoverLetter
        GCC-->>CI: CommandResult(success = true, output = letter text)
    else a flow of SD-04 fails (3a, 4a, 5a)
        AC-->>GCC: error
        GCC-->>CI: CommandResult(success = false, output = error message)
    end

    CI-->>CLI: CommandResult
    CLI-->>User: print output
```
