# Feature: Sync Strategy

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Manages bidirectional sync between the local Room database and the Obsidian-Dashboard-Desktop server using two WorkManager workers — one for pulling server data into Room (SyncWorker) and one for pushing local pending notes to the server (UploadWorker).

**What it does NOT do:**
- Does not surface conflicts to the user — uses silent last-write-wins resolution
- Does not clean up WorkManager orphaned entries on uninstall/reinstall
- Does not guarantee CancellationException is re-thrown in all catch blocks (edge cases may remain)

---

## Worker Architecture

| Worker | Direction | Trigger |
|--------|-----------|---------|
| `SyncWorker` | Server → Room (pull) | Periodic (15 min) + immediate |
| `UploadWorker` | Room → Server (push) | After capture + periodic |

Both workers are scheduled by `SyncScheduler` using WorkManager with:
- Network constraint: `CONNECTED`
- Exponential backoff on failure
- 15-minute minimum interval (WorkManager floor — user values below this are coerced)

---

## SyncWorker (Pull)

1. Fetch inbox list from server
2. Concurrent detail fetches using `coroutineScope { async/awaitAll }` (N+1 fix applied 2026-02-28)
3. For each fetched item, apply conflict resolution
4. Detect and handle stale items
5. Record last sync timestamp on success
6. Trigger widget count update via `WidgetStateUpdater`

### Conflict Resolution

Last-write-wins via ISO 8601 string comparison:

```
if (localNote.updated >= item.updated_at) → skip server update (keep local)
else → apply server update
```

**Risk:** If both sides were edited concurrently, the local version silently overwrites the server version (or vice versa) with no user notification.

### Retry Policy

SyncWorker retries the entire batch **only if ALL items failed AND items exist**. If any items succeed, `Result.success()` is returned.

### Stale Item Handling

If a detail fetch fails for a specific item: skip that item, log a warning, retry on the next sync cycle.

---

## UploadWorker (Push)

Routes each pending note based on its UID prefix:

| UID Pattern | Action |
|-------------|--------|
| `"pending_*"` | POST `/api/capture` (new note) |
| Real UID | PATCH `/api/note/:uid` (update existing) |

Processes all pending notes as a batch.

### Error Classification

| HTTP Status | Action |
|-------------|--------|
| 401 | Auth invalid → `Result.failure()`, stop all retries |
| 400–499 | Permanent client error → mark `syncError=true` on note, continue batch |
| 5xx / network error | Transient → `Result.retry()` (WorkManager reschedules) |

### Partial Success

If any items in the batch succeed, `Result.success()` is returned. The batch is not retried as a whole — individual failed items will be retried on the next sync cycle.

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| SyncWorker (server → Room pull) | ✅ PASS | |
| UploadWorker (Room → server push) | ✅ PASS | |
| WorkManager periodic scheduling | ✅ PASS | 15-min floor enforced |
| Network constraint (CONNECTED) | ✅ PASS | |
| Exponential backoff | ✅ PASS | |
| Concurrent detail fetches | ✅ PASS | Fixed 2026-02-28 (was N+1) |
| Conflict resolution (last-write-wins) | ⚠️ WARN | Silent data loss on concurrent edits |
| UID routing (pending_* vs real UID) | ✅ PASS | |
| Error classification (401/4xx/5xx) | ✅ PASS | |
| Partial success handling | ✅ PASS | |
| Stale item skip + retry next cycle | ✅ PASS | |
| Last sync time recorded | ✅ PASS | |
| Widget counts updated after sync | ✅ PASS | WidgetStateUpdater |
| CancellationException rethrown | ⚠️ WARN | Fixed in main callers; edge cases may remain |
| WorkManager orphan cleanup | ⚠️ WARN | Not handled on uninstall/reinstall |
