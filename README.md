#Project structure

This is a Kotlin Multiplatform project targeting Android, iOS.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

The composeApp module consists of the following source sets: androidMain, commonMain and iosMain (with commonTest if you chose to include tests).

A source set is a Gradle concept for a number of files logically grouped together, where each group has its own dependencies. In Kotlin Multiplatform, different source sets can target different platforms.

The commonMain source set uses the common Kotlin code, and platform source sets use Kotlin code specific to each target:
- androidMain also uses Kotlin/JVM.
- iosMain uses Kotlin/Native.

In general, write your implementation as common code whenever possible instead of duplicating functionality in platform-specific source sets.

In the composeApp/src/commonMain/kotlin directory, open the App.kt file. It contains the App() function, which implements a minimalistic but complete Compose Multiplatform UI.

```bash
composeApp/
 └── src/
 │    └── commonMain/
 │    │    └── kotlin/com/team_scheduler/
 │    │    │      ├── ui/
 │    │    │      │    ├── screens/
 │    │    │      │    │    ├── schedule/
 │    │    │      │    │    │    └── ScheduleScreen.kt
 │    │    │      │    │    ├── tasks/
 │    │    │      │    │    │    └── TasksScreen.kt
 │    │    │      │    │    └── settings/
 │    │    │      │    │         └── SettingsScreen.kt
 │    │    │      │    └── components/
 │    │    │      │         ├── TaskCard.kt
 │    │    │      │         ├── CalendarItem.kt
 │    │    │      │         └── BottomBar.kt
 │    │    │      ├── navigation/
 │    │    │      │    └── NavGraph.kt
 │    │    │      ├── viewmodel/
 │    │    │      │    ├── ScheduleViewModel.kt
 │    │    │      │    ├── TasksViewModel.kt
 │    │    │      │    └── UserViewModel.kt
 │    │    │      └── model/
 │    │    │           ├── Task.kt
 │    │    │           ├── Schedule.kt
 │    │    │           ├── User.kt
 │    │    └── resources/
 │    ├── androidMain/
 │    │   └── kotlin/ (Android-only code if needed)
 │    └── iosMain/
 │        └── kotlin/ (iOS-only code if needed)
```

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

# GitHub Workflow

This guide walks you through everything you need to do to get the repository on your computer, make a change, push it, open a Pull Request (PR), and get it merged. It assumes you already have Git intalled and setup and that you clone repos with HTTPS.
---

## Some concepts worth remembering

A **GitHub organization** is like a shared workspace that holds all our project repositories, teams, and permissions.

