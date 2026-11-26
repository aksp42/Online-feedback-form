# PRACTICAL GUIDE – Online Feedback Form (Java Maven DevOps)

Complete step-by-step sequence for the lab practical on **Windows** using **Command Prompt (CMD)** and **VS Code**.

| Part | Topic |
|------|-------|
| 0 | Prerequisites and setup check |
| A | Run the application locally |
| B | Maven build (`mvn package`) |
| C | Git init, GitHub push, feature branch |
| D | Pull Request and merge |
| E | Jenkins pipeline |
| F | GitHub Actions CI |
| G | Docker image build |
| H | Docker container run and browser check |

**Key values used everywhere in this guide**

| Item | Value |
|------|-------|
| Project folder | `online-feedback-form` |
| Build artifact | `target\online-feedback-form.jar` |
| Application URL | `http://localhost:9090` |
| Jenkins URL | `http://localhost:8080` |
| Docker image name | `online-feedback-form` |
| Docker container name | `feedback-app` |
| Feature branch | `feature/update-feedback-form` |
| GitHub repo name | `online-feedback-form` |

> In commands, replace `YOUR-USERNAME` with your GitHub username.
> 📸 marks a **screenshot checkpoint** for your practical file.

---

## 0. Prerequisites

Install these (all free):

| Software | Version | Needed for | Download |
|----------|---------|-----------|----------|
| JDK | **17** (Temurin recommended) | Running and compiling Java | https://adoptium.net |
| Apache Maven | 3.9.x | `mvn package` | https://maven.apache.org/download.cgi |
| Git for Windows | latest | Git commands, Jenkins checkout | https://git-scm.com |
| VS Code | latest | Opening/editing the project | https://code.visualstudio.com |
| GitHub account | – | Repo, Pull Request, Actions | https://github.com |
| Jenkins LTS (Windows installer) | latest LTS | Part E | https://www.jenkins.io/download |
| Docker Desktop | latest | Parts G, H | https://www.docker.com/products/docker-desktop |

Also needed: a working **internet connection** (Maven downloads its plugins the first time; Docker downloads the Java image).

### 0.1 Environment variables (only if commands below are "not recognized")

1. Press **Windows key** → type **"environment variables"** → open **"Edit the system environment variables"** → **Environment Variables...**
2. Under **System variables**:
   - New → Name `JAVA_HOME` → Value: your JDK folder, e.g. `C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot`
   - Edit **Path** → New → `%JAVA_HOME%\bin`
   - Edit **Path** → New → your Maven bin folder, e.g. `C:\apache-maven-3.9.11\bin`
3. Click OK on all windows. **Close and reopen CMD.**

### 0.2 Check everything (open a new CMD)

```cmd
java -version
mvn -version
git --version
docker --version
```

**Expected:**
- `java -version` → `openjdk version "17.0.x"` (17 or newer)
- `mvn -version` → `Apache Maven 3.9.x` and `Java version: 17.x`
- `git --version` → `git version 2.x`
- `docker --version` → `Docker version 2x.x.x`

📸 **Screenshot 1:** CMD showing all four versions.

---

## Opening the project

1. Extract `online-feedback-form.zip`, e.g. to `C:\DevOps\` → you get `C:\DevOps\online-feedback-form`.
   (Make sure the folder **directly** contains `pom.xml`, not another nested `online-feedback-form` folder.)
2. Open **VS Code** → **File → Open Folder...** → select `C:\DevOps\online-feedback-form`.
3. Open the terminal in VS Code: **Terminal → New Terminal**.
4. Make the terminal CMD: click the **˅** arrow next to **+** in the terminal panel → **Command Prompt**.
   (Or use the normal Windows CMD and run `cd /d C:\DevOps\online-feedback-form`.)

All commands below are run in **CMD inside the project folder** `C:\DevOps\online-feedback-form` unless stated otherwise.

📸 **Screenshot 2:** VS Code Explorer showing the project structure.

---

## A. Run the Application Locally

The app runs from the JAR, so build it first (this is also Part B):

```cmd
mvn package
java -jar target\online-feedback-form.jar
```

**Expected output:**
```
==============================================
 Online Feedback Form is running
 Open in browser: http://localhost:9090
 Press Ctrl + C to stop
==============================================
```

**Verify:**
1. Open **http://localhost:9090** in Chrome/Edge → the feedback form appears.
2. Click **Submit Feedback** with empty fields → the browser shows "Please fill out this field" (HTML validation works).
3. Fill in all fields (Name, Email, Category, click a star rating, Comments) → **Submit Feedback**.
4. A green ✔ **"Thank you, <name>!"** page appears with a summary table.
5. In CMD you see: `Feedback received: Feedback{name='...', ...}`
6. Optional health check — open **http://localhost:9090/health** → shows `OK`.

📸 **Screenshot 3:** Feedback form in the browser.
📸 **Screenshot 4:** Success page after submitting.
📸 **Screenshot 5:** CMD showing "Feedback received: ...".

**Stop the app:** click in the CMD window and press `Ctrl + C`.

> Port 9090 already in use? Stop the other program, or run on another port:
> `set PORT=9091` then `java -jar target\online-feedback-form.jar` and open `http://localhost:9091`.

