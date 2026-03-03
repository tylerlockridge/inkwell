# Feature: UI Architecture

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Implements a single-activity MVVM + Repository architecture with Hilt dependency injection, Compose-based screens, StateFlow state management, and a NavHost router that handles both in-app navigation and deep link routing from notifications.

**What it does NOT do:**
- Does not use a traditional bottom navigation tab bar — routes are screen-level only
- Does not support multiple back stacks (single back stack navigation)

---

## Architecture Pattern

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose |
| State | StateFlow + `collectAsStateWithLifecycle()` |
| ViewModel | Hilt-injected ViewModels |
| Repository | Repository pattern (injected via Hilt) |
| DI | Hilt throughout |

`collectAsState()` was replaced with `collectAsStateWithLifecycle()` on all 5 screens on 2026-02-28. This prevents state collection when the lifecycle is not in an active state.

---

## Screens

| Screen | Purpose |
|--------|---------|
| `CaptureScreen` | Primary note capture (457 lines) |
| `InboxScreen` | List of open notes |
| `NoteDetailScreen` | View/edit a single note |
| `SettingsScreen` | App configuration (289 lines after split refactor) |
| `SystemHealthScreen` | Sync health, worker status |
| `LockScreen` | Biometric auth prompt (inline composable in `MainActivity`) |

---

## Navigation

`CaptureNavHost` manages all routing. Routes:

| Route | Screen |
|-------|--------|
| `capture` | CaptureScreen |
| `inbox` | InboxScreen |
| `detail/:uid` | NoteDetailScreen |
| `settings` | SettingsScreen |
| `health` | SystemHealthScreen |

Deep links from FCM notifications are parsed by `DeepLink` and injected into `CaptureNavHost` for routing.

---

## ViewModels

| ViewModel | Responsibility |
|-----------|---------------|
| `CaptureViewModel` | Capture state, submission, offline routing |
| `InboxViewModel` | Note list, search, filter |
| `NoteDetailViewModel` | Single note state, edit, delete |
| `SettingsViewModel` | Settings read/write |
| `SystemHealthViewModel` | Sync health indicators |
| `MainViewModel` | Biometric lock state + sync triggers (extracted 2026-02-28) |

`MainViewModel` was extracted from `MainActivity` on 2026-02-28 to properly coordinate biometric lock state and sync triggers across screens without putting logic in the Activity.

---

## Dependency Injection

Hilt is used throughout. `HiltTestRunner` is configured for instrumented tests so that Hilt components are properly initialized in the test environment.

---

## Screen Size Notes

| File | Lines | Notes |
|------|-------|-------|
| `CaptureToolbar.kt` | 548 | Largest file; collapsible toolbar panels |
| `CaptureScreen.kt` | 457 | Main capture UI |
| `SettingsScreen.kt` | 289 | Split/refactored from larger original |

---

## Instrumented Tests

4 instrumented tests covering core screens:
- `CaptureScreen` UI test
- `InboxScreen` UI test
- `SettingsScreen` UI test
- `HiltTestRunner` setup verification

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| MVVM + Repository + Hilt throughout | ✅ PASS | |
| All screens as Compose functions | ✅ PASS | |
| CaptureNavHost routing | ✅ PASS | |
| Deep link routing from notifications | ✅ PASS | DeepLink parser |
| collectAsStateWithLifecycle() on all screens | ✅ PASS | Fixed 2026-02-28 |
| MainViewModel extracted | ✅ PASS | Fixed 2026-02-28 |
| HiltTestRunner for instrumented tests | ✅ PASS | |
| 4 instrumented UI tests | ✅ PASS | |
