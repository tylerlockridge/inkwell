# Feature: Home Screen Widgets

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Provides two Android home screen widgets built with Jetpack Glance (Compose for widgets) — a quick-launch widget for rapid capture and an inbox count widget showing sync and open note counts.

**What it does NOT do:**
- Does not update widget state in real time — counts are only refreshed after explicit capture or sync events
- Does not support widget configuration screens

---

## Widgets

### QuickCaptureWidget (4x1)

- Quick launch button that opens the Capture screen directly
- Displays inbox badge count (open notes count)
- Minimum size: 4 columns x 1 row

### InboxCountWidget (2x2)

- Displays pending sync count (notes waiting to upload)
- Displays inbox open count (notes with status='open')
- Minimum size: 2 columns x 2 rows

---

## Widget State Updates

`WidgetStateUpdater` is responsible for refreshing widget data. It is called:
- After every successful capture operation (in `CaptureRepository`)
- After every sync operation completes (in `SyncWorker`)

Updates are wrapped in try/catch — a widget update failure is non-blocking and does not affect the capture or sync result.

---

## Implementation

Both widgets use **Jetpack Glance** — the Compose-based widget API. Widget receivers are registered in `AndroidManifest.xml`.

Glance communicates with the app's data layer via `WidgetStateUpdater`, which reads from Room and posts updated state to the widget's `GlanceAppWidget` instance.

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| QuickCaptureWidget (4x1) | ✅ PASS | Quick launch + inbox badge |
| InboxCountWidget (2x2) | ✅ PASS | Pending sync + open count |
| Jetpack Glance implementation | ✅ PASS | |
| Widget receivers in AndroidManifest | ✅ PASS | |
| WidgetStateUpdater after capture | ✅ PASS | Non-blocking (try/catch) |
| WidgetStateUpdater after sync | ✅ PASS | Non-blocking (try/catch) |
| Real-time widget updates | ⚠️ WARN | Only after explicit capture/sync events |
