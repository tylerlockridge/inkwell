# Feature: Authentication & Security

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Handles Google Sign-In via Credential Manager, stores the auth token in hardware-backed encrypted storage, enforces biometric re-lock after background, and applies a strict 401 policy that halts retries and notifies the user.

**What it does NOT do:**
- Does not refresh tokens automatically — user must re-sign-in on expiry
- Does not support non-Google sign-in providers
- Does not pin certificates (ISRG Root X1 not configured in OkHttp/Ktor)

---

## Sign-In Flow

Google Sign-In uses the Android Credential Manager API. An ID token is exchanged with the server — no plaintext token is embedded in the APK.

Two `HttpClient` instances exist in `NetworkModule`:
- **Authenticated client**: adds `Authorization: Bearer <token>` header + 401 handler
- **Unauthenticated client**: used only for the auth exchange itself (avoids sending token to FCM/CDNs before a 401 challenge)

The `sendWithoutRequest()` mechanism was removed on 2026-02-28 to prevent token leakage to non-server hosts.

---

## Token Storage

| Mechanism | Details |
|-----------|---------|
| Storage class | `EncryptedSharedPreferences` |
| Encryption | AES256-GCM, Android Keystore-backed |
| Migration | One-time, idempotent: old plaintext DataStore token → `EncryptedSharedPreferences` on upgrade |
| Corruption recovery | `catch` keyset exception → delete corrupted prefs → recreate with fresh key |

`BuildConfig.DEFAULT_AUTH_TOKEN` was removed on 2026-02-28. Token is configured at runtime via Settings only.

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
| Google Sign-In (Credential Manager) | ✅ PASS | ID token exchange, no APK secret |
| Token storage (EncryptedSharedPreferences) | ✅ PASS | AES256-GCM, Keystore-backed |
| Token migration on upgrade | ✅ PASS | One-time, idempotent |
| Keyset corruption recovery | ✅ PASS | Delete + recreate |
| Biometric BIOMETRIC_STRONG enforcement | ✅ PASS | |
| Biometric re-lock on resume (60s) | ✅ PASS | Hard-coded timeout |
| Biometric unavailable skip | ✅ PASS | checkCapability() guard |
| 401 policy (clear + notify + no retry) | ✅ PASS | |
| HTTPS enforcement | ✅ PASS | localhost/10.0.2.2 exception |
| allowBackup=false | ✅ PASS | |
| BuildConfig.DEFAULT_AUTH_TOKEN removed | ✅ PASS | Fixed 2026-02-28 |
| sendWithoutRequest() removed | ✅ PASS | Fixed 2026-02-28 |
| Token refresh mechanism | 🔲 TODO | Must re-sign-in on expiry |
| Certificate pinning (ISRG Root X1) | 🔲 TODO | Not configured in OkHttp/Ktor |
