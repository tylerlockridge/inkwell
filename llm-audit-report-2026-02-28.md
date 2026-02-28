# LLM Audit Report — Inkwell Android App

**Date:** 2026-02-28
**Providers:** Codex (GPT-4o, full repo) + Gemini 3 Pro + GPT-5.2 (Monica)
**Mode:** Combined — Codex async + deep browser (Gemini + Monica)
**Tool:** llm-audit-v2 (Claude-in-Chrome)
**Codex task:** https://chatgpt.com/codex/tasks/task_e_69a31322031483288e6f3cf5557581ba

---

## Score Comparison

| Dimension     | Codex (1.5x) | Gemini | Monica | Weighted Avg |
|---------------|-------------|--------|--------|-------------|
| Architecture  | 7           | 8      | 7      | **7.3**     |
| Code Quality  | 6           | 6      | 5      | **5.7**     |
| Testing       | 5           | 4      | 4      | **4.4**     |
| Security      | 7           | 5      | 2      | **5.0**     |
| Performance   | 6           | 7      | 6      | **6.3**     |
| Documentation | 4           | 4      | 3      | **3.7**     |
| **Overall**   | **6**       | **6**  | **4.5**| **5.4**    |

> Weighted formula: (Codex × 1.5 + Gemini × 1.0 + Monica × 1.0) / 3.5
> Security divergence: Codex scored 7 (may have seen EncryptedSharedPreferences in actual code); Monica scored 2 (flagged sendWithoutRequest as credential exfiltration). Both findings are valid.

---

## Cross-Provider Findings

### CONFIRMED (2+ providers)

#### 1. `catch(e: Exception)` Swallows `CancellationException` — CRITICAL (all 3 providers)
**Files:** `data/repository/CaptureRepository.kt`, `sync/SyncWorker.kt`

All three providers independently flagged this as a top concern. `catch (e: Exception)` in Kotlin also catches `CancellationException`, which breaks structured concurrency. This causes:
- WorkManager workers that don't stop when requested
- ViewModel coroutine scopes with zombie jobs
- Retry logic that continues after the coroutine was cancelled
- Memory leaks from abandoned continuations

**Fix:** Catch specific exceptions (`IOException`, `HttpException`, Ktor's `ClientRequestException`) or rethrow CancellationException explicitly:
```kotlin
catch (e: Exception) {
    if (e is CancellationException) throw e  // minimum fix
    saveOffline(request, clientUuid, now)
}
```

#### 2. Last-Write-Wins Sync = Silent Data Loss — HIGH (all 3 providers)
**File:** `sync/SyncWorker.kt` — `if (localNote.updated >= item.updated_at) continue`

If a user edits a note in Obsidian on desktop, then makes a different offline edit on the phone, the phone's later timestamp overwrites the desktop change silently. No conflict is surfaced.

#### 3. Auth Token in Plaintext DataStore — HIGH (Gemini + Monica)
**File:** `data/local/PreferencesManager.kt`

DataStore does not encrypt. Bearer tokens should be stored with Android Keystore-backed encryption (EncryptedSharedPreferences or Tink). Note: Codex may have found EncryptedSharedPreferences already in use in some paths.

#### 4. `BuildConfig.DEFAULT_AUTH_TOKEN` Secrets Exposure — HIGH (Gemini + Monica)
**File:** `app/build.gradle.kts` (line 34)

Even sourced from `local.properties`, compiling a token into BuildConfig:
- Embeds it in the APK (extractable via `apktool`)
- Can be accidentally committed via CI artifact logs
- Trains the codebase toward insecure defaults

#### 5. Low Test Coverage / Shallow Tests — HIGH (all 3 providers)
**Files:** `app/src/test/`, `app/src/androidTest/`

28 unit tests + 4 instrumented for 68 source files. Codex specifically noted that many unit tests are structural smoke checks (e.g., `UploadWorkerLogicTest` checks string prefix logic, not worker behavior against a fake DAO). Critical sync paths — conflict resolution, offline fallback, 401 handling — lack behavioral coverage.

#### 6. No Exponential Backoff in SyncWorker — MEDIUM (Codex + Monica)
**File:** `sync/SyncWorker.kt`

`Result.retry()` without backoff configuration risks tight retry loops, battery drain, and network churn. WorkManager supports `BackoffPolicy.EXPONENTIAL`.

#### 7. Large UI Composables Risk Recomposition — MEDIUM (Codex + Monica)
**Files:** `ui/capture/CaptureScreen.kt` (457L), `ui/capture/CaptureToolbar.kt` (548L)

Files this large suggest composable function definitions that mix state orchestration + UI + side-effects. Large unstable lambdas trigger unnecessary recompositions.

---

### LIKELY — Codex Only (full repo access)

#### 8. N+1 Network Calls in SyncWorker — MEDIUM
**File:** `sync/SyncWorker.kt`

`syncInbox()` calls `getInbox()` to get a list, then calls `getNote(serverUrl, item.uid)` for each item. For a user with 50 inbox items, this is 51 sequential network calls per sync cycle. On slow networks this is very expensive.

**Fix:** Add a bulk-fetch endpoint on the server, or batch into parallel coroutines with `async {}` + `awaitAll()`.

#### 9. `collectAsState()` vs Lifecycle-Aware Collection — LOW
**Files:** UI composables

`collectAsState()` doesn't respect the Android lifecycle. Prefer `collectAsStateWithLifecycle()` (from `androidx.lifecycle:lifecycle-runtime-compose`) to stop collection when the app is backgrounded, saving CPU/battery.

#### 10. `MainActivity` Orchestrates Domain Logic — LOW
**File:** `MainActivity.kt`

Codex noted `MainActivity` directly injects and calls repository sync + biometric auth orchestration, pushing domain flows into the Activity layer. These should live in a coordinator ViewModel.

---

## Strengths

- **Modern stack executed well:** Hilt DI across app + WorkManager (HiltWorker + Configuration.Provider), Room with proper migrations (v1→2→3), FTS4 with triggers for search. All three providers praised this.
- **Offline-first design:** Sealed `CaptureResult.Online/Offline/Error` with clean fallback path is excellent for a capture tool.
- **Release build hardening:** `isMinifyEnabled=true`, `isShrinkResources=true`, `exportSchema=true` for Room.
- **WorkManager 401 handling:** Clears stale token and posts notification — operationally correct.
- **StateFlow + `.update {}` pattern:** Prevents race conditions in ViewModel state mutations.

---

## Risks in Production

1. **Credential exfiltration** via permissive `sendWithoutRequest { host != "localhost" }` — bearer token sent to any non-localhost host (Monica: critical severity)
2. **Silent data loss** from last-write-wins sync with no conflict surfacing
3. **Hard-to-debug sync freezes** from swallowed `CancellationException` in both repository and worker
4. **Token compromise** via plaintext DataStore + BuildConfig compilation
5. **Battery drain** from non-backoff WorkManager retries during network outages

---

## JSON Score Block

```json
{"date":"2026-02-28","provider":"combined","providers":{"codex":{"overall":6,"scores":{"architecture":7,"codeQuality":6,"testing":5,"security":7,"performance":6,"documentation":4}},"gemini":{"overall":6,"scores":{"architecture":8,"codeQuality":6,"testing":4,"security":5,"performance":7,"documentation":4}},"monica":{"overall":5,"scores":{"architecture":7,"codeQuality":5,"testing":4,"security":2,"performance":6,"documentation":3}}},"scores":{"architecture":7,"codeQuality":6,"testing":4,"security":5,"performance":6,"documentation":4},"overall":5,"weightedOverall":5.4}
```
