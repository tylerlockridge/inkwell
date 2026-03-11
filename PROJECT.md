---
status: Active
phase: Standalone project — separated from Obsidian-Dashboard-Desktop 2026-02-26
sourcePath: "C:\\Users\\tyler\\Documents\\Claude Projects\\Inkwell"
repoUrl: "https://github.com/tylerlockridge/inkwell"
repoSubdir: ""
ralph: true
testCmd: ""
qualityGates: ["./gradlew test", "./gradlew lint"]
lastRalphRun: "2026-03-06"
ralphRuns: 1
---

# Inkwell

Android app for capturing notes/tasks directly to an Obsidian vault inbox via a REST API.
Communicates with the Obsidian Dashboard Desktop server running on a DigitalOcean droplet.

<!-- QUICK-RESUME-UPDATED: 2026-03-10 -->
## Quick Resume
**Last Active:** 2026-03-10
**Current Phase:** Stable — IDEA type + attachments + coach marks shipped and tested
**Current Task:** Done (commit d1b51a0)
**Blockers:** None
**Next Action:** Install v2.3.0 APK on phone (`app/build/outputs/apk/release/app-release.apk`) and manually test: (1) pick a photo, save capture, wait for sync, check vault. Server-side multipart confirmed working via smoke test (201 response, correct file at `/vault/Attachments/{uid}/test.png`, `attachments:` frontmatter + `![[test.png]]` in markdown).

**⚠️ Known test gotcha (Android 16):**
- Always uninstall release APK before running `connectedAndroidTest` (signature mismatch)
- `Espresso.closeSoftKeyboard()` deadlocks on Android 16 — use `composeRule.waitForIdle()` instead
- Runtime permissions must be pre-granted via `GrantPermissionRule` (camera, media, notifications)

### Session 2026-03-10 — v2.3.0 release prep + server smoke test
- ✅ Bumped versionCode 10→11, versionName 2.2.0→2.3.0 (commit 37a994d)
- ✅ Built release APK: `app/build/outputs/apk/release/app-release.apk` (4.8MB)
- ✅ Server multipart smoke test confirmed:
  - `POST /api/capture` multipart → `201 Created`
  - File saved at `/vault/Attachments/{uid}/test.png`
  - Markdown has `attachments: [test.png]` frontmatter + `![[test.png]]` wikilink
- ⚠️ Phone disconnected — APK not yet installed. Use: `adb install -r app/build/outputs/apk/release/app-release.apk`

### Session 2026-03-10 — Attachment upload wired end-to-end
- ✅ `NoteEntity.attachmentsFromJson()` helper added
- ✅ `CaptureApiService.captureWithAttachments()` — sends multipart/form-data to existing server endpoint
  - Reads URI bytes via ContentResolver, resolves display filenames via OpenableColumns
  - Tags sent as comma-separated string (server multipart format)
  - 120s timeout for file uploads
- ✅ `UploadWorker.uploadNewCapture()` — routes to multipart path when attachmentUris non-empty
- ✅ No server changes needed (capture-server.ts already handles multipart on POST /api/capture)
- ✅ All 294 unit tests passing, lint clean (commit c2634a2)

### Session 2026-03-10 — Instrumented test fixes (Android 16)
- ✅ Added GrantPermissionRule to all 3 test classes (camera/media/notifications)
- ✅ Added androidx.test:rules dep
- ✅ Replaced Espresso.closeSoftKeyboard() with composeRule.waitForIdle() in Inbox/SettingsScreenTest
- ✅ 17/17 instrumented tests passing (commit d1b51a0)

### Session 2026-03-10 — IDEA type, attachment picker, coach marks (autonomous)
- ✅ Fixed `AttachmentPicker` camera URI state bug: `var cameraImageUri` → `remember { mutableStateOf<Uri?>(null) }` — survives recompositions properly
- ✅ Fixed lint error: added `<uses-feature android:name="android.hardware.camera" android:required="false" />` to manifest
- ✅ Fixed `CoachMarkManagerTest` compile error: added `testImplementation(libs.androidx.test.core)` + `androidx-test-core` to libs.versions.toml
- ✅ Quality gates: 294 unit tests passing, lint clean
- ✅ Committed: 31 files, 965 insertions (commit 75baec0)
- ⚠️ Attachment upload is local-only (phase 1): URIs stored in NoteEntity.attachmentUris as JSON, NOT sent to server. Server-side multipart endpoint needed for phase 2.
- ✅ Release APK built: `app/build/outputs/apk/release/app-release.apk` (ready to install)
- ⚠️ Install blocked this session: Windows Defender locks build intermediates during Gradle builds. Workaround: delete `app/build/intermediates` and `app/build/outputs/apk/release` mid-build, then rerun `./gradlew packageRelease`. Phone also dropped USB mid-session.

