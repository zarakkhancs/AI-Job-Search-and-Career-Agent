package com.careeragent.optimizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import com.careeragent.prompts.BulletPromptFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

class BulletOptimizerServiceTest {
    private static BulletOptimizerService service(MockLLMClient llm) {
        return new BulletOptimizerService(llm, new BulletPromptFactory());
    }

    @Test
    void returnsVersionsAndFlagsInventedFigures() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"versions\":[\"Engineered a REST API for the team\","
                + "\"Built a REST API that cut latency by 40%\",\"Delivered 3 endpoints\"]}");
        BulletResult result = service(llm).optimize("Built a REST API for 3 endpoints", null);
        assertEquals(3, result.versions().size());
        assertTrue(result.versions().get(0).unsupportedFigures().isEmpty());
        assertEquals(List.of("40%"), result.versions().get(1).unsupportedFigures());
        assertTrue(result.versions().get(2).unsupportedFigures().isEmpty());
    }

    @Test
    void percentSignDoesNotCauseFalseFlag() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"versions\":[\"Improved load time by 40%\"]}");
        BulletResult result = service(llm).optimize("Improved load time by 40 percent", null);
        assertTrue(result.versions().get(0).unsupportedFigures().isEmpty());
    }

    @Test
    void keepsAtMostThreeVersions() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"versions\":[\"a\",\"b\",\"c\",\"d\"]}");
        assertEquals(3, service(llm).optimize("Wrote code", null).versions().size());
    }

    @Test
    void badModelOutputBecomesOptimizerException() {
        assertThrows(OptimizerException.class, () -> service(new MockLLMClient("no json here")).optimize("Wrote code", null));
        assertThrows(OptimizerException.class, () -> service(new MockLLMClient("{\"other\":1}")).optimize("Wrote code", null));
        assertThrows(OptimizerException.class, () -> service(new MockLLMClient("{\"versions\":[\"\"]}")).optimize("Wrote code", null));
    }

    @Test
    void blankBulletIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service(new MockLLMClient("{}")).optimize("  ", null));
    }

    @Test
    void jobIsSentToTheModelWhenGiven() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"versions\":[\"Built an API\"]}");
        service(llm).optimize("Made an API", "Java developer wanted");
        assertTrue(llm.getPrompts().get(0).contains("Java developer wanted"));
    }
}