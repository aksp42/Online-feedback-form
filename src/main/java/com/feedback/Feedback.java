package com.feedback;

/**
 * One feedback submission. Kept only in memory (not stored permanently).
 */
public record Feedback(String name, String email, String category, int rating, String comments) {

    @Override
    public String toString() {
        return "Feedback{name='" + name + "', email='" + email + "', category='" + category
                + "', rating=" + rating + ", comments='" + comments + "'}";
    }
}