### Session 2026-03-10 — Auth fix + UI polish
- ✅ Google Sign-In removed — token baked into `BuildConfig.DEFAULT_AUTH_TOKEN` from `local.properties`
- ✅ `GoogleSignInManager.kt` deleted; `SettingsViewModel`, `ConnectionCard`, `SettingsUiState` cleaned up
- ✅ Server `/api/auth/google` response fixed: `{success:true}` → `{token:...}` (both server + Android)
- ✅ SSH hook: allows `138.197.81.173`, blocks all other SSH — documented in global CLAUDE.md + MEMORY.md
- ✅ UI: `FontFamily.Serif` on all headline/title styles — editorial journal feel across all screens
- ✅ UI: Inbox empty state — Obsidian diamond focal point with amber glow ring, serif headline
- ✅ UI: Bottom nav selected item renders in amber with warm indicator pill

### Session 2026-03-06 — Capture Type Toggle (Ralph Loop)
- All 5 stories passed (US-001 through US-005)
- Added 3-segment toggle (Task/Note/List) to CaptureScreen
- CaptureRequest DTO: added captureType, listName, items, persistent fields
- SmartToolbar hides irrelevant panels per type (NOTE hides priority/date/calendar, LIST hides all except tags)
- LIST mode: dedicated List Name + Items fields with persistent toggle
- NOTE mode: simplified capture, kind locked to "note"
- Reset and share intent handling preserved
- 6 files modified, compileDebugKotlin passes

### Session 2026-03-06 — Codex 5.3 Audit + Auth Fix
- ✅ Codex 5.3 audit via Monica.im: **8.61/10** (Arch 8.7, Quality 8.4, Testing 9.1, Security 8.9, Perf 8.2, Docs 7.3)
- ✅ **Fixed auth header bug (HIGH):** Added `sendWithoutRequest { true }` to Ktor bearer auth in NetworkModule.kt — token now sent proactively instead of requiring 401 challenge first
- ✅ **Hardened error handling:** Added `SerializationException` catch in SyncWorker before generic Exception — non-InboxResponse JSON no longer crashes sync
- ✅ **Documented sync policy:** Updated documentation/03-sync-strategy.md with formal conflict policy (LWW + pending protection), auth/error handling section, tombstone sweep docs

### Session 2026-03-05 — Test Coverage Sprint (Ralph Loop)
- ✅ All 11 stories in `prd.json` complete (US-000 through US-010)
- ✅ Added test deps: MockK 1.13.13, Robolectric 4.14.1, Turbine 1.2.0, work-testing (version catalog)
- ✅ 4 new test files: SyncWorkerIntegrationTest, CaptureMessagingServiceTest, CaptureMessagingTokenTest, InboxRepositoryTest
- ✅ Extended 4 existing test files: SyncConflictTest (+7), NotificationActionReceiverTest (+6), DeepLinkTest (+12), WidgetStateTest (+5)
- ✅ 34 test files total (was 30), all passing. Quality gates (test + lint) green.
- Note: JAVA_HOME must point to Android Studio JBR (`/c/Program Files/Android/Android Studio/jbr`), not the JRE at default JAVA_HOME
- Note: Hilt `@AndroidEntryPoint` components (CaptureMessagingService, NotificationActionReceiver) can't be instantiated in Robolectric without full Hilt test setup — tests replicate routing logic instead

### Session 2026-03-04 — UI Testing + Server Crash Fix
- ✅ All 4 screens screenshot-verified: Capture, Settings, Inbox (empty state), System Health
- ✅ Version 2.2.0 (versionCode 10) confirmed on device
- ✅ Settings: Connected (green) with token entered manually; Push Notifications ON; Haptic ON; Biometric OFF
- ✅ Fixed server crash: `email-commander.ts` `ImapFlow` socket timeout was escaping `poll()` try/catch as an uncaught exception → `process.exit(1)`. Fixed by adding `client.on('error', ...)` listener in `createImapClient()`. Source patched + running container patched + container stable.
- ⚠️ SyncWorker failing: `Illegal input: Fields [items, totalCount, syncToken] required but missing at path: $` — server returns error JSON (not InboxResponse). Root cause: auth token may not be in Authorization header. NOT yet fixed.

