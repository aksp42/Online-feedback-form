package com.feedback;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests: starts the real web server on a free port,
 * then opens the form and submits it like a browser would.
 */
class FeedbackAppTest {

    private static FeedbackApp app;
    private static String baseUrl;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws Exception {
        app = new FeedbackApp(0); // 0 = pick any free port, avoids clashes with 8080
        app.start();
        baseUrl = "http://localhost:" + app.getPort();
    }

    @AfterAll
    static void stopServer() {
        app.stop();
    }

    @Test
    void homePageShowsFeedbackForm() throws Exception {
        HttpResponse<String> res = get("/");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Online Feedback Form"));
        assertTrue(res.body().contains("name=\"email\""));
        assertTrue(res.body().contains("name=\"category\""));
        assertTrue(res.body().contains("name=\"rating\""));
        assertTrue(res.body().contains("name=\"comments\""));
    }

    @Test
    void stylesheetIsServed() throws Exception {
        assertEquals(200, get("/style.css").statusCode());
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        HttpResponse<String> res = get("/health");
        assertEquals(200, res.statusCode());
        assertEquals("OK", res.body());
    }

    @Test
    void validSubmissionShowsSuccessPage() throws Exception {
        HttpResponse<String> res = post(
                "name=Asha&email=asha%40example.com&category=Teaching&rating=5&comments=Very+good+course");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("submitted successfully"));
        assertTrue(res.body().contains("Thank you, Asha!"));
    }

    @Test
    void invalidSubmissionShowsErrorPage() throws Exception {
        HttpResponse<String> res = post("name=A&email=bad&category=Teaching&rating=5&comments=hi");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Submission failed"));
    }

    @Test
    void unknownPageReturns404() throws Exception {
        assertEquals(404, get("/does-not-exist").statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String formBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/submit"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
