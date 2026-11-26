package com.feedback;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for the feedback form: parsing, validation and building the
 * result pages. Kept separate from the web server so it is easy to unit test.
 */
public class FeedbackService {

    public static final List<String> CATEGORIES =
            List.of("Course Content", "Teaching", "Infrastructure", "Website", "Other");

    private FeedbackService() {
    }

    /** Converts "name=Asha&email=a%40b.com" into a map. */
    public static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return map;
        }
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            map.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return map;
    }

    /** Builds a Feedback object from the submitted form fields. */
    public static Feedback fromForm(Map<String, String> form) {
        int rating;
        try {
            rating = Integer.parseInt(form.getOrDefault("rating", "").trim());
        } catch (NumberFormatException e) {
            rating = 0;
        }
        return new Feedback(
                form.getOrDefault("name", "").trim(),
                form.getOrDefault("email", "").trim(),
                form.getOrDefault("category", "").trim(),
                rating,
                form.getOrDefault("comments", "").trim());
    }

    /** Server-side validation. Returns an error message, or null when everything is valid. */
    public static String validate(Feedback f) {
        if (f.name().length() < 2) {
            return "Name must contain at least 2 characters.";
        }
        if (!f.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Please enter a valid email address.";
        }
        if (!CATEGORIES.contains(f.category())) {
            return "Please select a valid feedback category.";
        }
        if (f.rating() < 1 || f.rating() > 5) {
            return "Rating must be between 1 and 5.";
        }
        if (f.comments().length() < 5) {
            return "Comments must contain at least 5 characters.";
        }
        if (f.comments().length() > 500) {
            return "Comments must not exceed 500 characters.";
        }
        return null;
    }

    /** Escapes user input before putting it into HTML (prevents HTML/script injection). */
    public static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String stars(int rating) {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    public static String successPage(Feedback f) {
        return page("Feedback Submitted",
                "<div class=\"icon success\">&#10004;</div>"
                + "<h1>Thank you, " + escapeHtml(f.name()) + "!</h1>"
                + "<p class=\"subtitle\">Your feedback has been submitted successfully.</p>"
                + "<table class=\"summary\">"
                + row("Name", escapeHtml(f.name()))
                + row("Email", escapeHtml(f.email()))
                + row("Category", escapeHtml(f.category()))
                + row("Rating", "<span class=\"stars\">" + stars(f.rating()) + "</span> (" + f.rating() + "/5)")
                + row("Comments", escapeHtml(f.comments()))
                + "</table>"
                + "<a class=\"btn\" href=\"/\">Submit another feedback</a>");
    }

    public static String errorPage(String message) {
        return page("Submission Error",
                "<div class=\"icon error\">&#10006;</div>"
                + "<h1>Submission failed</h1>"
                + "<p class=\"subtitle\">" + escapeHtml(message) + "</p>"
                + "<a class=\"btn\" href=\"/\">Go back to the form</a>");
    }

    private static String row(String label, String value) {
        return "<tr><th>" + label + "</th><td>" + value + "</td></tr>";
    }

    private static String page(String title, String content) {
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + title + " | Online Feedback Form</title>"
                + "<link rel=\"stylesheet\" href=\"/style.css\"></head>"
                + "<body><main class=\"card result\">" + content + "</main>"
                + "<footer>Online Feedback Form &middot; Java + Maven DevOps Practical</footer>"
                + "</body></html>";
    }
}
