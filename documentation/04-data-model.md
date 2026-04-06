# Feature: Data Model

*Created: 2026-03-02 | Updated: 2026-03-28 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Defines the local persistence layer using Room with FTS4 full-text search. Stores notes with sync state tracking, supports six schema migrations (v1–v6), and uses tolerant JSON deserialization for server schema evolution. As of v5, NoteEntity also persists capture-type metadata (task/note/list/idea), list items, and persistence flags. v6 adds Slice 3 fields: color, pinned, sourceUrl, shared.

**What it does NOT do:**
- Does not expose completed notes in the inbox — status='open' filter is always applied
- Does not bound the tag list size
- Does not store binary attachments

---

## Database

- **ORM:** Room (SQLite)
- **FTS:** FTS4 (full-text search on `title` + `body`)
- **Schema migrations:** v1 → v2 → v3 → v4 → v5 → v6 (exported to git for tracking)
- **Schema directory:** `app/schemas/` in project root

---

## NoteEntity Schema

| Column | Type | Notes |
|--------|------|-------|
| `uid` | String (PK) | Server UID or `"pending_" + UUID` for local-only |
| `title` | String | Note title |
| `body` | String | Note body text |
| `kind` | String | `"one_shot"` (default); task kinds TBD |
| `status` | String | `"open"`, `"completed"` (others possible from server) |
| `priority` | String | Priority level |
| `tags` | String | JSON-encoded array |
| `calendar` | String | Calendar selection |
| `date` | String | Date string |
| `startTime` | String? | Start time |
| `endTime` | String? | End time |
| `source` | String | `"android"`, `"web"`, `"gcal"` |
| `gcalEnabled` | Boolean | Google Calendar sync enabled |
| `gcalEventId` | String? | GCal event ID if pushed |
| `gcalLastPushedAt` | String? | Last GCal push timestamp |
| `clientUuid` | String? | Client-generated UUID for dedup |
| `attachmentUris` | String? | JSON array of local attachment URIs |
| `captureType` | String? | `"task"`, `"note"`, `"list_item"`, `"idea"` — null for legacy/server-only notes |
| `listName` | String? | Name of list (only when captureType = "list_item") |
| `listItemsJson` | String? | JSON array of list items — legacy: `["a","b"]`, structured: `[{"text":"a","checked":true}]` |
| `persistent` | Boolean | Whether the list persists after completion |
| `color` | String? | User-assigned color (hex from curated palette); local-only, server does not return this. Authored at capture time via Extras panel. |
| `pinned` | Boolean | Whether note is pinned; local-only, server does not return this. Authored at capture time via Extras panel. |
| `sourceUrl` | String? | Origin URL if captured from a web page; local-only, server does not return this. Authored at capture time via Extras panel. |
| `shared` | Boolean | Whether note originated from Android share intent (system-derived, not user-editable). Also consumed from server `CaptureMetadata.shared` during sync. |
| `pendingSync` | Boolean | `true` = needs upload to server |
| `syncError` | Boolean | `true` = last upload attempt failed (4xx) |
| `updated` | String | ISO 8601 timestamp |
| `created` | String | ISO 8601 timestamp |

**Indices:** `status`, `pending_sync` — for efficient query performance.

---

## Sync State Matrix

| `pendingSync` | `syncError` | Meaning |
|---------------|-------------|---------|
| `false` | `false` | Synced — up to date with server |
| `true` | `false` | Pending upload |
| `false` | `true` | Upload failed (permanent 4xx error) |

---

## Tags

Tags are stored as a JSON string in the `tags` column. Deserialized with:

```kotlin
Json { ignoreUnknownKeys = true }
```

This makes Inkwell tolerant of server-side tag schema changes — unknown fields are silently ignored rather than causing parse failures.

Tag list size is not bounded (known gap — see Business Rules).

---

## Status Values

| Value | Meaning |
|-------|---------|
| `"open"` | Active, shown in inbox |
| `"completed"` | Done, hidden from inbox |
| Other | Possible from server; handled gracefully |

---

## Full-Text Search (FTS4)

| Query Length | Strategy |
|-------------|----------|
| 3+ characters | FTS4 full-text index on `title` + `body` |
| 1–2 characters | Fallback to SQL `LIKE` query |

FTS4 provides fast token-based search. Short queries fall back to `LIKE` because FTS4 minimum token length is typically 3 characters.

---

## NoteDao Query Rules

All `NoteDao` queries filter to `WHERE status = 'open'`. Completed notes are never returned to the UI from Room queries — they remain in the database but are invisible to the inbox.

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| Room database with FTS4 | ✅ PASS | |
| NoteEntity schema (all columns) | ✅ PASS | |
| pendingSync / syncError state tracking | ✅ PASS | |
| Schema migrations v1→v2→v3→v4→v5→v6 | ✅ PASS | v4: attachments, v5: capture-type metadata, v6: Slice 3 fields |
| Tags as JSON string (ignoreUnknownKeys) | ✅ PASS | Schema evolution tolerant |
| status='open' filter in all NoteDao queries | ✅ PASS | |
| FTS4 with 3-char threshold + LIKE fallback | ✅ PASS | |
| Indices on status + pending_sync | ✅ PASS | |
| Capture-type metadata (captureType, list fields) | ✅ PASS | v5 migration; locally persisted |
| Capture-type in Inbox cards (badge + list preview) | ✅ PASS | Type-aware NoteCard |
| Capture-type in NoteDetail (type-dispatched layout) | ✅ PASS | I3 detail layout adapts by BrowseType |
| ChecklistItem dual-format parser (legacy + structured) | ✅ PASS | I4: 18 unit tests |
| Local checkbox toggle (listItemsJson → Room) | ✅ PASS | I4: NoteDao.updateListItemsJson, local-only |
| Server captureMetadata consumed during sync | ✅ PASS | I7: InboxSyncEngine.resolveCaptureMetadata() |
| Brainstorming idea normalization (task→idea) | ✅ PASS | I7: server "task" + kind "brainstorming" → local "idea" |
| Local checked state preserved during sync | ✅ PASS | I7: merge by item text match |
| Slice 3 request fields (color/pinned/sourceUrl/shared) | ✅ PASS | I7: DTO + network wired; I8: Room columns + migration + UI surfacing |
| Slice 3 local persistence (color/pinned/sourceUrl/shared) | ✅ PASS | I8: v6 migration, sync preservation, capture threading |
| Slice 3 UI surfacing (pinned/shared/sourceUrl/color in detail) | ✅ PASS | I8: detail + inbox indicators |
| Tag list size bound | ⚠️ WARN | Not bounded |
