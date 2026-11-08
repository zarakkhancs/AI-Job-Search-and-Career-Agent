package com.careeragent;

import com.careeragent.agent.AgentController;
import com.careeragent.agent.ToolRegistry;
import com.careeragent.alerts.AlertService;
import com.careeragent.analytics.MetricsService;
import com.careeragent.export.ExportService;
import com.careeragent.optimizer.BulletOptimizerService;
import com.careeragent.optimizer.OptimizeBulletTool;
import com.careeragent.prep.PrepSheetService;
import com.careeragent.ingestion.HttpPageFetcher;
import com.careeragent.ingestion.JobIngestionService;
import com.careeragent.resume.PdfTextExtractor;
import com.careeragent.resume.ResumeImportService;
import com.careeragent.prep.PrepSheetTool;
import com.careeragent.prompts.BulletPromptFactory;
import com.careeragent.prompts.PrepSheetPromptFactory;
import com.careeragent.alerts.Deadline;
import com.careeragent.alerts.DeadlineScheduler;
import com.careeragent.config.AppConfig;
import com.careeragent.config.ConfigException;
import com.careeragent.documents.TailorDocumentTool;
import com.careeragent.documents.TailoredDocumentService;
import com.careeragent.extraction.ExtractSkillsTool;
import com.careeragent.extraction.SkillExtractor;
import com.careeragent.interview.MockInterviewService;
import com.careeragent.llm.GeminiClient;
import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import com.careeragent.matching.JobMatcher;
import com.careeragent.matching.KeywordOverlapStrategy;
import com.careeragent.matching.MatchJobTool;
import com.careeragent.pipeline.ApplicationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Builds and holds the shared services used by both the CLI and the HTTP API. */
public class AppContext {
    public final LLMClient llm;
    public final AlertService alerts = new AlertService();
    public final ApplicationService applications = new ApplicationService(alerts);
    public final MetricsService metrics = new MetricsService(applications);
    public final ExportService exports = new ExportService();
    public final JobMatcher matcher = new JobMatcher(new KeywordOverlapStrategy());
    public final SkillExtractor extractor;
    public final TailoredDocumentService documents;
    public final MockInterviewService interviews;
    public final BulletOptimizerService bullets;
    public final PrepSheetService prep;
    public final ResumeImportService resumes;
    public final JobIngestionService jobs = new JobIngestionService(new HttpPageFetcher());
    public final List<Deadline> deadlines = new ArrayList<>();
    public final DeadlineScheduler scheduler = new DeadlineScheduler(alerts, 7);
    public final ToolRegistry tools = new ToolRegistry();
    public final AgentController agent;

    private AppContext(LLMClient llm) {
        this.llm = llm;
        this.extractor = new SkillExtractor(llm);
        this.documents = new TailoredDocumentService(llm, extractor);
        this.interviews = new MockInterviewService(llm, extractor);
        this.bullets = new BulletOptimizerService(llm, new BulletPromptFactory());
        this.prep = new PrepSheetService(llm, new PrepSheetPromptFactory());
        this.resumes = new ResumeImportService(new PdfTextExtractor(), extractor);
        tools.register(new ExtractSkillsTool(extractor));
        tools.register(new MatchJobTool(matcher));
        tools.register(new TailorDocumentTool(documents));
        tools.register(new OptimizeBulletTool(bullets));
        tools.register(new PrepSheetTool(prep));
        this.agent = new AgentController(llm, tools);
    }

    /** Builds a context around the given LLM client (used by tests). */
    public static AppContext withLlm(LLMClient llm) {
        return new AppContext(llm);
    }

    public static AppContext load() throws IOException {
        Path envFile = Files.exists(Path.of(".env")) ? Path.of(".env") : Path.of("..", ".env");
        AppConfig config = AppConfig.load(envFile, System.getenv());
        LLMClient chosen;
        try {
            chosen = new GeminiClient(config.getApiKey(), config.getModel());
        } catch (ConfigException e) {
            String message = e.getMessage();
            chosen = prompt -> {
                throw new LLMException(message);
            };
        }
        return new AppContext(chosen);
    }
}