# Feature: Troubleshooting & Edge Cases

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Documents known edge cases and how they are handled, plus known unhandled gaps where behavior is undefined or problematic. Use this file when investigating bugs or unexpected behavior.

**What it does NOT do:**
- Does not replace per-feature documentation — cross-reference the relevant feature doc for full context
- Does not cover server-side error handling

---

## Handled Edge Cases

### EncryptedSharedPreferences Keyset Corruption
**File:** `PreferencesManager.kt`
**Trigger:** Android Keystore corruption (e.g., after factory reset, device PIN change on some devices)
**Handling:**
1. Catch the keyset exception
2. Delete the corrupted preferences file
3. Recreate `EncryptedSharedPreferences` with a fresh key
4. User must sign in again (token is lost)

---

### Token Migration on Upgrade
**File:** `PreferencesManager.kt`
**Trigger:** User upgrades from a version that stored the token in plaintext DataStore
**Handling:**
- One-time migration: read plaintext token → write to `EncryptedSharedPreferences` → clear plaintext store
- Idempotent: if migration already ran (plaintext store empty), no-op

---

### Stale Detail Fetch Failure (SyncWorker)
**File:** `SyncWorker.kt`
**Trigger:** A note's detail endpoint returns an error during the concurrent fetch phase
**Handling:**
- Skip the affected item
- Log a warning
- Item will be retried on the next sync cycle (not marked as syncError)

---

### Auth Expired During Sync
**File:** `SyncWorker.kt`
**Trigger:** Server returns 401 mid-sync
**Handling:**
- Clear stored auth token
- Post "auth expired" notification to user
- Return `Result.failure()` — no retry until user re-authenticates

---

### Widget Update Failure
**Files:** `CaptureRepository.kt`, `SyncWorker.kt`
**Trigger:** `WidgetStateUpdater` throws an exception
**Handling:**
- Wrapped in try/catch
- Widget failure is non-blocking — capture and sync operations complete regardless

---

### Biometric Hardware Unavailable
**File:** `BiometricAuthManager.kt`
**Trigger:** Device has no biometric hardware, or hardware is temporarily unavailable
**Handling:**
- `checkCapability()` is called before enforcing lock
- If unavailable: skip biometric lock entirely, allow direct access

---

### FTS Short Query (1–2 Characters)
**File:** `NoteDao.kt`
**Trigger:** User types 1 or 2 characters in the search field
**Handling:**
- FTS4 minimum token length is typically 3 characters
- Queries below threshold fall back to SQL `LIKE '%query%'` search
- User sees results either way; performance may be slightly slower for short queries on large databases

---

### Partial Upload Batch Retry
**File:** `UploadWorker.kt`
**Trigger:** Some notes in a batch upload succeed and some fail transiently
**Handling:**
- Return `Result.success()` — do not retry the whole batch
- Individual failed notes (marked `syncError=true`) retry on the next cycle

---

## Unhandled Gaps

### Conflicting Edits (Concurrent Edit on Both Sides)
**Risk:** High
**Behavior:** Last-write-wins silently overwrites the losing version. No conflict is surfaced to the user. The user may not notice that edits were lost.
**Gap:** No merge UI, no conflict log, no notification.

---

### No Capture Retry Button for Locally-Saved Notes
**Risk:** Low-Medium
**Behavior:** If capture saves locally (offline), the user cannot manually trigger an upload. They must wait for `UploadWorker` to run on its next cycle (up to 15 minutes or next connectivity event).
**Gap:** No "retry now" button in the UI for pending notes.

---

### CancellationException in Secondary Callers
**Risk:** Medium
**Behavior:** `CancellationException` was fixed in `CaptureRepository.kt` and `SyncWorker.kt`, but other `catch(e: Exception)` blocks elsewhere in the codebase may still swallow structured concurrency cancellation, potentially causing zombie coroutines or delayed cancellation.
**Gap:** Audit of all catch-all exception handlers not complete.

---

### WorkManager Orphaned Entries After Reinstall
**Risk:** Low
**Behavior:** Periodic WorkManager jobs survive across app installs on some Android versions. After uninstall+reinstall, orphaned job entries may accumulate in the WorkManager database.
**Gap:** No cleanup or deduplication logic on first launch after install.

---

### FCM Silent Registration Failure
**Risk:** Low-Medium
**Behavior:** If the initial FCM device registration request to the server fails (e.g., server temporarily down during sign-in), the failure is logged but no retry is scheduled. The user will not receive push notifications until the next `onNewToken()` callback or the next sign-in.
**Gap:** No registration retry mechanism.

---

### Empty Capture Text Accepted
**Risk:** Low
**Behavior:** `onCapture()` in `CaptureViewModel` does not check `isNotBlank()` before submitting. An empty note can be created and synced.
**Gap:** No input validation on submission.

---

### Tag List Size Unbounded
**Risk:** Low
**Behavior:** No limit on the number of tags attached to a note. Extremely large tag lists could cause performance issues in JSON serialization or FTS indexing.
**Gap:** No validation or cap on tag count.

---

### Time Picker: No endTime > startTime Validation
**Risk:** Low
**Behavior:** The time picker in the capture toolbar does not enforce that end time is after start time. A note can be saved with an end time earlier than its start time.
**Gap:** No validation in `CaptureToolbar.kt`.

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| Keyset corruption recovery | ✅ PASS | PreferencesManager.kt |
| Token migration on upgrade | ✅ PASS | One-time, idempotent |
| Stale detail fetch skip | ✅ PASS | SyncWorker.kt |
| Auth expired (401) during sync | ✅ PASS | SyncWorker.kt |
| Widget update failure (non-blocking) | ✅ PASS | try/catch in both callers |
| Biometric unavailable skip | ✅ PASS | checkCapability() guard |
| FTS short query fallback | ✅ PASS | NoteDao.kt |
| Partial batch retry success | ✅ PASS | UploadWorker.kt |
| Conflicting edits handling | ❌ FAIL | Silent data loss, no user notification |
| Capture retry button (pending notes) | ⚠️ WARN | No manual retry in UI |
| CancellationException secondary callers | ⚠️ WARN | Partial fix only |
| WorkManager orphan cleanup | ⚠️ WARN | Not addressed |
| FCM registration retry | ⚠️ WARN | No retry mechanism |
| Empty capture text validation | ⚠️ WARN | Not validated |
| Tag list size bound | ⚠️ WARN | Unbounded |
| endTime > startTime validation | ⚠️ WARN | Not enforced |
