package com.feedback;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Online Feedback Form - main application.
 *
 * Uses the lightweight HTTP server that is built into Java, so no framework
 * or external server is needed.
 *
 *   GET  /            -> shows the feedback form (index.html)
 *   GET  /style.css   -> stylesheet
 *   POST /submit      -> processes the form and shows a success page
 *   GET  /health      -> returns "OK" (useful for checking the app/container is up)
 */
public class FeedbackApp {

    // 9090 is used (not 8080) because Jenkins uses 8080 by default
    public static final int DEFAULT_PORT = 9090;

    private final HttpServer server;

    public FeedbackApp(int port) throws IOException {
        // 0.0.0.0 = listen on all network interfaces (needed inside Docker)
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", this::handleStatic);
        server.createContext("/submit", this::handleSubmit);
        server.createContext("/health", ex -> send(ex, 200, "text/plain", "OK"));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    /** Actual port in use (useful when port 0 = "any free port" is used in tests). */
    public int getPort() {
        return server.getAddress().getPort();
    }

    // ---------------------------------------------------------------- handlers

    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            sendResource(ex, "static/index.html", "text/html; charset=UTF-8");
        } else if (path.equals("/style.css")) {
            sendResource(ex, "static/style.css", "text/css; charset=UTF-8");
        } else {
            send(ex, 404, "text/html; charset=UTF-8",
                    "<h1>404 - Page not found</h1><a href=\"/\">Go to feedback form</a>");
        }
    }

    private void handleSubmit(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            // Someone opened /submit directly in the browser - send them to the form
            ex.getResponseHeaders().add("Location", "/");
            ex.sendResponseHeaders(303, -1);
            ex.close();
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = FeedbackService.parseForm(body);
        Feedback feedback = FeedbackService.fromForm(form);
        String error = FeedbackService.validate(feedback);

        if (error != null) {
            send(ex, 400, "text/html; charset=UTF-8", FeedbackService.errorPage(error));
            return;
        }

        System.out.println("Feedback received: " + feedback);
        send(ex, 200, "text/html; charset=UTF-8", FeedbackService.successPage(feedback));
    }

    // ----------------------------------------------------------------- helpers

    private void sendResource(HttpExchange ex, String resource, String contentType) throws IOException {
        try (InputStream in = FeedbackApp.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                send(ex, 404, "text/plain", "Resource not found: " + resource);
                return;
            }
            sendBytes(ex, 200, contentType, in.readAllBytes());
        }
    }

    private static void send(HttpExchange ex, int status, String contentType, String text) throws IOException {
        sendBytes(ex, status, contentType, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(HttpExchange ex, int status, String contentType, byte[] bytes) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    // -------------------------------------------------------------------- main

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            port = Integer.parseInt(envPort.trim());
        }

        FeedbackApp app = new FeedbackApp(port);
        app.start();
        System.out.println("==============================================");
        System.out.println(" Online Feedback Form is running");
        System.out.println(" Open in browser: http://localhost:" + port);
        System.out.println(" Press Ctrl + C to stop");
        System.out.println("==============================================");
    }
}
