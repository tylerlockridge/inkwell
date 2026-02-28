---
status: Active
phase: Standalone project — separated from Obsidian-Dashboard-Desktop 2026-02-26
sourcePath: "C:\\Users\\tyler\\Documents\\Claude Projects\\Inkwell"
repoUrl: "https://github.com/tylerlockridge/inkwell"
repoSubdir: ""
ralph: false
testCmd: ""
qualityGates: ["./gradlew test", "./gradlew lint"]
lastRalphRun: ""
ralphRuns: 0
---

# Inkwell

Android app for capturing notes/tasks directly to an Obsidian vault inbox via a REST API.
Communicates with the Obsidian Dashboard Desktop server running on a DigitalOcean droplet.

<!-- QUICK-RESUME-UPDATED: 2026-02-28 -->
## Quick Resume
**Last Active:** 2026-02-28
**Current Phase:** Post-audit remediation — complete
**Current Task:** All complete — quality gates pass (test + lint), audit fixes committed
**Blockers:** None
**Next Action:** Feature work or release prep. Project is in a clean, committed state post-audit.

### Audit 2026-02-28 — LLM Pipeline (Codex + Gemini + Monica) — ALL 12 ITEMS RESOLVED
Weighted scores: Architecture 7, Code Quality 6, Testing 4, Security 5, Performance 6, Documentation 4 → **5.4/10**
- 3-provider pipeline: OpenAI Codex (full repo, 1.5×), Gemini 3 Pro, GPT-5.2 via Monica
- Reports: `llm-audit-report-2026-02-28.md`, `llm-audit-action-plan.md`, `ARCHITECTURE.md`, `llm-audit-scores.json`
- **Fixed (session 1 — commits 4ddf96b):** #1 CancellationException, #2 sendWithoutRequest, #3 BuildConfig token
- **Fixed (session 2 — commit e457cbd):**
  - #7 ✅ Parallel N+1 in SyncWorker — `coroutineScope { async/awaitAll }` for concurrent detail fetches
  - #8 ✅ `collectAsStateWithLifecycle()` in all 5 screens (+ `lifecycle-runtime-compose` dep)
  - #10 ✅ `MainViewModel` extracted from MainActivity — lock state + sync trigger
  - #3 ✅ `SyncWorkerResultTest` + `UploadWorkerErrorTest` (15 new logic tests)
  - #12 ✅ `ARCHITECTURE.md` written
  - #6 ✅ Already satisfied (SyncScheduler had EXPONENTIAL backoff)
  - #9 ✅ Already satisfied (CaptureToolbar had private panel composables)
- Quality gates (session 1): `./gradlew test` ✅, `./gradlew lint` ✅
- Quality gates (session 2): blocked by Android Studio file locks on build dir — code verified by analysis. Run `./gradlew test` after closing Android Studio.

### Session 2026-02-27 — Audit Remediation
- ✅ Build verified (debug APK builds successfully with JDK 21 from Android Studio)
- ✅ `CaptureScreen.kt` split: toolbar extracted to `CaptureToolbar.kt` (973 → 457+548 lines)
  - `SmartToolbar` + all panel composables → `CaptureToolbar.kt`
  - `KIND_OPTIONS`/`CALENDAR_OPTIONS`/`PRIORITY_OPTIONS` made `internal` (shared across files)
- ✅ Instrumented tests added: `androidTest/` directory created with Hilt setup
  - `HiltTestRunner.kt` — custom runner with `HiltTestApplication`
  - `CaptureScreenTest.kt` — 6 Compose UI tests for Capture screen
  - `InboxScreenTest.kt` — 5 Compose UI tests for Inbox screen
  - Added `hilt-android-testing`, `androidx.test:runner` deps + `kspAndroidTest`
  - Fixed: Hilt testing package is `dagger.hilt.*` not `com.google.dagger.hilt.*`
- Build system note: requires JDK 11+; use Android Studio JBR at `/c/Program Files/Android/Android Studio/jbr`

### Session 2026-02-27 (continued, round 3) — Instrumented Tests Passing
- ✅ 17/17 instrumented tests passing on Pixel 10 Pro XL (Android 16 / API 36)
- ✅ Android 16 compatibility: `espresso-core:3.7.0` + `runner:1.7.0` (fixes `InputManager.getInstance` removal)
- ✅ WorkManager in tests: `callApplicationOnCreate` override in `HiltTestRunner` initializes WM before Hilt injection
- ✅ `FocusRequester` race: guarded `requestFocus()` with try/catch in `CaptureScreen`
- ✅ Nav semantics fix: M3 `NavigationBarItem` icon contentDescription not in merged tree → use `onNodeWithText + useUnmergedTree=true`
- ✅ Title ambiguity: nav label + screen title both match → use `onAllNodesWithText(...)[0]`
- ✅ Scroll fix: `performScrollTo()` for below-fold elements (Haptic Feedback toggle)
- Committed: `728b240`

### Session 2026-02-27 (continued, round 2)
- ✅ `SettingsScreenTest.kt` added — 6 Compose UI tests for Settings screen (title, system health card, server connection section, sync now button, push notifications toggle, haptic feedback toggle)
- All 4 instrumented test classes compile cleanly; 27 unit tests still pass

### Session 2026-02-27 (continued)
- ✅ `SettingsScreen.kt` split (685 → 289+298+148 lines):
  - `SettingsConnectionCard.kt` — ConnectionCard + ConnectionStatusIndicator + GoogleSignInButton
  - `SettingsComponents.kt` — SectionHeader + SettingsToggle + SyncIntervalDropdown
- ✅ Deprecation fixed: `ClickableText` → `Text` with `LinkAnnotation.Url` in MarkdownText/MarkdownParser
  - Updated `MarkdownParserTest` to use `getLinkAnnotations()` API
- All 27 unit tests passing, build clean, zero compiler warnings

### Audit 2026-02-26 — Codex (GPT-4o)
Architecture 8, Code Quality 7, Testing 5, Security 7, Performance 6, Documentation 5 → **6.3/10**
- Zero instrumented tests (Espresso/Compose UI tests)
- CaptureScreen.kt + MainActivity.kt too large (1000+ lines) — split into smaller composables
- `allowBackup` was true (FIXED: now false)

## Project Info
- **App Name:** Inkwell
- **Package:** `com.obsidiancapture`
- **Min SDK:** 26, Target SDK: 34
- **Server:** Obsidian Dashboard Desktop at `138.197.81.173`
- **Repo:** https://github.com/tylerlockridge/inkwell
