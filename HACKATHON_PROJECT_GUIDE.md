# LocalSMS Full Operations and Release Documentation

## 1. Document Goal
This file is the complete operational guide for this repository.

It covers all required steps:
- What to do right after copying the repository
- How to install all dependencies
- How to run every service locally
- How to build and install the latest Android APK
- How to validate the full stack before release
- How to clean unused code and assets
- How to push to GitHub with many meaningful commits

## 2. Repository Modules
- frontend: React + TypeScript + Vite web client
- backend: Node.js message queue API
- otp-auth-service: Node.js OTP authentication service
- NativeGateway: Android application

## 3. Prerequisites
Install tools before doing anything else:
- Node.js 20+
- npm
- Java 17+
- Android SDK + Platform Tools
- adb
- Git

Recommended:
- Android Studio

Verify tools:

```bash
node -v
npm -v
java -version
adb version
git --version
```

## 4. First-Time Setup After Copying Repository
Run these commands from repository root:

```bash
cd backend && npm install
cd ../otp-auth-service && npm install
cd ../frontend && npm install
cd ..
```

## 5. Environment Configuration

### 5.1 Auth Service Environment
Create environment file:

```bash
cd otp-auth-service
cp .env.example .env
```

Update secrets in .env before production use:
- OTP_HASH_SECRET must be custom
- LOCAL_SMS_GATEWAY_PASS must be your real gateway password
- MONGODB_URI must point to your own database

Never commit real secrets.

### 5.2 Backend Environment
Backend currently runs with default port configuration from source.
If you later add environment variables, document them in this file before release.

### 5.3 Frontend Environment
Frontend uses hardcoded local API targets in source.
If endpoints change, update frontend source and re-run verification section.

## 6. Local Run Procedure
Open three terminals.

Terminal A (backend):

```bash
cd backend
node server.js
```

Terminal B (auth service):

```bash
cd otp-auth-service
node server.js
```

Terminal C (frontend):

```bash
cd frontend
npm run dev -- --host
```

## 7. Runtime Health Checks
Run after all services start:

```bash
curl http://localhost:3001/api/messages
curl http://localhost:3002/health
```

Expected:
- backend responds with JSON array or object
- auth service responds with success payload

## 8. Android Build and Latest APK Install
From repository root:

```bash
cd NativeGateway
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

Launch app on connected device:

```bash
adb devices
adb shell monkey -p com.kalpsms.localsms -c android.intent.category.LAUNCHER 1
```

Latest debug APK path:
- NativeGateway/app/build/outputs/apk/debug/app-debug.apk

## 9. Final End-to-End Verification Checklist
All items must pass before pushing:

1. frontend build

```bash
cd frontend
npm run build
```

2. auth service syntax check

```bash
cd otp-auth-service
npm run check
```

3. backend endpoint is reachable
4. auth health endpoint is reachable
5. Android app installs on real device
6. Android app launches successfully
7. OTP masking works in message views and logs

## 10. Cleanup Policy Before Every Push

### 10.1 Remove Unused Code
- Delete components that are no longer imported
- Remove dead helper functions
- Remove stale commented code blocks
- Remove obsolete TODO sections that no longer apply

### 10.2 Remove Unused Files
- Delete files not referenced anywhere
- Delete temporary logs and local dumps
- Delete generated local artifacts not needed in repository

### 10.3 No Extra PNG Policy
- Do not add decorative PNG files
- Keep only required Android launcher/resources
- Prefer CSS-driven UI over image-driven UI

Audit commands:

```bash
find . -type f -iname "*.png"
find . -type f -size +5M
```

### 10.4 Naming Rule
New docs and commit messages must use current project naming only.

## 11. High-Count Meaningful Commit Strategy
Goal: increase commit count with quality, not noise.

Rules:
- one logical unit per commit
- no mixed concerns in one commit
- every commit message must explain intent
- each commit should be easy to review independently

### 11.1 Suggested Commit Plan (15 commits)
1. docs(repo): rewrite master operations runbook
2. chore(frontend): dependency alignment and lock refresh
3. refactor(frontend): simplify post-login flow
4. style(frontend): neutral minimal UI pass
5. fix(frontend): remove unused navigation logic
6. fix(frontend): cleanup auth labels and copy
7. fix(android): mask otp in message details
8. fix(android): mask otp in incoming preview
9. fix(android): mask otp in logs list
10. chore(android): verify build and install workflow
11. chore(cleanup): remove dead code paths
12. chore(cleanup): remove unused files
13. chore(assets): enforce no extra png rule
14. test(manual): record end-to-end validation evidence
15. chore(release): final release prep commit

### 11.2 Commit Message Standard
Use format:

```text
type(scope): short summary
```

Types:
- feat
- fix
- refactor
- style
- chore
- docs
- test

Good examples:
- fix(android): mask 6-digit otp in logs adapter
- refactor(frontend): replace dashboard with single welcome page
- docs(repo): add clone-to-release runbook

Bad examples:
- update
- final
- changed files

## 12. GitHub Push Procedure

### 12.1 Create Release Branch

```bash
git checkout -b release/localsms-v1
```

### 12.2 Commit in Small Logical Chunks
Use selective staging:

```bash
git add <related-files-only>
git commit -m "type(scope): message"
```

### 12.3 Inspect Commit Quality

```bash
git log --oneline -n 30
```

### 12.4 Push Branch

```bash
git push -u origin release/localsms-v1
```

### 12.5 Open Pull Request
PR checklist must include:
- verification checklist from section 9
- cleanup confirmation from section 10
- commit quality confirmation from section 11

## 13. Troubleshooting

### 13.1 Port Already In Use
Symptom: service fails with address in use.
Action:
- stop old process using same port, or
- keep existing running process and use health endpoint to confirm status.

### 13.2 Device Not Detected
Check:

```bash
adb devices
```

If unauthorized:
- replug cable
- allow USB debugging prompt on phone

### 13.3 Android Install Failure
Actions:
- ensure same package is not blocked by device policy
- run clean and install again

```bash
cd NativeGateway
./gradlew clean installDebug
```

### 13.4 Frontend Build Failure
Actions:
- reinstall deps
- run build again

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

## 14. Single Command Summary
For quick run after fresh clone:

```bash
cd backend && npm install && node server.js
```

```bash
cd otp-auth-service && npm install && cp .env.example .env && node server.js
```

```bash
cd frontend && npm install && npm run dev -- --host
```

```bash
cd NativeGateway && ./gradlew clean assembleDebug installDebug
adb shell monkey -p com.kalpsms.localsms -c android.intent.category.LAUNCHER 1
```

## 15. Definition of Done for Release
A release is done only if:
- all checks in section 9 pass
- cleanup rules in section 10 are fully applied
- commit history is meaningful and granular
- release branch is pushed and PR is opened

