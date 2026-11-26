package com.feedback;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for parsing, validation and page building. */
class FeedbackServiceTest {

    private Feedback valid() {
        return new Feedback("Asha", "asha@example.com", "Teaching", 5, "Great classes!");
    }

    @Test
    void parseFormDecodesValues() {
        Map<String, String> form = FeedbackService.parseForm(
                "name=Asha+Sharma&email=asha%40example.com&rating=4");
        assertEquals("Asha Sharma", form.get("name"));
        assertEquals("asha@example.com", form.get("email"));
        assertEquals("4", form.get("rating"));
    }

    @Test
    void validFeedbackPassesValidation() {
        assertNull(FeedbackService.validate(valid()));
    }

    @Test
    void invalidEmailIsRejected() {
        Feedback f = new Feedback("Asha", "not-an-email", "Teaching", 5, "Great classes!");
        assertNotNull(FeedbackService.validate(f));
    }

    @Test
    void ratingOutOfRangeIsRejected() {
        Feedback f = new Feedback("Asha", "asha@example.com", "Teaching", 9, "Great classes!");
        assertNotNull(FeedbackService.validate(f));
    }

    @Test
    void unknownCategoryIsRejected() {
        Feedback f = new Feedback("Asha", "asha@example.com", "Food", 4, "Great classes!");
        assertNotNull(FeedbackService.validate(f));
    }

    @Test
    void missingRatingBecomesZeroAndIsRejected() {
        Feedback f = FeedbackService.fromForm(Map.of("name", "Asha"));
        assertEquals(0, f.rating());
        assertNotNull(FeedbackService.validate(f));
    }

    @Test
    void successPageShowsSubmittedData() {
        String html = FeedbackService.successPage(valid());
        assertTrue(html.contains("Thank you, Asha!"));
        assertTrue(html.contains("asha@example.com"));
        assertTrue(html.contains("Teaching"));
    }

    @Test
    void htmlInInputIsEscaped() {
        Feedback f = new Feedback("<script>x</script>", "a@b.com", "Other", 3, "hello there");
        String html = FeedbackService.successPage(f);
        assertFalse(html.contains("<script>x</script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}
