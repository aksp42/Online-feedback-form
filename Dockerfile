# Dockerfile for the Online Feedback Form
#
# IMPORTANT: run "mvn package" first, so that target\online-feedback-form.jar exists.
#
# Build image:   docker build -t online-feedback-form .
# Run container: docker run -d -p 9090:9090 --name feedback-app online-feedback-form
# Open browser:  http://localhost:9090

# Small official image that contains only the Java 17 runtime
FROM eclipse-temurin:17-jre

# Folder inside the container where the app lives
WORKDIR /app

# Copy the JAR built by Maven into the image
COPY target/online-feedback-form.jar app.jar

# The application listens on port 9090
EXPOSE 9090

# Command that starts the application when the container starts
CMD ["java", "-jar", "app.jar"]
