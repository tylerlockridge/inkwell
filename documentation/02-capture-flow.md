# Feature: Capture Flow

*Created: 2026-03-02 | Updated: 2026-03-28 | Project: Inkwell*

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
3. Send button is **disabled** when capture is invalid (see Capture Validity Rule below)
4. `onCapture()` validates again and shows snackbar "Add some content to capture" if invalid
5. If network is available: POST to server, receive real UID
6. If network is unavailable: save locally with `pendingSync=true`, assign `"pending_" + UUID` as UID

---

## Capture Validity Rule (I13)

A capture is valid when it contains **meaningful user content**:

| Capture Type | Valid When |
|-------------|-----------|
| Task / Note / Idea | `unifiedText.isNotBlank()` OR `sourceUrl.isNotBlank()` OR `selectedAttachments.isNotEmpty()` |
| List | `listName.isNotBlank()` AND at least one non-blank line in `listItems` |

**What counts as content:**
- Any non-whitespace text (title and/or body)
- A source URL (e.g., from share intent)
- At least one attachment (photo, document)

**What does NOT count:**
- Cosmetic metadata alone: pinned, color, tags, priority, calendar, kind, date/time
- Whitespace-only text
- Empty list items (blank lines only)

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

### URL extraction (I11)
If the shared text contains an `https://` or `http://` URL, the first match is extracted as `sourceUrl`:
- **URL-only text** (e.g., Chrome sharing a link): URL goes to `sourceUrl`, body gets title only (avoids duplication)
- **Prose + URL** (e.g., tweet with link): URL goes to `sourceUrl`, full text preserved in body (URL stays in context)
- **No URL**: `sourceUrl` stays empty, user can add manually via Extras panel
- **Multiple URLs**: first one becomes `sourceUrl`, all remain in body

### `shared` flag (I11)
When a capture originates from a share intent, `shared = true` is set automatically.
- **Canonical meaning**: "this note was captured from an external app via Android share sheet"
- Server may also set `shared` via `CaptureMetadata.shared` during sync
- Not user-editable — system-derived only

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
| Share intent URL extraction → sourceUrl | ✅ PASS | I11: first http/https URL auto-fills sourceUrl |
| Share intent → shared=true | ✅ PASS | I11: system-derived, not user-editable |
| CaptureResult sealed class routing | ✅ PASS | |
| Capture validity rule (I13) | ✅ PASS | Send disabled when invalid; snackbar fallback; 28 unit tests |
| Time picker endTime > startTime validation | ⚠️ WARN | No validation enforced |
