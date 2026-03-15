# LLM Audit Action Plan — Inkwell
**Date:** 2026-03-14 | **Overall Score:** 7.0/10 (weighted avg across 3 providers)

Previous audit (2026-02-28): 5.4/10 → **+1.6 improvement**

## Audit Summary

| Provider | Model | Overall | Arch | Quality | Testing | Security | Perf | Docs |
|----------|-------|---------|------|---------|---------|----------|------|------|
| Gemini | Gemini 3.1 | **8.0** | 9 | 9 | 9 | 9 | 8 | 7 |
| Codex #1 | GPT-5.4 | **7.0** | 7 | 7 | 8 | 8 | 6 | 7 |
| Codex #2 | GPT-5.4 | **6.0** | 6 | 6 | 7 | 7 | 6 | 6 |
| **Weighted Avg** | | **7.0** | 7.3 | 7.3 | 8.0 | 8.0 | 6.7 | 6.7 |

Total unique findings: 0 critical, 2 high, 6 medium, 5 low
**All 12 findings resolved in this session.**

---

## Cross-Referenced Findings — ALL RESOLVED

### 2-Provider Consensus (Codex #1 + #2)

| ID | Sev | Title | File | Status |
|----|-----|-------|------|--------|
| **AP-1** | MED | SyncWorker swallows CancellationException at top level | SyncWorker.kt:152 | ✅ FIXED |
| **AP-2** | MED | UploadWorker 401 handling inconsistent with SyncWorker | UploadWorker.kt:65 | ✅ FIXED |
| **AP-3** | LOW | NotificationActionReceiver leaks goAsync() on missing UID | NotificationActionReceiver.kt:35 | ✅ FIXED |

### Single-Provider HIGH (Codex #2)

| ID | Sev | Title | File | Status |
|----|-----|-------|------|--------|
| **AP-4** | HIGH | Online captures with attachments skip multipart path | CaptureRepository.kt:87 | ✅ FIXED |
| **AP-5** | HIGH | InboxRepository.syncInbox() uses string timestamp comparison | InboxRepository.kt:60 | ✅ FIXED |

### Single-Provider MEDIUM

| ID | Sev | Title | File | Status |
|----|-----|-------|------|--------|
| **AP-6** | MED | InboxRepository serial N+1 DB/network lookups | InboxRepository.kt:52 | ✅ FIXED |
| **AP-7** | MED | Widgets display-only despite tap affordances | QuickCaptureWidget.kt / InboxCountWidget.kt | ✅ FIXED |

### LOW Severity

| ID | Sev | Title | File | Status |
|----|-----|-------|------|--------|
| **AP-8** | LOW | Raw Log usage without DEBUG check | CaptureRepository.kt | ✅ FIXED |
| **AP-9** | LOW | WindowInsets(0) ignores system bars | CaptureScreen.kt | ✅ FIXED |
| **AP-10** | LOW | require() throws on invalid URL input | PreferencesManager.kt | ✅ FIXED |
| **AP-11** | LOW | Testing strategy doc stale | 09-testing-strategy.md | ✅ FIXED |
| **AP-12** | LOW | Tests codify known bugs as expected | NotificationActionReceiverTest.kt | ✅ FIXED |

---

## Fix Details

### ✅ AP-1 — CancellationException swallowed (MED, 2x consensus)
Added explicit `catch(CancellationException)` that rethrows before the generic `catch(Exception)` in SyncWorker.doWork(). Updated SyncWorkerIntegrationTest to verify cancellation propagates.

### ✅ AP-2 — UploadWorker 401 silent failure (MED, 2x consensus)
UploadWorker now mirrors SyncWorker on 401: clears stale auth token via `preferencesManager.setAuthToken("")` and posts an auth-expired notification before returning failure.

### ✅ AP-3 — goAsync() leak (LOW, 2x consensus)
NotificationActionReceiver now calls `pendingResult.finish()` before returning when UID is missing. Updated test to verify finish is called.

### ✅ AP-4 — Online attachment capture skips multipart (HIGH)
CaptureRepository.capture() now branches: when `attachmentUris` is non-empty, calls `apiService.captureWithAttachments()` instead of `apiService.capture()`.

### ✅ AP-5 — Timestamp string comparison (HIGH)
InboxRepository.syncInbox() now uses `Instant.parse()` comparison via private `isServerNewer()` helper, matching SyncWorker's approach.

### ✅ AP-6 — InboxRepository serial N+1 (MED)
Replaced serial `noteDao.getByUid()` + `apiService.getNote()` loop with bulk `noteDao.getAllByUids()` lookup + concurrent `coroutineScope { async/awaitAll }` detail fetches. Updated InboxRepositoryTest to mock `getAllByUids()`.

### ✅ AP-7 — Widget click actions (MED)
- QuickCaptureWidget: Added `clickable(actionStartActivity(...))` to both Capture column (→ `obsidiancapture://capture`) and Inbox column (→ `obsidiancapture://inbox`).
- InboxCountWidget: Wired the existing intent to the Column via `clickable(actionStartActivity(intent))`.

### ✅ AP-8 — Raw Log without DEBUG check (LOW)
All `android.util.Log` calls in CaptureRepository now gated on `BuildConfig.DEBUG`. Added proper `Log` import + `TAG` companion.

### ✅ AP-9 — WindowInsets(0) (LOW)
Removed `contentWindowInsets = WindowInsets(0)` from CaptureScreen's Scaffold — now uses default Scaffold inset handling.

### ✅ AP-10 — require() throws on invalid URL (LOW)
`PreferencesManager.setServerUrl()` now returns `Boolean` instead of throwing `IllegalArgumentException`. SettingsViewModel checks the return value and shows snackbar on failure.

### ✅ AP-11 — Testing strategy doc stale (LOW)
Updated `documentation/09-testing-strategy.md`: test counts (36 unit / 4 instrumented / 294+ tests), test stack (added Robolectric, Turbine), coverage table updated with all 36 test files, known gaps updated.

### ✅ AP-12 — Tests codify known bugs (LOW)
- NotificationActionReceiverTest: Updated to verify `finish()` IS called on missing UID (was asserting it was NOT called).
- SyncWorkerIntegrationTest: Updated to verify CancellationException propagates (was asserting it was caught and retried).

---

## Quality Gates
- `./gradlew test`: ✅ 294/294 passing
- `./gradlew lint`: ✅ Clean
