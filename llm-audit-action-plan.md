# LLM Audit Action Plan — Inkwell
**Date:** 2026-02-28 | **Overall Score:** 5.4/10 (weighted)

Sorted by: cross-provider confidence → severity

---

## CONFIRMED + CRITICAL (execute immediately)

### #1 — Fix `catch(e: Exception)` → `CancellationException` leakage
- **Confidence:** CONFIRMED (all 3 providers)
- **Severity:** CRITICAL
- **Files:** `data/repository/CaptureRepository.kt`, `sync/SyncWorker.kt`
- **Fix:** In every `catch (e: Exception)` block that falls back to offline/retry, add `if (e is CancellationException) throw e` as the first line. Prefer catching `IOException` + `ClientRequestException` specifically.
- **Status:** ☐ TODO → DONE in this session

### #2 — Fix `sendWithoutRequest` bearer token leakage
- **Confidence:** CONFIRMED (Monica critical, Codex noted)
- **Severity:** CRITICAL
- **File:** `di/NetworkModule.kt`
- **Fix:** Replace `request.url.host != "localhost"` with a strict allowlist. The app only talks to one user-configured server. Use the stored `serverUrl` host for comparison, or maintain a separate unauthenticated client for non-API calls.
- **Status:** ☐ TODO → DONE in this session

### #3 — Add behavioral sync tests (conflict + offline fallback)
- **Confidence:** CONFIRMED (all 3 providers)
- **Severity:** HIGH
- **Files:** `app/src/test/` — add `SyncWorkerConflictTest.kt`, `CaptureRepositoryOfflineTest.kt`
- **Fix:** Use Room in-memory database + mock Ktor client to test: last-write-wins skip, pending-local skip, 401 token-clear path, offline fallback, retry vs permanent failure distinction.
- **Status:** ☐ TODO (deferred — implement after code fixes)

---

## CONFIRMED + HIGH (next sprint)

### #4 — Encrypt auth token storage (EncryptedSharedPreferences)
- **Confidence:** CONFIRMED (Gemini + Monica)
- **File:** `data/local/PreferencesManager.kt`
- **Fix:** Migrate DataStore auth token to Android Keystore-backed `EncryptedSharedPreferences`. If staying on DataStore, wrap with Tink AEAD.

### #5 — Remove `BuildConfig.DEFAULT_AUTH_TOKEN`
- **Confidence:** CONFIRMED (Gemini + Monica)
- **File:** `app/build.gradle.kts:34`
- **Fix:** Delete the `buildConfigField` for `DEFAULT_AUTH_TOKEN`. Use empty string default and require explicit configuration. Token should never be compiled into the APK.

### #6 — Add exponential backoff to SyncWorker
- **Confidence:** CONFIRMED (Codex + Monica)
- **File:** `sync/SyncScheduler.kt`
- **Fix:** Set `WorkRequest.Builder.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)` when scheduling. Prevents tight retry loops during network outages.

---

## CONFIRMED + MEDIUM (backlog)

### #7 — Fix N+1 calls in SyncWorker (Codex full-repo finding)
- **Confidence:** LIKELY (Codex only — full repo access)
- **File:** `sync/SyncWorker.kt`
- **Fix:** Either add a bulk-detail endpoint on the server, or parallelize per-item fetches with `coroutineScope { items.map { async { apiService.getNote(...) } }.awaitAll() }`.

### #8 — Replace `collectAsState()` with `collectAsStateWithLifecycle()`
- **Confidence:** LIKELY (Codex only)
- **Files:** UI composables
- **Fix:** Add `androidx.lifecycle:lifecycle-runtime-compose` dependency. Replace `flow.collectAsState()` with `flow.collectAsStateWithLifecycle()` across all screens.

### #9 — Decompose large composables (CaptureScreen, CaptureToolbar)
- **Confidence:** CONFIRMED (Codex + Monica)
- **Files:** `ui/capture/CaptureScreen.kt` (457L), `ui/capture/CaptureToolbar.kt` (548L)
- **Fix:** Extract stable, stateless composables for the chip row, date picker, tag panel. Pass state via parameters, not by reaching up.

---

## LOW / SINGLE SOURCE (consider for future)

### #10 — Move domain orchestration out of MainActivity
- **Confidence:** LIKELY (Codex only)
- **Fix:** Create a coordinator ViewModel for biometric lock + startup sync trigger.

### #11 — Add conflict detection to sync (replace last-write-wins)
- **Confidence:** CONFIRMED 3/3 (data integrity risk)
- **Severity:** HIGH long-term, LOW urgency if single-user
- **Fix:** On conflict, write a `.conflict` variant or surface a UI diff. For single-user multi-device, consider vector clocks or CRDTs.

### #12 — Add architecture docs / KDoc to public APIs
- **Confidence:** CONFIRMED 3/3 (docs 3.7/10)
- **Fix:** Document sync strategy, token lifecycle, and conflict policy in ARCHITECTURE.md. Add KDoc to repository public methods.
