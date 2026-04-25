# PRD: Inkwell Daily Driver v1

**Status:** Accepted as the first post-core package baseline on 2026-04-22.
**First implementation slice:** D1 - Daily Workspace Navigation Shell, verified 2026-04-22.
**Second implementation slice:** D2 - Type-First Capture Refinement, defined 2026-04-24.
**Slice packets:** `documentation/17-daily-driver-v1-slice-1-navigation-shell.md`, `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md`

## Introduction

Inkwell Daily Driver v1 is the first post-core package for Inkwell under the Nexus umbrella. The core app already captures items reliably; this package turns Inkwell into the main mobile notes, tasks, lists, and brainstorming app Tyler can use throughout the day.

The package starts with the main app experience only. Project pages, remote operator controls, widgets, and notification expansions are intentionally secondary until the daily-driver app goals are fully defined and accepted.

## Goals

- Make Inkwell feel like a fast daily mobile workspace, not only a capture utility.
- Put notes, tasks, lists, and ideas into clear first-class mobile lanes.
- Keep quick capture fast enough for real life use.
- Make item review, editing, search, filtering, and sync state easy to understand.
- Preserve the closed Inkwell Core baseline while defining the next implementation target.
- Keep secondary Nexus project-page and remote-control goals parked until the main app is accepted.

## User Stories

### US-001: Define the Daily Driver navigation model

**Description:** As Tyler, I want Inkwell to open into a clear daily workspace so I immediately know where to capture, review, and act.

**Acceptance Criteria:**
- [x] Define the top-level app surfaces for Capture, Inbox, Tasks, Notes, Lists, Search, and System State.
- [x] State whether the existing route-only navigation remains enough or whether Daily Driver v1 needs a bottom/navigation rail pattern.
- [x] Keep project pages out of the main navigation for this package.
- [x] Update `documentation/06-ui-architecture.md` if navigation changes.
- [x] Unit tests pass with `./gradlew test`.
- [x] Verify on a physical Pixel device or emulator with screenshots.

**Status:** Implemented and visually verified in D1.

### US-002: Make quick capture type-first

**Description:** As Tyler, I want to capture a task, note, list, or idea with minimal friction so thoughts do not get lost.

**Acceptance Criteria:**
- [ ] Capture mode clearly communicates the active type: Task, Note, List, or Idea.
- [ ] Task capture shows task-relevant fields without crowding note/list capture.
- [ ] Note and idea capture prioritize body text, tags, source URL, color, and pinned state.
- [ ] List capture supports list name plus one-item-per-line entry.
- [ ] Share-intent capture keeps the current URL extraction and `shared=true` behavior.
- [ ] Existing capture validation rules remain intact.
- [ ] Unit tests cover type switching and validation.

### US-003: Provide a useful Notes lane

**Description:** As Tyler, I want notes to be easy to browse and reopen so reference material and captured thoughts do not disappear into one mixed inbox.

**Acceptance Criteria:**
- [ ] Notes are visible as a dedicated lane or filter from the main daily workspace.
- [ ] Note cards show title/body preview, tags, pinned state, color, source URL signal, and sync state when present.
- [ ] Empty notes state gives a direct capture action.
- [ ] Opening a note lands on the existing type-dispatched detail screen or a refined note detail screen.
- [ ] Editing title, body, and tags still works for notes.
- [ ] Search can find notes by title/body using the existing FTS/LIKE rules.

### US-004: Provide a useful Tasks lane

**Description:** As Tyler, I want actionable tasks separated from reference notes so I can quickly decide what to do next.

**Acceptance Criteria:**
- [ ] Tasks are visible as a dedicated lane or filter from the main daily workspace.
- [ ] Task cards show title/body preview, priority, schedule, tags, pinned state, and sync state when present.
- [ ] Done and Drop actions remain obvious and hard to confuse.
- [ ] Pending-sync and sync-error tasks are visibly different from clean synced tasks.
- [ ] Task sorting preserves pinned-first, newest-first ordering unless a future explicit sort is approved.
- [ ] Search can find tasks within the task lane.

### US-005: Provide a useful Lists lane

**Description:** As Tyler, I want checklists to feel like real mobile lists so they can handle groceries, packing, follow-ups, and planning fragments.

**Acceptance Criteria:**
- [ ] Lists are visible as a dedicated lane or filter from the main daily workspace.
- [ ] List cards show list name, item preview, progress count, persistence state, pinned state, and sync state when present.
- [ ] List detail keeps tappable checklist rows with clear checked/unchecked visual state.
- [ ] Local checked state remains preserved during sync.
- [ ] Empty list state gives a direct list-capture action.
- [ ] The package does not add server-synced checklist editing unless the backend contract is explicitly extended.