---

## B. Maven Build – `mvn package`

```cmd
mvn package
```

(For a fresh build that first deletes the old `target` folder: `mvn clean package`.)

**What happens:** Maven compiles the code → runs 14 JUnit tests → packages the JAR.
The first run downloads plugins from the internet and takes 1–3 minutes.

**Expected output (end part; the two test classes may appear in either order):**
```
[INFO]  T E S T S
[INFO] Running com.feedback.FeedbackAppTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 ...
[INFO] Running com.feedback.FeedbackServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 ...
[INFO] Results:
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jar:3.4.1:jar (default-jar) @ online-feedback-form ---
[INFO] Building jar: C:\DevOps\online-feedback-form\target\online-feedback-form.jar
[INFO] BUILD SUCCESS
```

**Verify the artifact:**
```cmd
dir target\*.jar
dir target\surefire-reports
```
You should see `online-feedback-form.jar` and the test report files (`.txt` and `.xml`).

📸 **Screenshot 6:** `Tests run: 14, Failures: 0` and `BUILD SUCCESS`.
📸 **Screenshot 7:** `dir target\*.jar` showing the JAR.

---

## C. Git Initialization, GitHub Push and Feature Branch

### C.1 One-time Git configuration (skip if already done)

```cmd
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

### C.2 Initialize the repository and make the first commit

```cmd
git init -b main
git add .
git status
git commit -m "Initial commit: Online Feedback Form with Maven, Jenkins, GitHub Actions and Docker"
```

**Expected:** `git status` lists files in green (the `target` folder is **not** listed because of `.gitignore`).
Commit output ends with something like `15 files changed, ... insertions(+)`.

> If `git init -b main` gives an error (very old Git), use: `git init` then `git branch -M main`.

### C.3 Create the GitHub repository

1. Go to https://github.com → **+** (top right) → **New repository**.
2. Repository name: `online-feedback-form`
3. Visibility: **Public** (makes Jenkins setup simpler).
4. **Do NOT** tick "Add a README", ".gitignore" or "license" (the project already has them).
5. Click **Create repository**.

📸 **Screenshot 8:** Empty new repository page on GitHub.

### C.4 Connect and push `main`

```cmd
git remote add origin https://github.com/YOUR-USERNAME/online-feedback-form.git
git push -u origin main
```

A browser/login window may open the first time (Git Credential Manager) → sign in to GitHub → allow.

**Expected:** `branch 'main' set up to track 'origin/main'`. Refresh the GitHub page → all files are visible.

**Verify:** On GitHub, open the **Actions** tab → a workflow run **"Maven CI"** has started for `main` (details in Part F).

📸 **Screenshot 9:** GitHub repository showing the project files.

### C.5 Create a feature branch and make a change

```cmd
git checkout -b feature/update-feedback-form
```

**Expected:** `Switched to a new branch 'feature/update-feedback-form'`

Now make a small visible change in VS Code:
- Open `src\main\resources\static\index.html`
- Change the subtitle line
  `We value your opinion. Please share your feedback below.`
  to
  `We value your opinion. Your feedback helps us improve!`
- Save (`Ctrl + S`).

Check the build still passes, then commit and push the branch:

```cmd
mvn package
git status
git add .
git commit -m "Update feedback form subtitle"
git push -u origin feature/update-feedback-form
```

**Expected:** `BUILD SUCCESS`, then push output shows
`* [new branch] feature/update-feedback-form -> feature/update-feedback-form`

**Verify:**
```cmd
git branch
git log --oneline
```
`git branch` shows `* feature/update-feedback-form` and `main`; `git log --oneline` shows two commits.

📸 **Screenshot 10:** CMD showing branch creation, commit and push.
📸 **Screenshot 11:** `git branch` and `git log --oneline` output.

---

## D. Pull Request and Merge

1. Open your repository on GitHub. A yellow banner appears: **"feature/update-feedback-form had recent pushes"** → click **Compare & pull request**.
   (Or: **Pull requests** tab → **New pull request** → base: `main`, compare: `feature/update-feedback-form`.)
2. Title: `Update feedback form subtitle`
   Description: `Changed the subtitle text on the feedback form.`
3. Click **Create pull request**.
4. Wait for the check **"Maven CI / build"** to show a **green ✔** ("All checks have passed").
5. Click **Merge pull request** → **Confirm merge**.
6. (Optional) Click **Delete branch**.

📸 **Screenshot 12:** Pull Request page with the green CI check.
📸 **Screenshot 13:** "Pull request successfully merged and closed".

### Update your local `main`

```cmd
git checkout main
git pull origin main
git log --oneline
```

**Expected:** The log now includes `Update feedback form subtitle` (and a "Merge pull request #1" commit).

📸 **Screenshot 14:** `git log --oneline` on `main` after merge.

---

## E. Jenkins Pipeline

### E.1 Install and unlock Jenkins (one time)

1. Run the Jenkins **Windows LTS installer** (`.msi`).
   - When asked, run Jenkins as **LocalSystem** (simplest option for a lab).
   - Port: keep **8080**.
   - Java: select your **JDK 17 (or 21)** folder.
2. Open **http://localhost:8080**.
3. The "Unlock Jenkins" page shows the path of the `initialAdminPassword` file. Open that file in Notepad, copy the password, paste it.
4. Click **Install suggested plugins** (this includes Pipeline, Git and JUnit plugins).
5. Create the admin user → Save and Finish → **Start using Jenkins**.

### E.2 Configure Maven in Jenkins (one time)

1. **Manage Jenkins → Tools**.
2. Scroll to **Maven installations** → **Add Maven**.
3. Name: **`Maven`** (exactly this – the `Jenkinsfile` uses this name).
4. Tick **Install automatically** (keep the latest 3.9.x version).
5. Click **Save**.

📸 **Screenshot 15:** Maven installation named `Maven` in Jenkins Tools.

### E.3 Create the pipeline job

1. Jenkins Dashboard → **New Item**.
2. Name: `online-feedback-form-pipeline` → select **Pipeline** → **OK**.
3. Scroll to the **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/YOUR-USERNAME/online-feedback-form.git`
   - Credentials: **- none -** (repo is public)
   - Branch Specifier: `*/main`
   - Script Path: `Jenkinsfile`
