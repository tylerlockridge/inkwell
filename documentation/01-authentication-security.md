# Feature: Authentication & Security

*Created: 2026-03-02 | Updated: 2026-03-25 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Authenticates with the server using a manually-configured per-device bearer token stored in hardware-backed encrypted storage. Enforces biometric re-lock after background and applies a strict 401 policy that halts retries and notifies the user.

**What it does NOT do:**
- Does not support Google Sign-In or OAuth (stale code removed in I5a)
- Does not refresh tokens automatically — user must re-enter token on expiry
- Does not pin certificates (ISRG Root X1 not configured in OkHttp/Ktor)

---

## Auth Model

**Per-device manual bearer token.** The user enters their server token in Settings. This is the only auth path — there is no embedded fallback token, no Google Sign-In, and no OAuth flow.

A blank token means unauthenticated. The app will not attempt API calls without a configured token.

One `HttpClient` instance exists in `NetworkModule`:
- **Authenticated client**: reads token from `PreferencesManager.authToken` StateFlow, adds `Authorization: Bearer <token>` header via `sendWithoutRequest { true }`

The `UnauthenticatedClient` was removed in I5a (only existed to support the now-deleted Google auth exchange).

---

## Token Storage

| Mechanism | Details |
|-----------|---------|
| Storage class | `EncryptedSharedPreferences` |
| Encryption | AES256-GCM, Android Keystore-backed |
| Migration | One-time, idempotent: old plaintext DataStore token → `EncryptedSharedPreferences` on upgrade |
| Corruption recovery | `catch` keyset exception → delete corrupted prefs → recreate with fresh key |
| Default value | `""` (empty string = unauthenticated) |

**Removed in I5a:** `BuildConfig.DEFAULT_AUTH_TOKEN` — the app no longer ships with or falls back to a shared bearer secret.

---

## Biometric Lock

Managed by `BiometricAuthManager`.

- `BIOMETRIC_STRONG` is enforced (not `WEAK` or `DEVICE_CREDENTIAL`)
- `checkCapability()` is called before enforcing lock — if hardware unavailable, lock is skipped
- Re-lock triggers on `onResume` if the app has been in the background for more than 60 seconds
- Timeout: `LOCK_TIMEOUT_MS = 60_000L` (hard-coded)

---

## 401 Policy

When any authenticated request receives a 401 response:
1. Clear the stored token
2. Post an "auth expired" notification to the user
3. Return `Result.failure()` — no retry is attempted
4. Stop all further sync retries until the user re-authenticates

---

## Network Security

- HTTPS enforced for all connections
- HTTP allowed only for `localhost` and `10.0.2.2` (dev/emulator addresses)
- `allowBackup=false` in `AndroidManifest.xml` — prevents token extraction via ADB backup
- Release build: R8 minification enabled with ProGuard rules for data classes

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| Per-device manual token auth | ✅ PASS | Settings → token field, always visible |
| No embedded fallback token | ✅ PASS | DEFAULT_AUTH_TOKEN removed (I5a) |
| No Google Sign-In code | ✅ PASS | Dead code removed (I5a) |
| Token storage (EncryptedSharedPreferences) | ✅ PASS | AES256-GCM, Keystore-backed |
| Token migration on upgrade | ✅ PASS | One-time, idempotent |
| Keyset corruption recovery | ✅ PASS | Delete + recreate |
| Biometric BIOMETRIC_STRONG enforcement | ✅ PASS | |
| Biometric re-lock on resume (60s) | ✅ PASS | Hard-coded timeout |
| Biometric unavailable skip | ✅ PASS | checkCapability() guard |
| 401 policy (clear + notify + no retry) | ✅ PASS | |
| HTTPS enforcement | ✅ PASS | localhost/10.0.2.2 exception |
| allowBackup=false | ✅ PASS | |
| Token refresh mechanism | 🔲 TODO | Must re-enter token on expiry |
| Certificate pinning (ISRG Root X1) | 🔲 TODO | Not configured in OkHttp/Ktor |
