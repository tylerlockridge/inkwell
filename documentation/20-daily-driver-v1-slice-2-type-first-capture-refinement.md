# Daily Driver v1 Slice 2: Type-First Capture Refinement

*Created: 2026-04-24 | Project: Inkwell | Package: Inkwell Daily Driver v1*

---

## Verdict

The second implementation slice is:

**D2 - Type-First Capture Refinement**

D2 starts from the verified D1 navigation shell and improves the capture entry
experience for Task, Note, List, and Idea captures.

---

## Roadmap Anchor

Sources:

- `tasks/prd-inkwell-daily-driver-v1.md`
- User story: `US-002: Make quick capture type-first`
- `documentation/19-daily-driver-v1-design-prep.md`

The PRD says capture mode must clearly communicate the active type, task capture
must show task-relevant fields, note and idea capture must prioritize body text and
metadata, and list capture must support list name plus one-item-per-line entry.

---

## Existing Baseline

D2 is a refinement slice, not a first DTO slice.

Already present in the codebase:

- `CaptureType { TASK, NOTE, LIST, IDEA }`
- segmented capture type buttons in `CaptureScreen`
- type-specific request mapping in `CaptureViewModel`
- list name and list-items state
- list validation requiring a name plus at least one non-blank item
- task/note/idea validation accepting text, source URL, or attachments
- request fields for `captureType`, `listName`, `items`, `persistent`, `color`,
  `pinned`, `sourceUrl`, and `shared`

---

## Implementation Scope

Expected areas:

- `app/src/main/kotlin/io/inkwell/ui/capture/CaptureScreen.kt`
- `app/src/main/kotlin/io/inkwell/ui/capture/CaptureToolbar.kt`
- `app/src/main/kotlin/io/inkwell/ui/capture/CaptureUiState.kt`
- `app/src/main/kotlin/io/inkwell/ui/capture/CaptureViewModel.kt`
- focused capture tests under `app/src/test/kotlin/io/inkwell/`
- `documentation/02-capture-flow.md`
- `documentation/06-ui-architecture.md` if navigation or screen structure changes

---

## Product Requirements

1. The active capture type must be obvious before Tyler starts typing.
2. Switching types must preserve useful draft text where that makes sense.
3. Task capture must keep task metadata available without crowding note/list modes.
4. Note capture must focus on body text, tags, source URL, color, and pinning.
5. Idea capture must feel distinct from a normal task and route as `idea`.
6. List capture must make the list name and item rows clear.
7. Share-intent capture must keep URL extraction and `shared=true` behavior.
8. Capture validation must stay strict enough to prevent empty captures.

---

## Design Gate

Before implementation, attach or reference approved design output from the
Claude / Anthropic UI design-tool lane.

Minimum design proof:

- capture screen with all four types represented
- task, note, list, and idea empty states
- list draft state with several item lines
- offline or pending-sync state
- failed-save state

If design output is not available, D2 can still proceed only as a narrow functional
cleanup that does not change the visual language.

---

## Explicit Non-Goals

- No mobile project pages.
- No remote session controls.
- No command prompt submission.
- No backend API change unless a failing test proves the current contract is wrong.
- No rich-text editor.
- No new attachment storage model.
- No full calendar or planner UI.
- No widget changes.

---

## Acceptance Criteria

- [ ] Capture type is visibly selected for Task, Note, List, and Idea.
- [ ] Task mode keeps task-relevant fields available.
- [ ] Note mode prioritizes body, tags, source URL, color, and pinned state.
- [ ] Idea mode routes as `idea` and does not silently behave like a task.
- [ ] List mode shows list name and one-item-per-line entry.
- [ ] List validation requires list name plus at least one non-blank item.
- [ ] Task / Note / Idea validation accepts meaningful text, source URL, or attachments.
- [ ] Share intent URL extraction and `shared=true` remain intact.
- [ ] Unit tests cover type switching, request mapping, and validation.
- [ ] `./gradlew --no-daemon test` passes.
- [ ] `./gradlew --no-daemon lint` passes.
- [ ] A debug or release APK builds.
- [ ] Screenshot proof shows the refined capture states.

---

## Stop Conditions

Stop and report before broadening scope if:

- the approved design requires backend fields the current API does not support
- type switching causes draft data loss that would surprise Tyler
- the capture screen becomes too crowded on the target Pixel viewport
- share-intent capture regresses
- validation allows empty or metadata-only captures