4. Click **Save**.

📸 **Screenshot 16:** Pipeline job configuration.

### E.4 Run the pipeline

Click **Build Now**. A build (#1) appears under **Build History**.

**Expected stages (all green):**
1. `Declarative: Checkout SCM`
2. `Declarative: Tool Install`
3. `Checkout`
4. `Build and Test`
5. `Archive Artifact`
6. `Declarative: Post Actions`

**Verify:**
- Click the build → **Console Output** → near the end:
  `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`,
  `BUILD SUCCESSFUL: target/online-feedback-form.jar created and all tests passed.`
  and finally `Finished: SUCCESS`.
- Build page → **Build Artifacts** shows `online-feedback-form.jar` (you can download it).
- Build page → **Test Result** shows `0 failures` (14 tests).

📸 **Screenshot 17:** Pipeline stage view, all stages green.
📸 **Screenshot 18:** Console Output ending with `Finished: SUCCESS`.
📸 **Screenshot 19:** Build Artifacts / Test Result.

---

## F. GitHub Actions CI

The workflow file is `.github/workflows/maven-ci.yml`. It runs automatically **on every push** (any branch) and on Pull Requests to `main`. You already triggered it in Parts C and D.

### F.1 Verify

1. GitHub repository → **Actions** tab.
2. You will see runs named **Maven CI** (one for the `main` push, one for the feature branch push, one for the PR, one for the merge).
3. Click the latest run → click the **build** job.
4. Expand the steps – all should have a green ✔:
   - Checkout code
   - Set up Java 17
   - Build and test with Maven → contains `Tests run: 14, Failures: 0` and `BUILD SUCCESS`
   - Upload JAR artifact
5. On the run summary page, under **Artifacts**, you will see `online-feedback-form-jar`.

📸 **Screenshot 20:** Actions tab listing successful "Maven CI" runs.
📸 **Screenshot 21:** Job steps with green ticks and `BUILD SUCCESS`.

### F.2 (Optional) Trigger a new run with a push

```cmd
git checkout main
```
Make any small change (for example, add a line to `README.md`), then:
```cmd
git add .
git commit -m "Trigger GitHub Actions"
git push
```
Open the **Actions** tab → a new run starts within seconds.

---

## G. Docker Image Creation

1. Start **Docker Desktop** and wait until it says **"Engine running"**.
2. In CMD, in the project folder, make sure the JAR exists (the Dockerfile copies it):

```cmd
mvn clean package
```

3. Build the image (note the **dot** at the end – it means "current folder"):

```cmd
docker build -t online-feedback-form .
```

**Expected:** Steps such as `FROM eclipse-temurin:17-jre`, `COPY target/online-feedback-form.jar app.jar`, ending with
`naming to docker.io/library/online-feedback-form` (exact wording varies by Docker version). No red `ERROR`.

4. Verify the image:

```cmd
docker images
```

**Expected:** A row with REPOSITORY `online-feedback-form` and TAG `latest`.

📸 **Screenshot 22:** `docker build` finished successfully.
📸 **Screenshot 23:** `docker images` showing `online-feedback-form`.

---

## H. Docker Container Execution and Browser Verification

1. Run the container:

```cmd
docker run -d -p 9090:9090 --name feedback-app online-feedback-form
```

`-d` = run in background, `-p 9090:9090` = connect your PC's port 9090 to the container's port 9090.
**Expected:** a long container ID is printed.

> If your local app from Part A is still running, stop it first (`Ctrl + C`), otherwise port 9090 is busy.

2. Check it is running:

```cmd
docker ps
docker logs feedback-app
```

**Expected:** `docker ps` shows `feedback-app` with STATUS `Up ...` and PORTS `0.0.0.0:9090->9090/tcp`.
`docker logs` shows `Online Feedback Form is running`.

3. Browser verification:
   - Open **http://localhost:9090** → the form appears.
   - Fill it in and submit → success page appears.
   - Run `docker logs feedback-app` again → shows `Feedback received: ...`.

Optional CMD check:
```cmd
curl http://localhost:9090/health
```
**Expected:** `OK`

📸 **Screenshot 24:** `docker ps` showing the running container.
📸 **Screenshot 25:** Browser at `http://localhost:9090` (form served from Docker).
📸 **Screenshot 26:** Success page + `docker logs feedback-app` showing the feedback.

4. Stop and clean up (after the examiner has seen it):

```cmd
docker stop feedback-app
docker rm feedback-app
```

---

## Practical Completion Checklist

| # | Task | Proof |
|---|------|-------|
| 1 | App runs locally | Screenshots 3–5 |
| 2 | `mvn package` → BUILD SUCCESS, 14 tests, JAR | Screenshots 6–7 |
| 3 | Git repo pushed to GitHub | Screenshot 9 |
| 4 | Feature branch created, committed, pushed | Screenshots 10–11 |
| 5 | Pull Request created and merged | Screenshots 12–14 |
| 6 | Jenkins pipeline successful | Screenshots 17–19 |
| 7 | GitHub Actions successful on push | Screenshots 20–21 |
| 8 | Docker image built | Screenshots 22–23 |
| 9 | Container running and verified in browser | Screenshots 24–26 |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `'java'` / `'mvn'` / `'git'` is not recognized | Add it to **Path** (section 0.1), then **reopen CMD**. |
| `mvn` shows `Java version: 1.8` or `release version 17 not supported` | `JAVA_HOME` points to an old Java. Set it to the JDK 17 folder and reopen CMD. |
| `No compiler is provided in this environment` | `JAVA_HOME` points to a JRE, not a JDK. Install a JDK 17 and point `JAVA_HOME` to it. |
| Maven `Could not transfer artifact ...` | Check internet connection (college proxy/Wi-Fi may block it). Try a mobile hotspot. |
| `Address already in use` / `BindException` when starting the app | Port 9090 is busy (maybe the Docker container is running). `docker stop feedback-app` or use `set PORT=9091`. |
| `git push` asks for a password and rejects it | GitHub needs browser login or a Personal Access Token, not your account password. Let Git Credential Manager open the browser login. |
| `git push` rejected: `fetch first` | You created the GitHub repo with a README. Run `git pull origin main --allow-unrelated-histories`, then push again. |
| Jenkins: `Tool type "maven" does not have an install of "Maven" configured` | Do section E.2; the name must be exactly `Maven`. |
| Jenkins: `'git' is not recognized` / cannot run git | Install Git for Windows, then **Manage Jenkins → Tools → Git installations** → Path to Git executable: `C:\Program Files\Git\bin\git.exe`. Restart Jenkins. |
| Jenkins: `No compiler is provided` | Jenkins runs with a JRE. **Manage Jenkins → Tools → JDK installations → Add JDK**, Name `JDK17`, JAVA_HOME = your JDK 17 folder; then in `Jenkinsfile` add `jdk 'JDK17'` inside the `tools { }` block, commit and push. |
| Jenkins page doesn't open | Open **Services** (Windows key → "services") → find **Jenkins** → Start. |
| `docker build` error: `target/online-feedback-form.jar: not found` | Run `mvn package` first. |
| `docker: error during connect` / `cannot find the file specified` | Docker Desktop is not running. Start it and wait for "Engine running". |
| `docker run` error: `container name "/feedback-app" is already in use` | `docker rm -f feedback-app`, then run again. |
| `docker run` error: `port is already allocated` | Stop the local Java app (Ctrl + C) or the old container. |
