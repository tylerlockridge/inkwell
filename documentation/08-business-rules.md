# Feature: Business Rules Reference

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

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

**Rule 14 — FTS search threshold:**
- Queries of 3+ characters: use FTS4 full-text index
- Queries of 1–2 characters: fall back to SQL `LIKE` query

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
| Rule 14: FTS threshold (3 chars) | ✅ PASS | |
