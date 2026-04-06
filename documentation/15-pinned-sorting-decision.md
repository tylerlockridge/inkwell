# Pinned Sorting Decision Packet

*Created: 2026-03-28 | Project: Inkwell | Status: **Option A implemented (I15)***

---

## Current Behavior

**Pinned field:** `NoteEntity.pinned` (Boolean, Room v6, default false). Set at capture time via Extras panel or preserved locally through sync. Server does not return this field.

**Current ordering:** All inbox queries use `ORDER BY created DESC` — strict reverse-chronological. Pinned notes have no ordering advantage. The only visible indicator is a small pin icon in the inbox card's timestamp row (I8).

**Affected queries in `NoteDao`:**
| Query | Current SQL | Used By |
|-------|------------|---------|
| `getInboxNotes()` | `WHERE status = 'open' ORDER BY created DESC` | All tab + type-filtered tabs |
| `getPendingSyncNotes()` | `WHERE pending_sync = 1 ORDER BY created DESC` | Pending tab |
| `searchNotes()` | `WHERE ... LIKE ... ORDER BY created DESC` | Search (short queries) |
| `searchNotesFts()` | `JOIN notes_fts ... ORDER BY notes.created DESC` | Search (3+ char queries) |

**Tab filtering:** Done in-memory via `Flow.maybeFilterByType()` after the query. The DB returns all open notes; Kotlin filters by `BrowseType`.

---

## Options

### Option A: Pinned floats to top in all views

**Change:** `ORDER BY created DESC` → `ORDER BY pinned DESC, created DESC` in all 4 queries.

Pinned notes always appear before unpinned notes within the same view. Among pinned notes, newest first. Among unpinned, newest first.

| Dimension | Assessment |
|-----------|-----------|
| **Scanability** | Strong. Pinned items form a stable "shelf" at top of every view. Easy to find what you pinned. |
| **Tab interaction** | Consistent. Pinned tasks float in Tasks tab, pinned ideas float in Ideas tab, etc. No surprises when switching tabs. |
| **Search interaction** | Pinned results sort first. Slightly unusual for search, but tolerable for a small personal inbox. |
| **Pending sync** | Pinned pending items sort first in Pending tab. Makes sense — you'd want to see your pinned pending items first. |
| **Mental model** | Clear: "pinned = stays at the top." Universal digital convention (Gmail, Slack, iOS Notes, Notion). |
| **Implementation** | Minimal. Change 4 SQL `ORDER BY` clauses. No Kotlin logic, no new UI, no migration. |
| **Risk of surprise** | Low. Matches what "pin" means everywhere else. |
| **Product fit** | Good. A fast capture app benefits from "important stuff rises above the stream." |

### Option B: Pinned floats to top only in All tab

**Change:** Either modify only `getInboxNotes()` query, or add Kotlin-level re-sorting in `observeTab()` for the All case.

| Dimension | Assessment |
|-----------|-----------|
| **Scanability** | Partial. Pinned items float in All but not in type tabs. User may pin something in All, switch to Tasks tab, and not see it at top. |
| **Tab interaction** | Inconsistent. Pin behavior depends on which tab you're looking at. |
| **Search interaction** | No pinned boost in search. |
| **Pending sync** | No pinned boost in Pending tab. |
| **Mental model** | Confusing. "Pinned sometimes means top, sometimes doesn't." |
| **Implementation** | Medium. Either two versions of the query, or Kotlin-level sorting for one tab. |
| **Risk of surprise** | Medium. Switching tabs changes whether pin matters. |
| **Product fit** | Weak. Partial application of a simple concept adds cognitive load without benefit. |

### Option C: User toggle / sort mode

**Change:** Add a sort button or dropdown to Inbox toolbar. Options: "Newest" (current), "Pinned first", possibly others.

| Dimension | Assessment |
|-----------|-----------|
| **Scanability** | Flexible but requires user action. Most users never change sort modes. |
| **Tab interaction** | Sort mode would apply globally or per-tab (per-tab is more complex). |
| **Search interaction** | Sort mode affects search results — adds more state to manage. |
| **Pending sync** | Would need its own sort interaction or inherit from the main mode. |
| **Mental model** | Most complex. Now there are two independent axes: tab filter + sort mode. |
| **Implementation** | High. New UI control, new preference storage, per-tab or global sort state, multiple query variants or Kotlin-level sorting. |
| **Risk of surprise** | Low (user controls it), but high discovery cost (user may never find the toggle). |
| **Product fit** | Overkill. Inkwell is a fast personal capture tool, not a project management app. One more setting is one more decision point. |