### US-006: Improve search and filters for daily use

**Description:** As Tyler, I want to find recent items quickly so Inkwell can replace Google Keep-style lookup.

**Acceptance Criteria:**
- [ ] Search remains available from the main daily workspace.
- [ ] Search can be scoped by All, Tasks, Notes, Lists, Ideas, and Pending.
- [ ] Short query behavior continues to use SQL `LIKE`; 3+ character behavior continues to use FTS.
- [ ] No-results states explain which lane/filter was searched.
- [ ] Filter state is visible and easy to clear.

### US-007: Make sync and account state understandable

**Description:** As Tyler, I want to know whether my phone and Nexus are connected so I can trust the app during capture and review.

**Acceptance Criteria:**
- [ ] Main workspace surfaces connection status, pending uploads, and sync errors without hiding them behind unclear icons.
- [ ] Settings and System Health remain available.
- [ ] Auth-expired state gives a clear recovery path.
- [ ] Offline captures continue to save locally.
- [ ] No direct remote execution controls are added in this package.

## Functional Requirements

- FR-1: The package name is `Inkwell Daily Driver v1`.
- FR-2: The package must treat notes, tasks, lists, and ideas as the main mobile product.
- FR-3: The package must preserve the closed Inkwell Core behaviors unless a story explicitly changes them.
- FR-4: The app must support fast creation of Task, Note, List, and Idea captures.
- FR-5: The app must provide a clear way to browse Tasks, Notes, Lists, Ideas, Pending items, and All items.
- FR-6: The app must provide item detail and edit flows for title, body, and tags.
- FR-7: The app must keep list checkbox toggles local-only unless the backend list contract changes.
- FR-8: Search and filtering must remain type-aware.
- FR-9: Sync state must be readable in plain mobile UI, especially pending uploads, sync errors, and auth expiration.
- FR-10: Project pages must stay out of Daily Driver v1's main scope.
- FR-11: Remote operator controls must stay out of Daily Driver v1 except passive session/status visibility if it does not distract from the main app.

## Non-Goals

- No mobile project pages in the main Daily Driver v1 package.
- No direct remote "proceed", shell, command execution, or Claude/Codex control buttons.
- No full calendar replacement or planner UI.
- No widget package.
- No notification expansion package.
- No rich text editor.
- No binary attachment storage redesign.
- No multi-user or collaboration model.
- No backend contract changes unless a Daily Driver v1 story proves they are required.

## Design Considerations

- Treat Google Keep-level speed and polish as the product bar.
- Default to dark mode and a quiet, usable daily workspace.
- Favor fewer, clearer controls over dense command-center UI.
- Keep project/status/control-plane language out of the first-run daily app surface.
- Use explicit empty states instead of decorative placeholders.
- Keep touch targets at least 48dp.
- Use the existing Material 3 Compose foundation unless the scoped design pass approves a different pattern.

## Technical Considerations

- Existing Android stack: Kotlin, Jetpack Compose, MVVM, Hilt, Room, WorkManager.
- Existing Nexus APIs: `/api/capture`, `/api/inbox`, `/api/note/:uid`, `/api/lists`, and `/api/list/:uid`.
- Existing local schema supports `captureType`, `listName`, `listItemsJson`, `persistent`, `color`, `pinned`, `sourceUrl`, and `shared`.
- Existing search behavior is split between SQL `LIKE` for 1-2 character queries and FTS for 3+ character queries.
- Existing checklist checked state is local-only and preserved during sync.
- Physical-device verification matters before calling the package accepted.

## Success Metrics

- Tyler can capture a basic thought in under 10 seconds.
- Tyler can open the app and identify where to review tasks, notes, and lists in under 5 seconds.
- Notes, tasks, and lists each have a clear empty, populated, search, and sync-error state.
- Existing core tests still pass.
- Device verification confirms the main app feels good enough to use daily before secondary project goals begin.

## Open Questions

- Which device screenshot set is the acceptance proof for Daily Driver v1?

## Resolved Decisions

- Daily Driver v1 should use an expanded bottom navigation shell.
- D1 bottom nav should be Capture, Inbox, Tasks, Notes, Lists.
- Search should stay inside the browse lanes rather than becoming a bottom-nav item in D1.
- Ideas should remain available through the existing Inbox tab/filter model in D1.
- Pending should remain available through the existing Inbox tab/filter model in D1.
- The first implementation pass should be UI/navigation only unless initial-tab routing needs a small ViewModel helper.
- D2 is Type-First Capture Refinement.
- D2 is a refinement of existing `CaptureType` support, not a new DTO-only slice.
- Approved design output should be saved in this repo before broad visual implementation.
