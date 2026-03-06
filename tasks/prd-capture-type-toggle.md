# PRD: Inkwell Capture Type Toggle

## Introduction

Nexus now supports three capture types — Task, Note, and List — via the Chrome extension and web form. Inkwell (the Android app) still only sends tasks. This PRD adds a 3-way capture type toggle to Inkwell so mobile captures route correctly, matching the Chrome extension and web form behavior.

**Backend is already done.** The server `POST /api/capture` already accepts `captureType: "task" | "note" | "list_item"` and routes accordingly. This is purely an Inkwell UI + DTO change.

## Goals

- Add Task / Note / List toggle to the Inkwell capture screen
- Show conditional fields based on capture type (matching Chrome extension behavior)
- Send `captureType` field in API requests
- Note mode: simplified fields (no priority, no due date, no calendar)
- List mode: list name + items entry
- Maintain backward compatibility (default to task if toggle not set)

## User Stories

### US-001: Add captureType to CaptureRequest DTO
**Description:** As a developer, I need the API request to include the capture type field.

**Acceptance Criteria:**
- [ ] Add `captureType: String? = null` field to `CaptureRequest` data class
- [ ] Field is serialized as `captureType` in JSON (matches server expectation)
- [ ] When null or "task", server behavior is unchanged (backward compatible)
- [ ] Typecheck passes (./gradlew compileDebugKotlin)

### US-002: Add CaptureType to ViewModel state
**Description:** As a developer, I need the ViewModel to track the selected capture type and adjust form behavior.

**Acceptance Criteria:**
- [ ] Add `CaptureType` enum: `TASK`, `NOTE`, `LIST` to capture UI package
- [ ] Add `captureType: CaptureType` field to `CaptureUiState` (default `TASK`)
- [ ] Add `onCaptureTypeChange(type: CaptureType)` function to `CaptureViewModel`
- [ ] When type is NOTE: clear priority, date, startTime, endTime, calendar fields
- [ ] When type is LIST: track `listName: String` and `listItems: String` in state
- [ ] Add `onListNameChange(name: String)` and `onListItemsChange(items: String)` to ViewModel
- [ ] `onCapture()` builds CaptureRequest with correct `captureType` value: "task", "note", or "list_item"
- [ ] For LIST captures: set `body` to items text, set request fields `listName` and `items` (split by newline)
- [ ] For NOTE captures: set `captureType = "note"` in request
- [ ] Tests for ViewModel state transitions between capture types
- [ ] Typecheck passes

### US-003: Capture type toggle UI
**Description:** As a user, I want to select Task/Note/List at the top of the capture screen.

**Acceptance Criteria:**
- [ ] 3-segment M3 `SegmentedButton` row at top of CaptureScreen (above the text field): Task / Note / List
- [ ] Task selected (default): show current form unchanged (kind, tags, priority, date, calendar in SmartToolbar)
- [ ] Note selected: hide priority, date/time, calendar from SmartToolbar. Show only tags and kind selector
- [ ] List selected: replace main text field with two fields: List Name (single line) and Items (multiline, one per line). Hide SmartToolbar metadata panels except tags
- [ ] Selected segment uses MaterialTheme.colorScheme.primary highlight
- [ ] Toggle persists during the session but resets to Task on new launch
- [ ] Switching type preserves text content where possible (body text carries over between Task and Note)
- [ ] Verify on device (build and install APK)
- [ ] Typecheck passes

### US-004: List-specific capture fields
**Description:** As a user, I want to enter a list name and items when capturing a list from my phone.

**Acceptance Criteria:**
- [ ] When LIST is selected, the main content area shows: a "List Name" OutlinedTextField (single line) and an "Items (one per line)" OutlinedTextField (multiline, min 4 lines)
- [ ] Persistent toggle: Switch labeled "Keep list" (default off = ephemeral, 24h auto-archive)
- [ ] Submit validation: list name and at least one item required, show snackbar error if empty
- [ ] CaptureRequest for list: `captureType = "list_item"`, `body = items joined by newline`, plus `listName` and `items` fields
- [ ] After successful capture, both fields clear
- [ ] Typecheck passes

### US-005: Note-specific capture adjustments
**Description:** As a user, when I select Note mode, I want a simplified capture experience focused on reference material.

**Acceptance Criteria:**
- [ ] In NOTE mode, SmartToolbar hides: priority panel, date/time panel, calendar selector
- [ ] SmartToolbar shows: tags panel and kind selector (but kind defaults to "note" and is not changeable)
- [ ] Capture button label changes to "Save Note" (instead of "Capture")
- [ ] CaptureRequest for notes includes `captureType = "note"` and `kind = "note"`
- [ ] Source field remains "android"
- [ ] Typecheck passes

## Non-Goals

- No color picker for notes on mobile (Chrome extension has this; deferred for Inkwell)
- No pin toggle on mobile (can be done via web UI later)
- No list management UI (viewing/editing existing lists) — Inkwell is capture-focused
- No offline queue changes (existing queue handles all capture types the same way)

## Technical Considerations

- **SegmentedButton** requires Material3 1.2+ (already in Inkwell's dependencies)
- **CaptureRequest DTO**: Add fields as nullable with defaults to maintain backward compatibility
- **kotlinx.serialization**: New fields must be annotated correctly for JSON serialization
- **Share intent**: When text is shared to Inkwell from another app, default to Task mode (existing behavior)
- **Persistent toggle for lists**: Add `persistent: Boolean? = null` to CaptureRequest

## Success Metrics

- Capture type toggle adds < 0.5 seconds to capture flow
- List captures create checklist files in `Inbox/Lists/` on server
- Note captures create reference files in `Inbox/Notes/` on server
- All existing task capture tests continue to pass
