# LLM Audit Report — Inkwell Android — 2026-02-26

**Project:** Inkwell (Android capture app for Obsidian vault)
**Focus:** Full Android app codebase health
**Provider:** Codex (GPT-4o) via chatgpt.com/codex
**Scope:** `app/src/main/kotlin/`, `app/src/test/`, `app/src/androidTest/`, `app/build.gradle.kts`, `app/src/main/res/`
**Test run:** Blocked (invalid `org.gradle.java.home` in `gradle.properties` in Codex environment); JVM unit test existence confirmed via file scan

---

## Score Summary

| Dimension | Codex (GPT-4o) |
|-----------|:--------------:|
| Architecture | 8 |
| Code Quality | 7 |
| Testing | 5 |
| Security | 7 |
| Performance | 6 |
| Documentation | 5 |
| **Overall** | **6.3** |

### Finding Counts

| Severity | Count |
|----------|:-----:|
| Critical | 0 |
| High | 0 |
| Medium | 4 |
| Low | 3 |

---

## Dimension Findings

### 1. Architecture — 8/10

**Strengths:**
- Strong modular structure with Hilt DI modules for network/database layers and clear repository abstraction — solid baseline for maintainability and testability
- Background sync architecture is well-defined: centralized WorkManager scheduling with periodic + immediate workers, constraints, and backoff

**Weaknesses:**
- `MainActivity` still carries significant orchestration responsibilities: biometric lock state, lifecycle sync trigger, deep-link/share dispatch — suggests some presentation logic bleeding into the activity layer

---

### 2. Code Quality — 7/10

**Strengths:**
- Generally consistent Kotlin/Compose patterns with state flows and view-model driven updates

**Weaknesses:**
- `CaptureScreen.kt` is a large monolithic UI file containing significant UI + interaction logic along with many hardcoded UI strings (should be in resources)
- Broad exception swallowing in repository paths (`catch (_: Exception)`) hides failure causes and complicates debugging and observability

---

### 3. Testing — 5/10

**Strengths:**
- Good unit test breadth for domain/value logic: URL validation, DTOs, state models, navigation constants

**Weaknesses:**
- No instrumented tests found under `app/src/androidTest` — UI/integration/device behavior coverage is entirely missing
- Several tests are shallow constant checks that don't validate runtime behavior through integration seams (e.g., receiver actions, route constants)

---

### 4. Security — 7/10

**Strengths:**
- Encrypted auth token storage (`EncryptedSharedPreferences`) with migration from plaintext
- URL validation constraining to HTTPS/HTTP schemes
- Network security config blocks cleartext globally (localhost/emulator exceptions only)

**Weaknesses:**
- `allowBackup=true` in manifest may increase data exfiltration surface — encrypted files and databases would be included in ADB/cloud backups
- Default auth token is injected into `BuildConfig` from env/local properties and can be baked into release APKs — token in binary = token in attacker hands

---

### 5. Performance — 6/10

**Strengths:**
- Release build: minify + resource shrinking enabled
- WorkManager constraints and backoff configured — avoids hammering the server on transient failures

**Weaknesses:**
- Widget inbox count is derived by loading all open notes and calling `.size`, rather than a SQL `COUNT(*)` query — scales poorly as inbox grows
- Foreground lifecycle sync trigger runs on each `STARTED` state entry — may add repeated unnecessary network/database work on simple app lifecycle events (e.g., permission dialogs, multi-window)

---

### 6. Documentation — 5/10

**Strengths:**
- `INSTALL.md` provides setup, build, troubleshooting, and FCM configuration notes
- Some inline KDoc exists in sync components (workers/schedulers)

**Weaknesses:**
- KDoc is inconsistent across app layers — repositories, ViewModels, and composables mostly undocumented
- No dedicated architecture doc, test strategy doc, or security model doc beyond install guidance

---

## Top 3 Prioritized Improvements

### P1 — Add instrumented/UI integration tests

Add instrumented tests for core user flows: capture, inbox actions, settings/auth, deep-link handling, share intents, biometric lock. Currently zero coverage for device-level behavior.

**Impact:** Testing, Reliability
**Effort:** High — requires Espresso or Compose UI test setup; can be phased by flow priority

---

### P2 — Refactor large UI/controller units

Split `CaptureScreen.kt` into smaller composables with focused responsibilities. Move `MainActivity` orchestration logic (biometric, sync trigger, deep-link dispatch) into lifecycle-aware managers or ViewModels.

**Impact:** Code Quality, Architecture
**Effort:** Medium — decomposition work; high test confidence needed first

---

### P3 — Harden security defaults

1. Set `allowBackup=false` (or use `fullBackupContent` rules to exclude sensitive files)
2. Remove baked default auth token fallback in release builds — require explicit token provisioning at install time

**Impact:** Security
**Effort:** Low — manifest change + build config adjustment

---

## Codex Run Metadata

| Field | Value |
|-------|-------|
| Provider | Codex (GPT-4o) |
| Runtime | 2m 37s |
| Files produced | `Obsidian-Dashboard/android/INKWELL_ANDROID_AUDIT.md` (+53 lines) |
| Tests executed | Blocked by Gradle JDK config in Codex env; file scan confirmed test structure |
| Repo | `tylerlockridge/claude-projects` @ main |
