package com.careeragent.prep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import com.careeragent.prompts.PrepSheetPromptFactory;
import org.junit.jupiter.api.Test;

class PrepSheetServiceTest {
    private static PrepSheetService service(MockLLMClient llm) {
        return new PrepSheetService(llm, new PrepSheetPromptFactory());
    }

    @Test
    void capsQuestionsAtFiveAndThree() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"technical\":[\"t1\",\"t2\",\"t3\",\"t4\",\"t5\",\"t6\"],"
                + "\"behavioral\":[\"b1\",\"b2\",\"b3\",\"b4\"]}");
        PrepSheet sheet = service(llm).generate("Backend job", null);
        assertEquals(5, sheet.technical().size());
        assertEquals(3, sheet.behavioral().size());
    }

    @Test
    void acceptsQuestionsGivenAsObjects() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"technical\":[{\"question\":\"What is a JOIN?\"}],"
                + "\"behavioral\":[{\"text\":\"Tell me about a conflict.\"}]}");
        PrepSheet sheet = service(llm).generate("Backend job", null);
        assertEquals("What is a JOIN?", sheet.technical().get(0));
        assertEquals("Tell me about a conflict.", sheet.behavioral().get(0));
    }

    @Test
    void missingSectionBecomesPrepException() {
        MockLLMClient llm = new MockLLMClient("{\"technical\":[\"t1\"]}");
        assertThrows(PrepException.class, () -> service(llm).generate("Backend job", null));
    }

    @Test
    void nonJsonBecomesPrepException() {
        assertThrows(PrepException.class, () -> service(new MockLLMClient("sorry")).generate("Backend job", null));
    }

    @Test
    void blankJobIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service(new MockLLMClient("{}")).generate(" ", null));
    }

    @Test
    void resumeIsSentToTheModelWhenGiven() throws Exception {
        MockLLMClient llm = new MockLLMClient("{\"technical\":[\"t\"],\"behavioral\":[\"b\"]}");
        service(llm).generate("Backend job", "My resume mentions Rust");
        assertTrue(llm.getPrompts().get(0).contains("My resume mentions Rust"));
    }
}