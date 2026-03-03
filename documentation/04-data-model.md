# Feature: Data Model

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Defines the local persistence layer using Room with FTS4 full-text search. Stores notes with sync state tracking, supports three schema migrations, and uses tolerant JSON deserialization for server schema evolution.

**What it does NOT do:**
- Does not expose completed notes in the inbox — status='open' filter is always applied
- Does not bound the tag list size
- Does not store binary attachments

---

## Database

- **ORM:** Room (SQLite)
- **FTS:** FTS4 (full-text search on `title` + `body`)
- **Schema migrations:** v1 → v2 → v3 (exported to git for tracking)
- **Schema directory:** `schema/` in project root

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
| `time` | String | Time string |
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
| Schema migrations v1→v2→v3 | ✅ PASS | Exported to git |
| Tags as JSON string (ignoreUnknownKeys) | ✅ PASS | Schema evolution tolerant |
| status='open' filter in all NoteDao queries | ✅ PASS | |
| FTS4 with 3-char threshold + LIKE fallback | ✅ PASS | |
| Indices on status + pending_sync | ✅ PASS | |
| Tag list size bound | ⚠️ WARN | Not bounded |
