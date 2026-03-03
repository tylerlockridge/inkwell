# Feature: Security Checklist

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Tracks the security posture of the Inkwell app across all layers — token storage, network, build configuration, coroutine safety, and lifecycle correctness. Items resolved during the 2026-02-28 audit are marked. Outstanding items require follow-up.

**What it does NOT do:**
- Does not replace the per-feature security documentation in `01-authentication-security.md`
- Does not cover server-side security (that is Obsidian-Dashboard-Desktop's concern)

---

## Resolved Items (Fixed 2026-02-28)

| Item | Fix Applied | Notes |
|------|------------|-------|
| `CancellationException` rethrown | ✅ PASS | `CaptureRepository.kt` + `SyncWorker.kt` |
| `sendWithoutRequest()` removed | ✅ PASS | Prevents token sent to FCM/CDNs before 401 challenge |
| `BuildConfig.DEFAULT_AUTH_TOKEN` removed | ✅ PASS | Runtime config only, no APK secret |
| N+1 detail fetches → concurrent | ✅ PASS | `coroutineScope { async/awaitAll }` in SyncWorker |
| `collectAsState()` → `collectAsStateWithLifecycle()` | ✅ PASS | All 5 screens updated |
| `MainViewModel` extracted | ✅ PASS | Biometric state coordination out of Activity |
| `EncryptedSharedPreferences` for token | ✅ PASS | AES256-GCM, Android Keystore-backed |
| `allowBackup=false` in manifest | ✅ PASS | Prevents ADB backup token extraction |
| Release build R8 + ProGuard | ✅ PASS | Minification + data class rules |

---

## Outstanding Items

### Warnings (require attention)

| Item | Risk | Notes |
|------|------|-------|
| Last-write-wins conflict resolution | High | Silent data loss on concurrent edits — no conflict surfacing to user |
| `CancellationException` edge cases | Medium | Fix applied in main callers; other `catch(e: Exception)` blocks in callees may still swallow cancellation |

### TODOs (not yet implemented)

| Item | Priority | Notes |
|------|----------|-------|
| Certificate pinning (ISRG Root X1) | Medium | Not configured in OkHttp/Ktor; MITM risk on compromised networks |
| FCM registration retry | Low | If initial device registration fails, no retry is scheduled; relies on next `onNewToken()` |

---

## Security Architecture Summary

| Layer | Mechanism | Status |
|-------|-----------|--------|
| Auth token at rest | EncryptedSharedPreferences (AES256-GCM, Keystore) | ✅ PASS |
| Auth token in transit | HTTPS + Bearer header; unauthenticated client for auth exchange | ✅ PASS |
| Backup protection | `allowBackup=false` | ✅ PASS |
| APK secrets | None (BuildConfig token removed) | ✅ PASS |
| Biometric enforcement | `BIOMETRIC_STRONG` + 60s re-lock | ✅ PASS |
| Network HTTPS enforcement | Required; HTTP only for localhost/10.0.2.2 | ✅ PASS |
| 401 response handling | Clear token + notify + stop retries | ✅ PASS |
| Release build hardening | R8 + ProGuard | ✅ PASS |
| Certificate pinning | Not configured | 🔲 TODO |
| Token refresh | No mechanism | 🔲 TODO |
| Conflict resolution transparency | Silent overwrite | ⚠️ WARN |

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| All 2026-02-28 audit fixes applied | ✅ PASS | 9 items resolved |
| Conflict resolution silent data loss | ⚠️ WARN | No user-facing conflict resolution |
| CancellationException edge cases | ⚠️ WARN | May remain in non-primary callers |
| Certificate pinning | 🔲 TODO | |
| FCM registration retry | 🔲 TODO | |
