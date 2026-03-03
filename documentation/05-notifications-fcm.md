# Feature: Notifications & FCM

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Receives Firebase Cloud Messaging push notifications to trigger syncs, show new capture alerts, and surface sync errors. Supports background notification actions (mark done, retry) without requiring the user to open the app.

**What it does NOT do:**
- Does not retry FCM device registration if the initial registration fails
- Does not alert the user if their device is not subscribed to push notifications
- Does not support notification grouping/bundling

---

## FCM Service

`CaptureMessagingService` extends `FirebaseMessagingService` and handles three push message types:

| Message Type | Action |
|-------------|--------|
| `new_capture` | Show notification with deep link to inbox/detail |
| `sync_required` | Trigger immediate `SyncWorker` run |
| `sync_error` | Show error notification with retry action |

---

## Notification Actions

Notifications include interactive actions handled without opening the app:

| Action | Handler | Behavior |
|--------|---------|---------|
| View | Deep link | Routes to inbox / note detail / capture screen |
| Mark Done | `NotificationActionReceiver` broadcast | Updates note status to `'completed'` in Room, no app open required |
| Retry Sync | `NotificationActionReceiver` broadcast | Triggers `UploadWorker` immediately |

`NotificationActionReceiver` is a broadcast receiver registered in `AndroidManifest.xml`.

---

## Device Registration

Device registration happens on sign-in:
- Device ID + FCM token are sent to the server
- Token is re-registered automatically on refresh via `onNewToken()` callback

**Known gap:** If the initial registration request fails, there is no retry scheduled. The next opportunity to register is on the next `onNewToken()` callback or the next sign-in.

---

## Deep Link Routing

Notification taps are routed through `CaptureNavHost` via `DeepLink` parser:

| Destination | Route |
|------------|-------|
| Capture screen | `capture` |
| Inbox | `inbox` |
| Note detail | `detail/:uid` |
| Settings | `settings` |
| System health | `health` |

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| new_capture notification | ✅ PASS | |
| sync_required push trigger | ✅ PASS | |
| sync_error notification | ✅ PASS | |
| Mark-done background action | ✅ PASS | NotificationActionReceiver |
| Retry sync background action | ✅ PASS | NotificationActionReceiver |
| FCM token registration on sign-in | ✅ PASS | |
| Token re-registration on onNewToken() | ✅ PASS | |
| Deep link routing (all routes) | ✅ PASS | CaptureNavHost |
| FCM registration failure retry | ⚠️ WARN | No retry; relies on next onNewToken() |
| User notification if not subscribed | ⚠️ WARN | No detection or alert |
