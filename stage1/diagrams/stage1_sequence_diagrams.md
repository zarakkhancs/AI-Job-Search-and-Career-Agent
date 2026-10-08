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

![sequence_SD-01_import_resume](umlet/png/sequence_SD-01_import_resume.png)

*UMLet source: [`sequence_SD-01_import_resume.uxf`](umlet/sequence_SD-01_import_resume.uxf)*

---

## SD-02: Import Job Posting (F02, UC-02)

![sequence_SD-02_import_job_posting](umlet/png/sequence_SD-02_import_job_posting.png)

*UMLet source: [`sequence_SD-02_import_job_posting.uxf`](umlet/sequence_SD-02_import_job_posting.uxf)*

---

## SD-03: Analyze Skill Gap and Match (F03, UC-03)

This diagram shows the full AI pipeline (controller, facade, prompt factory, Gemini adapter, validation and retry). SD-04, SD-07, SD-08 and SD-12 reuse the same pipeline with their own factory and prompt.

![sequence_SD-03_analyze_skill_gap_and_match](umlet/png/sequence_SD-03_analyze_skill_gap_and_match.png)

*UMLet source: [`sequence_SD-03_analyze_skill_gap_and_match.uxf`](umlet/sequence_SD-03_analyze_skill_gap_and_match.uxf)*

---

## SD-04: Generate Cover Letter (F04, UC-04)

![sequence_SD-04_generate_cover_letter](umlet/png/sequence_SD-04_generate_cover_letter.png)

*UMLet source: [`sequence_SD-04_generate_cover_letter.uxf`](umlet/sequence_SD-04_generate_cover_letter.uxf)*

---

## SD-05: Manage Application Pipeline (F05, UC-05)

![sequence_SD-05_manage_application_pipeline](umlet/png/sequence_SD-05_manage_application_pipeline.png)

*UMLet source: [`sequence_SD-05_manage_application_pipeline.uxf`](umlet/sequence_SD-05_manage_application_pipeline.uxf)*

---

## SD-06: Track Deadlines and Receive Alerts (F06, UC-06)

Part A is user-triggered (setting a deadline). Part B is timer-triggered and uses the Observer pattern.

![sequence_SD-06_track_deadlines_and_receive_alerts](umlet/png/sequence_SD-06_track_deadlines_and_receive_alerts.png)

*UMLet source: [`sequence_SD-06_track_deadlines_and_receive_alerts.uxf`](umlet/sequence_SD-06_track_deadlines_and_receive_alerts.uxf)*

---

## SD-07: Generate Interview Prep Sheet (F07, UC-07)

![sequence_SD-07_generate_interview_prep_sheet](umlet/png/sequence_SD-07_generate_interview_prep_sheet.png)

*UMLet source: [`sequence_SD-07_generate_interview_prep_sheet.uxf`](umlet/sequence_SD-07_generate_interview_prep_sheet.uxf)*

---

## SD-08: Optimize Resume Bullet Point (F08, UC-08)

![sequence_SD-08_optimize_resume_bullet_point](umlet/png/sequence_SD-08_optimize_resume_bullet_point.png)

*UMLet source: [`sequence_SD-08_optimize_resume_bullet_point.uxf`](umlet/sequence_SD-08_optimize_resume_bullet_point.uxf)*

---

## SD-09: View Job Search Analytics (F09, UC-09)

![sequence_SD-09_view_job_search_analytics](umlet/png/sequence_SD-09_view_job_search_analytics.png)

*UMLet source: [`sequence_SD-09_view_job_search_analytics.uxf`](umlet/sequence_SD-09_view_job_search_analytics.uxf)*

---

## SD-10: Export Application Package (F10, UC-10)

![sequence_SD-10_export_application_package](umlet/png/sequence_SD-10_export_application_package.png)

*UMLet source: [`sequence_SD-10_export_application_package.uxf`](umlet/sequence_SD-10_export_application_package.uxf)*

---

## SD-11: Run Career Agent Assistant (F11, UC-11)

This is the central agent diagram: memory recall, planning through the LLM, validated tool execution, replanning on failure, and memory storage.

![sequence_SD-11_run_career_agent_assistant](umlet/png/sequence_SD-11_run_career_agent_assistant.png)

*UMLet source: [`sequence_SD-11_run_career_agent_assistant.uxf`](umlet/sequence_SD-11_run_career_agent_assistant.uxf)*

---

## SD-12: Practice Mock Interview (F12, UC-12)

![sequence_SD-12_practice_mock_interview](umlet/png/sequence_SD-12_practice_mock_interview.png)

*UMLet source: [`sequence_SD-12_practice_mock_interview.uxf`](umlet/sequence_SD-12_practice_mock_interview.uxf)*

---

## SD-13: Run a Feature from the CLI (Command pattern)

The CLI reaches the same controllers as the GUI. Each CLI request becomes a `Command` object (receiver: a controller) that the `CommandInvoker` executes and records. The example is `GenerateCoverLetterCommand`; the other ten commands (`ImportResumeCommand`, `ImportJobCommand`, `AnalyzeMatchCommand`, `RunAgentCommand`, `GeneratePrepSheetCommand`, `OptimizeBulletCommand`, `MovePipelineCommand`, `ViewAnalyticsCommand`, `ExportPackageCommand`, `StartMockInterviewCommand`) follow the same structure and differ only in their receiver controller.

![sequence_SD-13_run_a_feature_from_the_cli](umlet/png/sequence_SD-13_run_a_feature_from_the_cli.png)

*UMLet source: [`sequence_SD-13_run_a_feature_from_the_cli.uxf`](umlet/sequence_SD-13_run_a_feature_from_the_cli.uxf)*
