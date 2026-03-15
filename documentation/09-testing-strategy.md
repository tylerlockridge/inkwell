# Feature: Testing Strategy

*Created: 2026-03-02 | Updated: 2026-03-14 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Covers unit testing of the data, repository, and sync worker layers plus instrumented UI tests for the three primary screens. Quality gates (lint, Kotlin compiler, test runner) all pass as of the 2026-03-14 audit.

**What it does NOT do:**
- Does not test conflict resolution scenarios (last-write-wins edge cases)
- Does not test biometric lock timeout behavior
- Does not have full end-to-end integration tests

---

## Test Counts

| Type | Count |
|------|-------|
| Unit test files | 36 |
| Instrumented test files | 4 |
| **Total test files** | **40** |
| Unit tests passing | 294+ |
| Instrumented tests passing | 17 |
| Estimated coverage | ~45% |

---

## Test Stack

| Tool | Purpose |
|------|---------|
| JUnit 4 | Test runner and assertions |
| MockK 1.13.13 | Kotlin-idiomatic mocking |
| Robolectric 4.14.1 | Android framework simulation in unit tests |
| Turbine 1.2.0 | Flow testing |
| Hilt testing | DI in instrumented tests |
| Compose testing | UI assertions and interactions |
| Room in-memory DB | Isolated DAO testing |
| WorkManager testing | Worker unit tests |

---

## Unit Test Coverage

| Area | Test Files | Notes |
|------|-----------|-------|
| `NoteEntity` | NoteEntityTest | Data class, JSON serialization |
| `NoteDao` / FTS | FtsSearchTest | CRUD, filter by status, FTS queries |
| `CaptureRepository` | CaptureResultTest | Offline routing, pendingSync state |
| `InboxRepository` | InboxRepositoryTest | Note list retrieval, sync result types |
| `SyncWorker` | SyncWorkerResultTest, SyncWorkerIntegrationTest | Pull logic, conflict resolution, retry, cancellation |
| `UploadWorker` | UploadWorkerErrorTest, UploadWorkerLogicTest | Push routing, error classification, batch handling |
| `SyncScheduler` | SyncSchedulerTest | Schedule/cancel/trigger logic |
| `SyncConflict` | SyncConflictTest | Timestamp comparison, Instant.parse semantics |
| Auth / Biometric | BiometricAuthManagerTest, BuildConfigTest | BIOMETRIC_STRONG, token config |
| DTOs | ApiDtoTest, CaptureRequestTest, GoogleAuthDtoTest, DeviceRegistrationTest | Serialization roundtrips |
| UI State | CaptureUiStateTest, InboxUiStateTest, SettingsUiStateTest, NoteDetailUiStateTest | State defaults, transitions |
| Navigation | NavigationTest, DeepLinkTest, ScreenTest | Route parsing, deep link handling |
| Notifications | NotificationActionReceiverTest, NotificationChannelsTest, CaptureMessagingServiceTest, CaptureMessagingTokenTest | Action routing, channel config |
| Widgets | WidgetStateTest | State updater, widget instantiation |
| Misc | ShareIntentParserTest, MarkdownParserTest, PreferencesDefaultsTest, UrlValidationTest, NoteExportTest, CoachMarkManagerTest, CaptureTypeIdeaTest | Parser logic, validation, export |

---

## Instrumented Tests

| Test | Screen | Tests |
|------|--------|-------|
| `CaptureScreenTest` | Capture UI interactions | 6 |
| `InboxScreenTest` | Inbox list display | 5 |
| `SettingsScreenTest` | Settings read/write | 6 |
| `HiltTestRunner` | Hilt component initialization | — |

**Android 16 notes:**
- Always uninstall release APK before running `connectedAndroidTest` (signature mismatch)
- Runtime permissions must be pre-granted via `GrantPermissionRule` (camera, media, notifications)
- `Espresso.closeSoftKeyboard()` deadlocks on Android 16 — use `composeRule.waitForIdle()` instead

---

## Quality Gates (as of 2026-03-14)

| Gate | Status |
|------|--------|
| `./gradlew test` | PASS (294+ tests) |
| `./gradlew lint` | PASS |
| Kotlin compiler | PASS |

---

## Known Test Gaps

| Gap | Risk | Status |
|-----|------|--------|
| Conflict resolution (last-write-wins edge cases) | High — silent data loss | Partial (SyncConflictTest covers timestamps) |
| Biometric lock timeout (60s) | Low — timing-based, hard to unit test | Open |
| Widget click actions | Low — requires device | Open |
| Coverage >= 80% | Medium — at ~45% estimated | Open |

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| 36 unit test files (294+ tests) | PASS | Data, repository, worker, auth, navigation, notifications |
| 4 instrumented test files (17 tests) | PASS | Capture, Inbox, Settings, HiltRunner |
| JUnit 4 + MockK + Robolectric + Turbine + Hilt + Compose | PASS | Full test stack |
| Room in-memory DB for DAO tests | PASS | |
| ./gradlew test passing | PASS | |
| ./gradlew lint passing | PASS | |
| Kotlin compiler clean | PASS | |
| Coverage >= 80% | TODO | ~45% estimated |
