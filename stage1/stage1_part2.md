## 2.4 Design Patterns Explanations

**1. Strategy Pattern**
*   **Problem addressed:** Extracting data from various input formats (PDF resumes, web URLs, pasted raw text) requires completely different parsing algorithms. 
*   **Participating classes:** `ExtractionStrategy` (Interface), `PDFResumeExtractor`, `WebJobScraper`, `PastedTextJobParser` (Concrete Strategies), and `IngestionService` (Context).
*   **Class roles:** The `ExtractionStrategy` interface defines a common `extract()` contract. The concrete extractors implement the specific file or web parsing logic. The `IngestionService` relies on the interface to execute the extraction without knowing the underlying implementation details.
*   **Appropriateness:** It allows the application to dynamically swap extraction algorithms at runtime based on the specific type of input the user provides.
*   **Alternative difficulties:** The `IngestionService` would become bloated with massive conditional `if/else` blocks for every supported file format or URL type, violating the Open/Closed Principle. 

**2. Factory Method Pattern**
*   **Problem addressed:** The system must generate complex, context-specific prompts for various AI features (cover letters, mock interviews, skill matching) without exposing the prompt construction logic to the standard controllers.
*   **Participating classes:** `PromptFactory` (Creator), `LLMPrompt` (Product Interface), `CoverLetterPromptFactory`, `MatchPromptFactory`, `MockInterviewPromptFactory` (Concrete Creators), and `CoverLetterPrompt`, `MatchAnalysisPrompt` (Concrete Products).
*   **Class roles:** `PromptFactory` dictates the instantiation interface. The concrete factories handle injecting specific user and job data into the correct `LLMPrompt` object structure required by the external LLM.
*   **Appropriateness:** It centralizes the creation of AI prompts, ensuring all payloads adhere to a strict template before network transmission.
*   **Alternative difficulties:** Feature controllers or the Facade would be forced to manually concatenate messy strings of prompt instructions inline, leading to duplicated code, inconsistent AI outputs, and a brittle architecture.

**3. State Pattern**
*   **Problem addressed:** Job applications move through distinct phases (Wishlist, Applied, Interviewing, Offer). Certain actions and transitions are only valid in specific phases, requiring strict validation logic to prevent illegal lifecycle moves.
*   **Participating classes:** `ApplicationState` (State Interface), `WishlistState`, `AppliedState`, `InterviewingState`, `OfferState`, `RejectedState` (Concrete States), and `JobApplication` (Context).
*   **Class roles:** `JobApplication` maintains a reference to its current `ApplicationState`. The concrete state classes encapsulate the specific behaviors and transition validation rules for their respective pipeline phase.
*   **Appropriateness:** It encapsulates state-specific transition logic cleanly, moving rules into dedicated classes rather than bloating the main domain object.
*   **Alternative difficulties:** Tracking and validating application statuses would require extensive, deeply nested `switch` statements scattered across the codebase, making the system highly prone to bugs when adding new pipeline stages.

**4. Facade Pattern**
*   **Problem addressed:** Communicating with the LLM involves complex, low-level mechanics: constructing prompts, managing network clients, checking timeouts, and parsing JSON payloads.
*   **Participating classes:** `AIEngineFacade` (Facade), `PromptFactory`, `LLMClient`, `JSONResponseParser`, `ResponseValidator` (Subsystem Classes).
*   **Class roles:** The `AIEngineFacade` provides simplified, high-level entry points (e.g., `generateCoverLetter()`). It coordinates the subsystem classes to execute the network call and parse the response, completely hiding the underlying complexity.
*   **Appropriateness:** It shields the rest of the application (like standard UI controllers or CLI commands) from the intricate mechanics of LLM integration.
*   **Alternative difficulties:** Every standard controller utilizing AI features would need to independently orchestrate network connections, error handling, and JSON parsing, drastically increasing code duplication and tight coupling.

**5. Observer Pattern**
*   **Problem addressed:** When application deadlines approach, the GUI dashboard and notification alert systems must update immediately without the frontend constantly querying the database.
*   **Participating classes:** `DeadlineSubject` (Publisher/Subject), `DeadlineObserver` (Subscriber Interface), `DashboardObserver`, `NotificationAlertObserver` (Concrete Observers).
*   **Class roles:** `DeadlineSubject` tracks deadlines (triggered periodically by a scheduler). When a 48-hour threshold is crossed, it notifies all registered `DeadlineObserver` implementations. The concrete observers then push alerts to the UI or save them to the database.
*   **Appropriateness:** It establishes a reactive, one-to-many, event-driven architecture that keeps frontend components synchronized with backend state changes in real-time.
*   **Alternative difficulties:** The GUI would be forced to implement aggressive, continuous polling to the database to check for approaching deadlines, severely degrading system and database performance.

**6. Command Pattern**
*   **Problem addressed:** Processing arbitrary text-based CLI inputs requires decoupling the user's terminal request from the backend execution logic, allowing for organized execution, logging, and history tracking.
*   **Participating classes:** `CommandInvoker` (Invoker), `Command` (Interface), `GenerateCoverLetterCommand`, `RunAgentCommand`, `ImportResumeCommand` (Concrete Commands).
*   **Class roles:** The CLI application parses arguments and creates a specific `Command` object encapsulating the request. The `CommandInvoker` triggers the `execute()` method, which delegates the work to the actual backend REST controllers (the Receivers).
*   **Appropriateness:** It standardizes how distinct requests are encapsulated as objects, cleanly separating the invoker of the request from the object that performs the actual work.
*   **Alternative difficulties:** The main CLI application loop would require massive, procedural routing functions (`if/else` blocks) directly invoking backend controller methods, breaking the separation of concerns.

**7. Adapter Pattern**
*   **Problem addressed:** The system needs to communicate with external, third-party LLMs. Hardcoding the proprietary API structure (e.g., Google Gemini) into the core system prevents easily switching to other AI providers in the future.
*   **Participating classes:** `LLMClient` (Target Interface), `GeminiClient` (Adapter), `GeminiRestAPI` (Adaptee).
*   **Class roles:** `GeminiClient` implements the internal `LLMClient` target interface while holding an instance of the external `GeminiRestAPI`. It translates standard internal prompt requests into the specific JSON payload required by Gemini.
*   **Appropriateness:** It completely decouples the application's core AI Engine Facade from the proprietary mechanics and data structures of a specific external vendor.
*   **Alternative difficulties:** Swapping out Gemini for another LLM provider (like OpenAI or Claude) would require tearing apart and rewriting the entire `AIEngineFacade` and all parsing logic, rather than simply plugging in a new Adapter class.
