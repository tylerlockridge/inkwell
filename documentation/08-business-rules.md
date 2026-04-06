# Feature: Business Rules Reference

*Created: 2026-03-02 | Updated: 2026-03-28 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Documents the authoritative business rules that govern Inkwell's behavior. These rules define routing decisions, error handling policy, data constraints, and sync behavior. When code and this document conflict, investigate the code — this document reflects the audited state as of 2026-03-02.

**What it does NOT do:**
- Does not define UI/UX design rules (see `06-ui-architecture.md`)
- Does not define infrastructure or deployment constraints (see `11-deployment-release.md`)

---

## Sync Rules

**Rule 1 — Offline UID routing:**
- `uid` starts with `"pending_"` → POST `/api/capture` (new note, no server record yet)
- `uid` is a real server UID → PATCH `/api/note/:uid` (update existing record)

**Rule 2 — Conflict resolution (last-write-wins):**
```
if (localNote.updated >= item.updated_at) → skip server update (keep local)
else → apply server update to Room
```
Risk: Silent data loss if both sides were edited concurrently. No conflict is surfaced to the user.

**Rule 3 — 401 policy:**
- Clear stored auth token
- Post "auth expired" notification to the user
- Return `Result.failure()` — no retry, no further sync until user re-authenticates

**Rule 4 — 4xx policy (non-401):**
- Mark `syncError=true` on the affected note
- Continue processing the rest of the batch (don't abort)

**Rule 5 — 5xx / network error policy:**
- Return `Result.retry()` — WorkManager reschedules with exponential backoff

**Rule 6 — Partial batch success:**
- If any items in the batch succeed → return `Result.success()`
- Do not retry the whole batch; individual failed items retry on the next cycle

**Rule 7 — SyncWorker full retry condition:**
- Retry the entire pull only if ALL items failed AND at least one item exists
- If zero items: return success (empty inbox is valid)

---

## Security Rules

**Rule 8 — Biometric re-lock timeout:**
- App locks after 60 seconds in background
- Constant: `LOCK_TIMEOUT_MS = 60_000L` (hard-coded, not user-configurable)
- Lock is skipped if `BiometricAuthManager.checkCapability()` returns unavailable

**Rule 9 — HTTPS enforcement:**
- HTTPS required for all server communication
- HTTP permitted only for `localhost` and `10.0.2.2` (emulator/dev only)

---

## Data Rules

**Rule 10 — Inbox status filter:**
- Inbox always shows only `status='open'` notes
- Completed notes (`status='completed'`) are never returned by `NoteDao` queries

**Rule 11 — Sync interval minimum:**
- WorkManager enforces a 15-minute minimum periodic interval
- User-configured values below 15 minutes are coerced to 15 minutes

**Rule 12 — Default capture kind:**
- All new captures default to `kind = "one_shot"` unless the user explicitly changes it in the toolbar

**Rule 13 — Tag serialization tolerance:**
- Tags stored as JSON string in Room
- Deserialized with `Json { ignoreUnknownKeys = true }`
- Unknown tag fields from server are silently ignored (schema evolution safety)

**Rule 14 — Capture validity (I13):**
- Task/Note/Idea: valid if text is non-blank OR sourceUrl is non-blank OR attachments are present
- List: valid if listName is non-blank AND at least one non-blank item line exists
- Cosmetic metadata alone (pinned, color, tags, priority, calendar, kind, date) does NOT make a capture valid
- Send button is disabled when invalid; snackbar "Add some content to capture" if onCapture() is called while invalid
- 28 unit tests cover all edge cases (whitespace-only, URL-only, attachment-only, metadata-only, list with blank items)

**Rule 15 — Pinned-first inbox ordering (I15):**
- All inbox queries order by `pinned DESC, created DESC`
- Pinned notes always appear above unpinned notes within the same view
- Among pinned notes: newest first. Among unpinned notes: newest first.
- Applies to: main inbox, pending tab, LIKE search, FTS search
- In-memory tab filtering (by BrowseType) preserves the pinned-first ordering
- 10 unit tests in `PinnedSortingTest`

**Rule 16 — FTS search threshold:**
- Queries of 3+ characters: use FTS4 full-text index
- Queries of 1–2 characters: fall back to SQL `LIKE` query

---

## Browse / Navigation Rules

**Rule 17 — Inbox type classification (BrowseType):**
1. If `captureType` is non-null: map directly (`"task"` → TASK, `"note"` → NOTE, `"list_item"` → LIST, `"idea"` → IDEA)
2. Else if `listName` or `listItemsJson` is non-blank → LIST
3. Else if `kind == "note"` → NOTE
4. Else if `kind == "brainstorming"` → IDEA
5. Otherwise → TASK

This heuristic exists because the server detail API does not return `captureType`. Locally captured notes have explicit types; synced-only notes fall back to steps 2–5.

**Rule 18 — Inbox tab model:**
- 6 tabs: All, Tasks, Notes, Lists, Ideas, Pending
- Type tabs (Tasks/Notes/Lists/Ideas) filter the inbox Flow in-memory using BrowseType
- Search applies within the selected tab's type filter (All/Pending search the full set)
- Tab counts are derived from a single `getInboxNotes()` Flow grouped by BrowseType

**Rule 19 — Review queue removed:**
- The old `Review` tab (kind != 'one_shot') was replaced by explicit type tabs in I2
- `NoteDao.getReviewQueue()` and `InboxRepository.getReviewQueue()` were removed

**Rule 20 — Type-dispatched detail views:**
- Detail screen dispatches to one of four layouts based on `browseType`:
  - **Task:** body → tags → schedule (date/time/calendar) → status (priority, kind, source, GCal, sync)
  - **Note:** body (prominent, 10-line edit) → tags → compact details (source, kind, sync)
  - **List:** list header (name, persistent) → read-only checklist items → body → tags → compact details
  - **Idea:** "Brainstorm / Idea" label → body (prominent, 8-line edit) → tags → compact details
- Title, body, and tags remain editable across all types (NoteUpdateRequest supports these)
- Schedule, priority, captureType, list items, and persistent are read-only (no server API support)

**Rule 21 — Local checklist interaction:**
- List detail checkboxes are tappable. Toggling updates local state immediately (optimistic UI).
- Checked state persists in Room via `NoteDao.updateListItemsJson()`. Does NOT mark `pending_sync`.
- Storage format: dual-format `listItemsJson` column — legacy `["a","b"]` reads as all unchecked; structured `[{"text":"a","checked":true}]` written on first toggle.
- Sync preservation: `InboxSyncEngine` carries forward local `listItemsJson` during sync overwrites (server detail API does not return list items).
- Visual: checked items show strikethrough + dimmed text. Header shows `N/M done` progress.
- Cue: "Checklist state saved locally" label below items.

---

## Status

| Rule | Status | Notes |
|------|--------|-------|
| Rule 1: Offline UID routing | ✅ PASS | |
| Rule 2: Last-write-wins conflict resolution | ⚠️ WARN | Silent data loss risk |
| Rule 3: 401 policy | ✅ PASS | |
| Rule 4: 4xx → syncError, continue batch | ✅ PASS | |
| Rule 5: 5xx/network → Result.retry() | ✅ PASS | |
| Rule 6: Partial batch success | ✅ PASS | |
| Rule 7: SyncWorker retry condition | ✅ PASS | |
| Rule 8: Biometric re-lock (60s) | ✅ PASS | Hard-coded |
| Rule 9: HTTPS enforcement | ✅ PASS | |
| Rule 10: Inbox status='open' filter | ✅ PASS | |
| Rule 11: Sync interval minimum (15 min) | ✅ PASS | WorkManager coerced |
| Rule 12: Default kind = one_shot | ✅ PASS | |
| Rule 13: Tag JSON with ignoreUnknownKeys | ✅ PASS | |
| Rule 14: Capture validity (I13) | ✅ PASS | 28 unit tests; send disabled when invalid |
| Rule 15: Pinned-first inbox ordering (I15) | ✅ PASS | 10 unit tests; 4 SQL queries updated |
| Rule 16: FTS threshold (3 chars) | ✅ PASS | |
| Rule 17: BrowseType classification heuristic | ✅ PASS | 16 unit tests |
| Rule 18: 6-tab inbox model | ✅ PASS | ScrollableTabRow |
| Rule 19: Review queue removed | ✅ PASS | Replaced by type tabs |
| Rule 20: Type-dispatched detail views | ✅ PASS | I3: layout adapts per BrowseType |
| Rule 21: Local checklist interaction | ✅ PASS | I4: checkbox toggle, dual-format parse, 18 tests |
