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

<!-- QUICK-RESUME-UPDATED: 2026-02-27 -->
## Quick Resume
**Last Active:** 2026-02-27
**Current Phase:** Post-separation cleanup — code quality pass complete
**Current Task:** All audit findings addressed + SettingsScreenTest added
**Blockers:** None
**Next Action:** Run instrumented tests on a connected device/emulator (`./gradlew connectedDebugAndroidTest`). All 4 instrumented test classes ready (HiltTestRunner + CaptureScreenTest + InboxScreenTest + SettingsScreenTest). Lint requires network for first-time jar download (hamcrest/javawriter — pre-existing).

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
