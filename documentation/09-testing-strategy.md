# Feature: Testing Strategy

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Covers unit testing of the data, repository, and sync worker layers plus instrumented UI tests for the three primary screens. Quality gates (lint, Kotlin compiler, test runner) all pass as of the 2026-03-02 audit.

**What it does NOT do:**
- Does not test conflict resolution scenarios
- Does not test FCM notification routing or `NotificationActionReceiver`
- Does not test deep link routing
- Does not test widget state updates
- Does not test biometric lock timeout behavior

---

## Test Counts

| Type | Count |
|------|-------|
| Unit tests | 30 |
| Instrumented tests | 4 |
| **Total** | **34** |
| Estimated coverage | ~30% |

---

## Test Stack

| Tool | Purpose |
|------|---------|
| JUnit 4 | Test runner and assertions |
| MockK | Kotlin-idiomatic mocking |
| Hilt testing | DI in instrumented tests |
| Compose testing | UI assertions and interactions |
| Room in-memory DB | Isolated DAO testing |

---

## Unit Test Coverage

| Area | Tests |
|------|-------|
| `NoteEntity` | Data class correctness |
| `NoteDao` | CRUD, filter by status, FTS queries |
| `CaptureRepository` | Offline routing, pendingSync state |
| `InboxRepository` | Note list retrieval, search |
| `SyncWorker` | Pull logic, conflict resolution, retry policy |
| `UploadWorker` | Push routing, error classification, batch handling |
| Auth (token storage/retrieval) | EncryptedSharedPreferences read/write |

---

## Instrumented Tests

| Test | Screen |
|------|--------|
| `CaptureScreenTest` | Capture UI interactions |
| `InboxScreenTest` | Inbox list display |
| `SettingsScreenTest` | Settings read/write |
| `HiltTestRunner` | Hilt component initialization in test env |

---

## Quality Gates (as of 2026-03-02)

| Gate | Status |
|------|--------|
| `./gradlew test` | ✅ PASS |
| `./gradlew lint` | ✅ PASS |
| Kotlin compiler | ✅ PASS |

---

## Known Test Gaps

| Gap | Risk |
|-----|------|
| Conflict resolution (last-write-wins edge cases) | High — silent data loss |
| FCM notification routing | Medium — affects push reliability |
| `NotificationActionReceiver` | Medium — background actions untested |
| Deep link routing | Medium — notification taps |
| Widget state update logic | Low — non-blocking, isolated |
| Biometric lock timeout (60s) | Low — timing-based, hard to unit test |

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| 30 unit tests | ✅ PASS | Data, repository, worker, auth |
| 4 instrumented tests | ✅ PASS | Capture, Inbox, Settings, HiltRunner |
| JUnit 4 + MockK + Hilt + Compose test stack | ✅ PASS | |
| Room in-memory DB for DAO tests | ✅ PASS | |
| ./gradlew test passing | ✅ PASS | |
| ./gradlew lint passing | ✅ PASS | |
| Kotlin compiler clean | ✅ PASS | |
| Coverage >= 80% | ❌ FAIL | ~30% estimated |
| Conflict resolution tests | 🔲 TODO | |
| FCM / NotificationActionReceiver tests | 🔲 TODO | |
| Deep link routing tests | 🔲 TODO | |
| Widget state tests | 🔲 TODO | |
| Biometric timeout tests | 🔲 TODO | |