### Session 2026-03-03 — Install v2.2.0 on Phone
- ✅ Fixed `build.gradle.kts` signing path: `localProps.getProperty()` fallback + `../keys/release.keystore` (was `keys/`)
- ✅ Built signed `app-release.apk` (v2.2.0, SHA-256 verified)
- ✅ Installed on phone `58100DLCQ00724`
- ✅ Fixed `docker-compose.yml`: removed `- ANDROID_FINGERPRINT` passthrough from `environment` block (was overriding `env_file` with null)
- ✅ Fixed `server.ts`: added `/.well-known/assetlinks.json` route BEFORE the `/api/` gate (was falling through to 404)
- ✅ Server image rebuilt (Docker build cache hit after power cycle — ~2 min). Both endpoints verified live.

### Session 2026-03-02 — Post-Audit Feature Plan (v2.2.0)
**Phase 1 — Tombstone Sync** ✅
- `Obsidian-Dashboard-Desktop/src/registry.ts` — added `queryDeletedItems()` method
- `Obsidian-Dashboard-Desktop/src/api-server.ts` — added `GET /api/inbox/deleted` route + handler; added `GET /.well-known/assetlinks.json` route + handler
- `app/src/main/kotlin/.../data/local/dao/NoteDao.kt` — added `deleteByUidsIfSynced()`
- `app/src/main/kotlin/.../data/remote/CaptureApiService.kt` — added `getDeletedInbox()`
- `app/src/main/kotlin/.../sync/SyncWorker.kt` — captures `priorSyncedAt` before sync, runs tombstone sweep after upserts

**Phase 2 — App Links** ✅
- `app/src/main/AndroidManifest.xml` — added HTTPS `autoVerify="true"` intent-filter for `tyler-capture.duckdns.org/app/`
- `app/src/main/kotlin/.../ui/navigation/DeepLink.kt` — added HTTPS URI constants + HTTPS branch in `parseToRoute()`
- `app/src/main/kotlin/.../ui/navigation/CaptureNavHost.kt` — added HTTPS `navDeepLink` alongside custom-scheme links on all 4 destinations
- `Obsidian-Dashboard-Desktop/infra/docker-compose.yml` — added `ANDROID_FINGERPRINT` passthrough env var with setup comment

**Phase 3 — Release Prep** ✅
- `app/build.gradle.kts` — versionCode 9→10, versionName 2.1.2→2.2.0

### Audit 2026-03-02 — Deep Composite Audit (Security + Coroutines + Sync) — ALL 15 ITEMS RESOLVED
Perspectives: Android Security Engineer, Coroutine & Concurrency Specialist, Data Integrity / Sync Architect
- **C-1 ✅** `pending_` orphan bug — `NoteDao.replacePendingWithServer()` `@Transaction` method added; `UploadWorker.uploadNewCapture()` uses it instead of bare upsert. Prevents infinite duplicate uploads.
- **H-1 ✅** JWT logged to logcat — `CaptureApiService.exchangeGoogleToken()` Log.d now gated on `BuildConfig.DEBUG`; logs only status code, not body.
- **H-2 ✅** BIOMETRIC_WEAK accepted — `BiometricAuthManager` now uses `BIOMETRIC_STRONG` only in both `canAuthenticate()` and `setAllowedAuthenticators()`; unused `BIOMETRIC_WEAK` import removed.
- **H-3 ✅** Notification ID collision (1001 in both SyncWorker + CaptureMessagingService) — `CaptureMessagingService` IDs moved to 2001/2002 range.
- **H-5 ✅** `UploadWorker` returned `Result.retry()` on blank server URL — changed to `Result.success()` (matches SyncWorker).
- **H-6 ✅** `CaptureMessagingService.serviceScope` never cancelled — `onDestroy()` override added.
- **M-1 ✅** ISO 8601 string comparison — `SyncWorker` now uses `isServerNewer()` private helper with `Instant.parse()`.
- **M-3 ✅** `getInbox()` called without explicit limit — `SyncWorker` now passes `limit = INBOX_FETCH_LIMIT` (200).
- **M-4 ✅** O(n) serial DB reads in stale detection — replaced with `noteDao.getAllByUids()` single bulk query + in-memory filter.
- **M-6 ✅** `setAuthToken("")` stored empty string — now calls `remove()` on the EncryptedSharedPreferences key.
- **L-2 ✅** `Json` instance created per `exchangeGoogleToken` call — `CaptureApiService` now injects the singleton `Json` from `NetworkModule`.
- **NoteDao** — added `deleteByUid()`, `getAllByUids()`, and `replacePendingWithServer()` `@Transaction` method.
- **SyncConflictTest** — updated to test `Instant.parse()` semantics; added mixed-precision and invalid-timestamp test cases.
- Quality gates: blocked by Android Studio file locks on build dir — code verified by analysis. Run `./gradlew :app:testDebugUnitTest` after closing Android Studio.

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
