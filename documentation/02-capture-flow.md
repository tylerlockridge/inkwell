# Feature: Capture Flow

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Provides the primary note capture experience — unified text input with a collapsible metadata toolbar, batch mode for sequential captures, offline fallback with local persistence, and share intent support for capturing from other apps.

**What it does NOT do:**
- Does not validate that capture text is non-empty before submission
- Does not validate that end time is after start time in the time picker
- Does not support rich text / markdown formatting in the capture input

---

## Key Files

| File | Lines | Purpose |
|------|-------|---------|
| `CaptureScreen.kt` | 457 | Main capture UI and input handling |
| `CaptureViewModel.kt` | — | State management, submission logic, offline routing |
| `CaptureToolbar.kt` | 548 | Collapsible metadata panels |

---

## Metadata Toolbar

`CaptureToolbar.kt` provides collapsible panels for each metadata field:

| Panel | Options |
|-------|---------|
| Kind | `one_shot` (default), other task kinds TBD |
| Calendar | Calendar selection options |
| Priority | Priority level options |
| Date/Time | Date picker + time picker (no endTime > startTime validation) |
| Tags | Tag selection/entry |

Smart defaults are persisted across sessions — previously selected values are remembered.

---

## Capture Submission

1. User enters text in the unified input field
2. Metadata toolbar values are attached (kind defaults to `one_shot`)
3. `onCapture()` is called — **no `isNotBlank()` check** on input text (known gap)
4. If network is available: POST to server, receive real UID
5. If network is unavailable: save locally with `pendingSync=true`, assign `"pending_" + UUID` as UID

---

## Offline Fallback

| State | Value |
|-------|-------|
| `pendingSync` | `true` |
| `uid` | `"pending_" + UUID` |
| User feedback | "Saved locally" toast |
| Retry mechanism | `UploadWorker` picks up on next sync cycle |

A pending sync counter is displayed in the UI so users know how many notes are awaiting upload.

---

## Batch Mode

Captures multiple notes in sequence without resetting to a home screen between each. The toolbar state is preserved between batch entries according to smart default persistence.

---

## Share Intent Handling

`ShareIntentParser` parses the `text` and `title` fields from an Android share intent and pre-fills the capture screen. This allows capturing from any app that supports the Android share sheet.

---

## CaptureResult Routing

`CaptureResult` is a sealed class used to route post-capture navigation:
- Success (synced) → appropriate screen
- Success (local) → appropriate screen with pending indicator
- Error → error state handling

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| Unified text input + metadata toolbar | ✅ PASS | |
| Kind / calendar / priority / date / time fields | ✅ PASS | |
| Default kind = one_shot | ✅ PASS | |
| Batch capture mode | ✅ PASS | |
| Smart default persistence across sessions | ✅ PASS | |
| Offline fallback (pendingSync=true + local UID) | ✅ PASS | |
| "Saved locally" toast feedback | ✅ PASS | |
| Pending sync counter in UI | ✅ PASS | |
| Share intent pre-fill | ✅ PASS | ShareIntentParser |
| CaptureResult sealed class routing | ✅ PASS | |
| Empty text validation | ⚠️ WARN | onCapture() does not check isNotBlank() |
| Time picker endTime > startTime validation | ⚠️ WARN | No validation enforced |
