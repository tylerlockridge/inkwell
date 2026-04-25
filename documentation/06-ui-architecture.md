# Feature: UI Architecture

*Created: 2026-03-02 | Updated: 2026-04-22-D1-implemented | Project: Inkwell*

---

## Feature Overview

**What it does:**
Implements a single-activity MVVM + Repository architecture with Hilt dependency injection, Compose-based screens, StateFlow state management, and a NavHost router that handles both in-app navigation and deep link routing from notifications.

**What it does NOT do:**
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
| `CaptureScreen` | Primary capture — visually polished I6.7 (bold-dark banner, tighter surface) |
| `InboxScreen` | Type-aware inbox — visually overhauled I6.4 (bold-dark, tighter density, inline tab counts) |
| `NoteDetailScreen` | Type-dispatched detail — visually overhauled I6.5 (bold-dark, section labels, type badge) |
| `SettingsScreen` | App configuration — visually overhauled I9 (bold-dark, tighter density, cohesive cards) |
| `SystemHealthScreen` | Instrument panel — visually overhauled I9 (bold-dark, tighter StatusRow, compact layout) |
| `LockScreen` | Biometric auth prompt (inline composable in `MainActivity`) |

---

## Navigation

`CaptureNavHost` manages all routing. Current bottom navigation exposes Capture,
Inbox, Tasks, Notes, and Lists. Settings remains reachable from screen actions, but
it is no longer a bottom-nav item.

Routes:

| Route | Screen |
|-------|--------|
| `capture` | CaptureScreen |
| `inbox` | InboxScreen with `InboxTab.All` |
| `tasks` | InboxScreen with `InboxTab.Tasks` |
| `notes` | InboxScreen with `InboxTab.Notes` |
| `lists` | InboxScreen with `InboxTab.Lists` |
| `note/{uid}` | NoteDetailScreen |
| `settings` | SettingsScreen |
| `system-health` | SystemHealthScreen |

Deep links from FCM notifications are parsed by `DeepLink` and injected into `CaptureNavHost` for routing.

---

## ViewModels

| ViewModel | Responsibility |
|-----------|---------------|
| `CaptureViewModel` | Capture state, submission, offline routing |
| `InboxViewModel` | Type-aware inbox browse, search within type, tab counts |
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
| Daily Driver v1 D1 bottom nav | ✅ PASS | Capture/Inbox/Tasks/Notes/Lists implemented; screenshots in `.visual-qa/reviews/d1-navigation-shell-2026-04-22-pass2/` |
| Deep link routing from notifications | ✅ PASS | DeepLink parser |
| collectAsStateWithLifecycle() on all screens | ✅ PASS | Fixed 2026-02-28 |
| MainViewModel extracted | ✅ PASS | Fixed 2026-02-28 |
| HiltTestRunner for instrumented tests | ✅ PASS | |
| 4 instrumented UI tests | ✅ PASS | |
| Type-aware inbox tabs (6 tabs, ScrollableTabRow) | ✅ PASS | I2: All/Tasks/Notes/Lists/Ideas/Pending |
| BrowseType classifier + 16 unit tests | ✅ PASS | I2: heuristic documented in BrowseType.kt |
| Type-dispatched detail views (Task/Note/List/Idea) | ✅ PASS | I3: shared edit, type-specific metadata layout |
| Interactive list checkboxes (local-only) | ✅ PASS | I4: ChecklistItemRow, strikethrough, progress counter |
| Inbox visual overhaul (pilot slice 1) | ✅ PASS | I6.4: bold-dark density, inline tab counts, type badges, subtle sync |
| Inbox pending-state differentiation | ✅ PASS | I6.4b: 5% amber tint on pending cards, 2-line note preview |
| Detail visual overhaul (pilot slice 2) | ✅ PASS | I6.5: type badge in title bar, section labels, MetadataCard, tighter layout |
| Capture visual overhaul (pilot slice 3) | ✅ PASS | I6.7: connection banner, tighter toolbar, refined writing surface |
| Slice 3 metadata in detail (pinned/shared/color/sourceUrl) | ✅ PASS | I8: inline status indicators, metadata rows, tappable source URL |
| Slice 3 pinned indicator in inbox cards | ✅ PASS | I8: pin icon in timestamp row |
| Settings visual overhaul | ✅ PASS | I9: bold-dark title, uppercase section labels, tighter card padding, cohesive surface colors |
| Settings connection card unified | ✅ PASS | I9: both states use surfaceContainerLow, inline status indicators |
| System Health visual overhaul | ✅ PASS | I9: instrument-panel density, tighter StatusRow (bodySmall), compact StatBlock, subsection labels |
| Capture-time Slice 3 metadata (pin/color/sourceUrl) | ✅ PASS | I10: ToolbarPanel.Extras with pin toggle, curated color picker, source URL field |
| Share intent → sourceUrl auto-fill | ✅ PASS | I11: first http/https URL extracted, body cleaned when URL-only |
| Share intent → shared=true automatic | ✅ PASS | I11: system-derived flag for share-intent captures |
