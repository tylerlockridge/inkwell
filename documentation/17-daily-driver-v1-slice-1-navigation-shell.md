# Daily Driver v1 Slice 1: Navigation Shell

*Created: 2026-04-22 | Updated: 2026-04-22-visual-verified | Project: Inkwell | Package: Inkwell Daily Driver v1*

---

## Verdict

The first implementation slice is:

**D1 - Daily Workspace Navigation Shell**

This is the right first slice because every daily-driver lane depends on the app shell. Inkwell already has a bottom nav with `Capture`, `Inbox`, and `Settings`; this slice turns that shell into a daily workspace without changing backend contracts.

---

## Roadmap Anchor

Source:

- `tasks/prd-inkwell-daily-driver-v1.md`
- User story: `US-001: Define the Daily Driver navigation model`

The PRD says the first package must define the top-level app surfaces for Capture, Inbox, Tasks, Notes, Lists, Search, and System State while keeping project pages out of the main navigation.

---

## Product Decision

Daily Driver v1 should use a bottom navigation shell, but not every surface belongs in the bottom bar.

Bottom nav becomes:

1. Capture
2. Inbox
3. Tasks
4. Notes
5. Lists

Supporting surfaces:

- Search stays inside Inbox/Tasks/Notes/Lists through the existing search affordance.
- Ideas stay available through the existing Inbox tab/filter model, not as a first bottom-nav item.
- Pending stays available through the existing Inbox tab/filter model.
- Settings stays reachable from existing screen actions.
- System Health stays reachable from Settings.

This keeps the daily mobile app focused on capture and the three main review lanes Tyler named: notes, tasks, and lists.

---

## Implementation Scope

### Files Expected To Change

- `app/src/main/kotlin/io/inkwell/ui/navigation/Screen.kt`
- `app/src/main/kotlin/io/inkwell/ui/navigation/CaptureNavHost.kt`
- `app/src/main/kotlin/io/inkwell/ui/inbox/InboxScreen.kt`
- `app/src/main/kotlin/io/inkwell/ui/inbox/InboxViewModel.kt` if needed for initial-tab routing
- `app/src/test/kotlin/io/inkwell/NavigationTest.kt`
- `app/src/test/kotlin/io/inkwell/ScreenTest.kt`
- `app/src/test/kotlin/io/inkwell/BuildConfigTest.kt`
- `app/src/androidTest/kotlin/io/inkwell/InboxScreenTest.kt` if bottom-nav labels or setup change
- `documentation/06-ui-architecture.md`
- `PROJECT.md`

### Code Shape

1. Extend `Screen.kt`.
   - Add bottom-nav destinations for Tasks, Notes, and Lists.
   - Keep Capture as the first item.
   - Remove Settings from `bottomNavItems`, but keep the Settings route.
   - Keep `NOTE_DETAIL_ROUTE` and `SYSTEM_HEALTH_ROUTE` unchanged.

2. Reuse `InboxScreen` for the daily lanes.
   - `Screen.Inbox` opens `InboxTab.All`.
   - `Screen.Tasks` opens `InboxTab.Tasks`.
   - `Screen.Notes` opens `InboxTab.Notes`.
   - `Screen.Lists` opens `InboxTab.Lists`.
   - Keep the current internal tab row during Slice 1 so Ideas and Pending remain reachable.

3. Add initial-tab support.
   - Add an `initialTab: InboxTab = InboxTab.All` parameter to `InboxScreen`.
   - On first composition for each route, call `InboxViewModel.onTabChange(initialTab)` when needed.
   - Avoid repeatedly resetting the selected tab after the user interacts with filters/search.

4. Update `CaptureNavHost.kt`.
   - Add composable routes for the new daily lane screens.
   - Hide the bottom bar only on System Health and detail screens if the current behavior needs calmer reading space.
   - Keep Settings reachable from Capture and Inbox actions.

5. Update icons and labels.
   - Use existing Material icon imports when available.
   - Do not add custom SVGs for this slice.
   - Keep labels short enough for mobile: `Capture`, `Inbox`, `Tasks`, `Notes`, `Lists`.

---

## Explicit Non-Goals

- No mobile project pages.
- No remote session controls.
- No command prompt submission.
- No terminal output display.
- No backend API changes.
- No database migration.
- No rewrite of `InboxScreen`.
- No removal of Ideas or Pending filters.
- No widget or notification changes.