### Option D: Pin is visual emphasis only (current behavior)

**Change:** None. Pin icon shows on cards; ordering remains chronological.

| Dimension | Assessment |
|-----------|-----------|
| **Scanability** | Weak for pinned items. As more captures arrive, pinned items scroll down. The pin icon helps scanning but doesn't prevent burial. |
| **Tab interaction** | No interaction. Consistent because nothing happens. |
| **Search interaction** | No interaction. |
| **Pending sync** | No interaction. |
| **Mental model** | Unclear. "I pinned this, but it still scrolled away." Users may wonder why pin exists. |
| **Implementation** | Zero. Already shipped. |
| **Risk of surprise** | None. |
| **Product fit** | Weak. The pin affordance sets an expectation that the current behavior doesn't fulfill. If pinned items don't sort differently, the feature feels incomplete. |

---

## Comparison Matrix

| Criterion | A: All views | B: All tab only | C: Toggle | D: Visual only |
|-----------|:-----------:|:---------------:|:---------:|:--------------:|
| Scanability | ★★★ | ★★ | ★★★ | ★ |
| Tab consistency | ★★★ | ★ | ★★ | ★★★ |
| Mental model clarity | ★★★ | ★ | ★★ | ★ |
| Implementation simplicity | ★★★ | ★★ | ★ | ★★★ |
| Risk of surprise | ★★★ | ★★ | ★★★ | ★★★ |
| Product fit for Inkwell | ★★★ | ★ | ★ | ★ |
| **Total** | **18** | **9** | **12** | **12** |

---

## Recommendation: Option A

**Pinned floats to top in all views.**

This is the strongest choice on every dimension except "zero effort" (which Option D wins trivially). It matches the universal mental model of pinning, requires the smallest implementation (4 SQL clause changes), and makes the pin feature feel complete.

### Why not the others:
- **B** creates confusing inconsistency without any compensating benefit.
- **C** adds UI complexity and a settings decision for a feature that has an obvious default behavior.
- **D** leaves the pin feature feeling broken — "I pinned it, but it scrolled away" is a disappointing UX.

---

## Implementation Shape (if approved)

### Changes required (estimated: 15 minutes)

**1. NoteDao.kt — 4 queries:**
```sql
-- getInboxNotes
SELECT * FROM notes WHERE status = 'open' ORDER BY pinned DESC, created DESC

-- getPendingSyncNotes
SELECT * FROM notes WHERE pending_sync = 1 ORDER BY pinned DESC, created DESC

-- searchNotes
SELECT * FROM notes WHERE status = 'open' AND (...) ORDER BY pinned DESC, created DESC

-- searchNotesFts
SELECT ... ORDER BY notes.pinned DESC, notes.created DESC
```

**2. No other code changes needed.**
- In-memory `maybeFilterByType()` preserves the DB sort order.
- Inbox card already shows pin icon (I8).
- No new UI, no migration, no preference.

### Tests to add
- Query ordering test: pinned note sorts before unpinned with earlier created date
- Tab filtering preserves pinned-first ordering
- Search results respect pinned ordering
- Unpinning a note moves it back to chronological position

### Migration / user-facing considerations
- **No migration needed.** The `pinned` column already exists (Room v6).
- **No user-facing setting.** Behavior change is automatic.
- **Existing pinned notes (if any):** Will immediately float to top on next app launch. This is the expected outcome.
- **Unpinning:** Standard behavior — note returns to its chronological position.

---

## No-Regret Prep Work

There is **no prep work that makes sense outside of the actual implementation.** The entire change is 4 SQL clause modifications. Splitting it into prep + implementation would add overhead without value.

---

## Decision

**Tyler approved Option A on 2026-03-28.** Implemented in I15, verified on-device 2026-03-28.
- 4 NoteDao queries updated to `ORDER BY pinned DESC, created DESC`
- 10 unit tests in `PinnedSortingTest`
- No UI changes, no migration, no new preferences
- Device verification confirmed pinned items float above unpinned in all views
