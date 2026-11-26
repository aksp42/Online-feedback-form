# Online Feedback Form – Java Maven DevOps Practical

A simple feedback form web application built with **Java 17, HTML and CSS**, used to demonstrate a basic
DevOps workflow: **Maven → Git/GitHub → Jenkins → GitHub Actions → Docker**.

The app shows a feedback form (Name, Email, Feedback Category, Rating, Comments), the Java backend
validates the submission, and a success page shows what was submitted. Nothing is stored permanently
(no database).

> Step-by-step exam instructions are in **[PRACTICAL-GUIDE.md](PRACTICAL-GUIDE.md)**.

---

## Technology Stack

| Area | Tool |
|------|------|
| Language | Java 17 |
| Web server | Java's built-in HTTP server (`com.sun.net.httpserver`) – no framework needed |
| Frontend | HTML + CSS (with HTML5 form validation) |
| Build tool | Apache Maven |
| Tests | JUnit 5 (14 tests) |
| Version control | Git + GitHub (feature branch + Pull Request) |
| CI server | Jenkins (Declarative Pipeline – `Jenkinsfile`) |
| Cloud CI | GitHub Actions (`.github/workflows/maven-ci.yml`) |
| Container | Docker (`Dockerfile`) |

**Application port: `9090`** → open `http://localhost:9090`
(8080 is not used because Jenkins runs on 8080.)

---

## Project Structure

```
online-feedback-form/
├── .github/
│   └── workflows/
│       └── maven-ci.yml          GitHub Actions workflow (runs on every push)
├── src/
│   ├── main/
│   │   ├── java/com/feedback/
│   │   │   ├── FeedbackApp.java      Main class – starts the web server, handles requests
│   │   │   ├── FeedbackService.java  Form parsing, validation, success/error pages
│   │   │   └── Feedback.java         Data record for one feedback submission
│   │   └── resources/static/
│   │       ├── index.html            The feedback form
│   │       └── style.css             Styling (responsive)
│   └── test/java/com/feedback/
│       ├── FeedbackServiceTest.java  Unit tests (parsing, validation, HTML escaping)
│       └── FeedbackAppTest.java      Integration tests (starts server, loads form, submits it)
├── .dockerignore                 Files Docker should not copy
├── .gitignore                    Files Git should not track (e.g. target/)
├── Dockerfile                    Builds the Docker image from the JAR
├── Jenkinsfile                   Jenkins pipeline (Checkout → Build & Test → Archive)
├── pom.xml                       Maven configuration
├── PRACTICAL-GUIDE.md            Step-by-step practical execution guide
└── README.md                     This file
```

### Important files explained

| File | What it does |
|------|--------------|
| `pom.xml` | Tells Maven the project name, Java version (17), the JUnit test dependency, and makes the JAR runnable. Output: `target/online-feedback-form.jar`. |
| `FeedbackApp.java` | Entry point (`main`). Starts the server on port 9090 (or the `PORT` environment variable). Routes: `GET /` form, `GET /style.css`, `POST /submit`, `GET /health`. |
| `FeedbackService.java` | Reads the submitted form, validates it on the server (name, email format, category, rating 1–5, comments 5–500 chars) and builds the success or error page. Escapes user input to prevent HTML injection. |
| `Feedback.java` | A Java `record` holding one submission. |
| `index.html` / `style.css` | The form UI. Fields use `required`, `type="email"`, `minlength`, `maxlength` for browser-side validation. |
| `FeedbackServiceTest.java` | 8 unit tests for the business logic. |
| `FeedbackAppTest.java` | 6 integration tests: starts the real server on a free port and sends HTTP requests. |
| `Jenkinsfile` | Jenkins pipeline. Uses the Jenkins Maven tool named `Maven`, runs `mvn -B clean package`, archives the JAR and publishes test results. Works on Windows (`bat`) and Linux (`sh`). |
| `maven-ci.yml` | GitHub Actions: on every push (and PR to `main`) it sets up Java 17, runs `mvn -B clean package` and uploads the JAR. |
| `Dockerfile` | Uses the `eclipse-temurin:17-jre` image, copies the JAR, exposes port 9090 and runs it. Requires `mvn package` to be run first. |

---

## Quick Start (Windows CMD)

```cmd
cd online-feedback-form
mvn package
java -jar target\online-feedback-form.jar
```

Open **http://localhost:9090** in a browser. Press `Ctrl + C` in CMD to stop.

### Docker

```cmd
mvn package
docker build -t online-feedback-form .
docker run -d -p 9090:9090 --name feedback-app online-feedback-form
```

Open **http://localhost:9090**. Stop and remove with:

```cmd
docker stop feedback-app
docker rm feedback-app
```

---

## DevOps Workflow

```
 Developer (VS Code)
      │  edit code on a feature branch
      ▼
 mvn package  ──► compile + 14 tests + target/online-feedback-form.jar
      │
      ▼
 git push (feature branch) ──► GitHub ──► GitHub Actions runs Maven build & tests
      │
      ▼
 Pull Request ──► review ──► Merge into main
      │
      ▼
 Jenkins pipeline (Build Now) ──► Checkout → Build & Test → Archive JAR
      │
      ▼
 docker build ──► docker run ──► http://localhost:9090
```

### Git branching workflow used

1. `main` – stable code.
2. `feature/update-feedback-form` – a feature branch where a change is made.
3. The feature branch is pushed to GitHub → GitHub Actions checks it.
4. A **Pull Request** is opened from the feature branch into `main` and merged.
5. Local `main` is updated with `git pull`.

Full commands are in [PRACTICAL-GUIDE.md](PRACTICAL-GUIDE.md).

---

## Application Routes

| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/` | Feedback form |
| GET | `/style.css` | Stylesheet |
| POST | `/submit` | Processes the form → success page (200) or error page (400) |
| GET | `/health` | Returns `OK` – quick check that the app is running |

---

## Limitations (by design)

- Feedback is **not stored** – it is only shown on the success page and printed in the console/log.
- No login, no database, no cloud deployment – kept simple for a lab practical.