A **Team** is group inside the organization that gives members access to repositories (right now there's only 1 team but we could create more if we need).

A **Repository (Repo):** Where our project’s code live.

A **branch** is a separate version of the project where you can safely make changes without affecting the main code.

- main → contains stable, reviewed, working code.
- feature/<name> → for individual features or tasks. Example branch names: feature/login-screen, feature/attendance-tracking, fix/ui-overlap

A **commit** is a snapshot of your work —> it saves changes with a message describing what you did. You can do it from Android Studio IDE or using the commands:
  ```bash
  git add .
  git commit -m "Implement login screen layout and navigation"
  git push origin feature/login-screen
  ```

💡 Use clear, descriptive commit messages — imagine someone reading your commits later to understand what changed.

- Everyone in the team can **read**, **clone**, and **create branches**.
- The `main` branch is **protected** — you **must not** push directly to it.
- We do all our work in **feature branches** and merge through **Pull Requests**.


## Setting Up Locally

⚠️ You’ll need a Personal Access Token (PAT) to push changes later. You can generate it now (Step 3) or the first time Git asks for it.

### Step 1 — Clone the Repository
1. Go to the repository in GitHub.
2. Click the green **“Code”** button.
3. Copy the HTTPS link.
4. In your terminal, run:
  ```bash
   cd <folder where you want to store the repo (eg. Desktop)>
   git clone https://github.com/hkr-mobile-dev-team-1/team-scheduler-app.git
   cd <team-scheduler-app>
  ```
Now you have a local copy of the project on your computer.

💡 You can also clone directly in Android Studio: Get from VCS → GitHub → Log in → Select repository → Clone.

### Step 2 — Open it in Android Studio
1. Open Android Studio
2. Choose File → Open, navigate to the cloned repo folder, and open it.
3. Let Gradle sync and Android Studio index the project.

### Step 3 — Create a Personal Access Token (PAT) — one time per GitHub account
1. Sign in to GitHub and go to **Settings → Developer settings → Personal access tokens → Tokens (classic)** and click **Generate new token (classic)**.
2. Give it a name like `android-dev`.
3. Under scopes, check **repo** (this gives push/pull access to private repos).
4. Create the token and **copy it** (you’ll only see it once). Keep it secret.
   When you run `git push` and Git prompts:
```bash
Username for 'https://github.com': <your-github-username>
Password for 'https://github.com': <paste-your-PAT-here>
```
Enter your GitHub username, and paste the PAT where it asks for the password. The characters won’t show while pasting — that’s normal.

If you're using Android Studio to push changes, it will ask for authentication the first time. Choose option `token`, paste it. It will remember it for future pushes.

## Workflow for New Features

Every new feature, bug fix, or task should be developed in its own branch. This keeps work isolated and makes reviewing easier. Whenever you start working on a new task or feature:

### 1. Update your local main branch
  ```bash
  git checkout main
  git pull origin main
  ```

### 2. Create a new branch
  ```bash
  git checkout -b feature/<short-description>
  ```
If you use Android Studio, you can create a branch from the bottom-right branch widget or from VCS → Git → Branches → New Branch…
### 3. Work on your feature
- Commit regularly with clear messages
- Push changes to your branch
  ```bash
  git push origin feature/<short-description>
  ```
If you are using Android Studio GitHub integration: use Commit (or Commit and Push) from the VCS menu or the commit toolbar. If prompted for credentials, use your GitHub username and paste your PAT in the password field.

### 4. Create a Pull Request (PR).
Open the repository page on GitHub and you should see a “Compare & pull request” suggestion for your pushed branch. If not:

1. Go to **Pull requests → New pull request**.
2. Select the base branch (`main`) and compare branch (your `feature/...` branch).
3. Write a short description of what your PR changes.
4. If your work is incomplete, set the PR to **Draft**. When ready, click **Ready for review**.

### 5. Code Review
- At least one other teammate must review your PR.
- They can request changes or approve it.
- Once approved, it can be merged into main.

### 6. After merging: update your local branches
After a PR is merged, keep local branches up to date:
```bash
git checkout main
git pull origin main
```
Delete your feature branch locally and remotely if you like:
```bash
git branch -d feature/short-description
git push origin --delete feature/short-description
```

## Key Rules
- 🚫 Never push directly to main.
- ✅ Always open a Pull Request.
- 👀 Every PR must be reviewed by at least one teammate.
- 🧹 Use “Squash and merge” to keep commit history clean.

## Using GitHub Issues

We use GitHub Issues to track tasks, bugs, and progress.

### Creating an Issue

1. Go to the Issues tab.
2. Click New Issue.
3. Write a clear title and description.
4. Assign it to yourself or a teammate.
5. Add relevant labels (e.g., feature, bug, enhancement).

### Linking Issues to Pull Requests

In your pull request description, you can automatically close issues:
```bash
Closes #12
```
That will close issue #12 when the PR is merged.

## Common commands cheat-sheet
```bash
# clone
git clone https://github.com/<org>/<repo>.git

# update base branch
git checkout dev
git pull origin dev

# create branch
git checkout -b feature/your-change

# stage, commit, push
git add .
git commit -m "Short descriptive message"
git push -u origin feature/your-change

# delete branch locally / remotely
git branch -d feature/your-change
git push origin --delete feature/your-change
```

## Best Practices
1. Keep branches focused and small (one feature = one branch)
2. Pull latest changes before starting work
3. Write meaningful commit messages
4. Review teammates’ PRs thoughtfully
5. Test your feature before opening a PR

