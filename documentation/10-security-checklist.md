# Feature: Security Checklist

*Created: 2026-03-02 | Updated: 2026-03-25 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Tracks the security posture of the Inkwell app across all layers — token storage, network, build configuration, coroutine safety, and lifecycle correctness. Items resolved during audits are marked. Outstanding items require follow-up.

**What it does NOT do:**
- Does not replace the per-feature security documentation in `01-authentication-security.md`
- Does not cover server-side security (that is Obsidian-Dashboard-Desktop's concern)

---

## Resolved Items

| Item | Fix Applied | When | Notes |
|------|------------|------|-------|
| `CancellationException` rethrown | ✅ PASS | 2026-02-28 | `CaptureRepository.kt` + `SyncWorker.kt` |
| `sendWithoutRequest()` added back | ✅ PASS | 2026-03-06 | Proactive token send for the active manual bearer-token auth path |
| N+1 detail fetches → concurrent | ✅ PASS | 2026-02-28 | `coroutineScope { async/awaitAll }` in SyncWorker |
| `collectAsState()` → `collectAsStateWithLifecycle()` | ✅ PASS | 2026-02-28 | All screens updated |
| `MainViewModel` extracted | ✅ PASS | 2026-02-28 | Biometric state coordination out of Activity |
| `EncryptedSharedPreferences` for token | ✅ PASS | 2026-02-28 | AES256-GCM, Android Keystore-backed |
| `allowBackup=false` in manifest | ✅ PASS | 2026-02-28 | Prevents ADB backup token extraction |
| Release build R8 + ProGuard | ✅ PASS | 2026-02-28 | Minification + data class rules |
| `DEFAULT_AUTH_TOKEN` removed | ✅ PASS | 2026-03-25 (I5a) | BuildConfig field + all 4 PreferencesManager fallback references removed |
| Google Sign-In dead code removed | ✅ PASS | 2026-03-25 (I5a) | GoogleAuthDto, exchangeGoogleToken(), UnauthenticatedClient, Credential Manager deps |
| Token entry always visible in Settings | ✅ PASS | 2026-03-25 (I5a) | Collapsible toggle removed — manual token is the primary auth path |
| Chrome extension token remediation | ✅ PASS | 2026-03-25 (I5b) | No hardcoded token, `chrome.storage.local` only, narrow host perms, sync→local migration |

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
| Chrome extension auth model verified | ✅ PASS | I5b: no hardcoded token, `chrome.storage.local` only, narrow host permissions, sync→local migration in place |

---

## Security Architecture Summary

| Layer | Mechanism | Status |
|-------|-----------|--------|
| Auth model | Per-device manual bearer token (Settings) | ✅ PASS |
| Auth token at rest | EncryptedSharedPreferences (AES256-GCM, Keystore) | ✅ PASS |
| Auth token in transit | HTTPS + Bearer header (sendWithoutRequest) | ✅ PASS |
| Backup protection | `allowBackup=false` | ✅ PASS |
| APK secrets | None (BuildConfig token removed) | ✅ PASS |
| Google Sign-In code | Removed (I5a) | ✅ PASS |
| Biometric enforcement | `BIOMETRIC_STRONG` + 60s re-lock | ✅ PASS |
| Network HTTPS enforcement | Required; HTTP only for localhost/10.0.2.2 | ✅ PASS |
| 401 response handling | Clear token + notify + stop retries | ✅ PASS |
| Release build hardening | R8 + ProGuard | ✅ PASS |
| Certificate pinning | Not configured | 🔲 TODO |
| Token refresh | No mechanism | 🔲 TODO |
| Conflict resolution transparency | Silent overwrite | ⚠️ WARN |
| Chrome extension token | Manual per-device, `chrome.storage.local`, scoped host perms | ✅ PASS |