---

## Implementation Result

Status: code implemented, automated proof passed, visual proof captured.

Changed files:

- `app/src/main/kotlin/io/inkwell/ui/navigation/Screen.kt`
- `app/src/main/kotlin/io/inkwell/ui/navigation/CaptureNavHost.kt`
- `app/src/main/kotlin/io/inkwell/ui/inbox/InboxScreen.kt`
- `app/src/main/res/layout/widget_loading.xml`
- `app/src/test/kotlin/io/inkwell/NavigationTest.kt`
- `app/src/test/kotlin/io/inkwell/ScreenTest.kt`
- `app/src/test/kotlin/io/inkwell/BuildConfigTest.kt`
- `documentation/06-ui-architecture.md`
- `documentation/17-daily-driver-v1-slice-1-navigation-shell.md`
- `PROJECT.md`

Implementation notes:

- Bottom nav now exposes `Capture`, `Inbox`, `Tasks`, `Notes`, and `Lists`.
- `Settings` remains a route but is no longer a bottom-nav item.
- The new `Tasks`, `Notes`, and `Lists` routes reuse `InboxScreen(initialTab = ...)`.
- `InboxScreen` now accepts `initialTab` and applies it through the existing `InboxViewModel.onTabChange(...)` path.
- `Screen.SYSTEM_HEALTH_ROUTE` and `Screen.NOTE_DETAIL_ROUTE` are unchanged.
- `widget_loading.xml` now uses `app:tint`, which keeps the full lint gate clean.

Automated proof:

- `./gradlew --no-daemon test` passed.
- `./gradlew --no-daemon lint` passed.
- `./gradlew --no-daemon assembleRelease` passed and produced `app/build/outputs/apk/release/app-release.apk`.
- `./gradlew --no-daemon installDebug` passed on `Medium_Phone_API_36.1`.

Visual proof:

- Screenshot proof was captured on `Medium_Phone_API_36.1` after Tyler unlocked the emulator.
- Canonical evidence folder: `.visual-qa/reviews/d1-navigation-shell-2026-04-22-pass2/`.
- Screenshots captured: `01-capture.png`, `02-inbox.png`, `03-tasks.png`, `04-notes.png`, and `05-lists.png`.
- Matching UI dumps confirmed the expected title, selected bottom-nav item, and visible five-label bottom nav for each route.

---

## Acceptance Criteria

- [x] Bottom nav has exactly five items: Capture, Inbox, Tasks, Notes, Lists.
- [x] Capture remains first.
- [x] Settings is no longer a bottom-nav item but remains reachable.
- [x] Tapping Inbox opens the all-items view.
- [x] Tapping Tasks opens the task-filtered view.
- [x] Tapping Notes opens the note-filtered view.
- [x] Tapping Lists opens the list-filtered view.
- [x] Ideas and Pending remain reachable through the existing inbox tab/filter model.
- [x] Search still works inside the selected lane.
- [x] Note detail deep links still open the detail screen.
- [x] System Health deep links still open the health screen.
- [x] `./gradlew test` passes.
- [x] `./gradlew lint` passes.
- [x] A debug or release APK builds.
- [x] Screenshot proof shows the new bottom nav and each daily lane.

---

## Test Updates

Update expected bottom-nav count and route order in:

- `NavigationTest`
- `ScreenTest`
- `BuildConfigTest`

Add or update assertions for:

- route uniqueness
- `Capture` as first route
- `Settings` still has a route but is not in `bottomNavItems`
- daily lane routes map to the correct `InboxTab`

Instrumented test updates:

- Keep existing Inbox navigation test valid.
- Add smoke coverage for Tasks, Notes, and Lists bottom-nav labels if stable on device/emulator.

---

## Stop Conditions

Stop and report before broadening scope if:

- The bottom nav cannot fit five labels cleanly on the Pixel target.
- Initial-tab routing causes repeated tab resets after user interaction.
- Existing deep links break.
- Tests reveal Settings or System Health became unreachable.

---

## Next Slice After D1

After D1 lands, the next slice should be:

**D2 - Type-First Capture Refinement**

That slice should improve task/note/list/idea capture entry now that the app shell makes those lanes first-class.

Packet:

- `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md`
