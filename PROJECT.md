---
status: Active
phase: Inkwell Daily Driver v1 D2 defined
dashboardVisible: true
sourcePath: "C:\\Users\\tyler\\Documents\\Claude Projects\\Inkwell"
repoUrl: "https://github.com/tylerlockridge/inkwell"
repoSubdir: ""
ralph: true
testCmd: ""
qualityGates: ["./gradlew test", "./gradlew lint"]
lastRalphRun: "2026-03-06"
ralphRuns: 1
---

# Inkwell

Android app for capturing notes/tasks directly to an Obsidian vault inbox via a REST API.
Communicates with the Obsidian Dashboard Desktop server running on a DigitalOcean droplet.

## Quick Resume
**What this is:** Inkwell is the Android daily-driver app for Tyler's Nexus system: quick notes, tasks, lists, ideas, and share-intent capture into the Obsidian workflow.
**Current phase:** Inkwell Daily Driver v1 D2 defined.
**Current focus:** Attach approved design-tool output for D2 Type-First Capture Refinement, then implement against the verified D1 shell.
**Next action:** Save the approved D2 capture-design material in this repo and use `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md` as the implementation packet.
**Review state:** D1 navigation shell code and visual proof are complete; the misplaced Dashboard handoff has been localized into Inkwell docs.
**Verification:** Latest implementation proof 2026-04-22: `./gradlew --no-daemon test`, `./gradlew --no-daemon lint`, `./gradlew --no-daemon assembleRelease`, and `./gradlew --no-daemon installDebug` passed. Visual proof captured on `Medium_Phone_API_36.1` in `.visual-qa/reviews/d1-navigation-shell-2026-04-22-pass2/`.
**Start date:** 2026-02-26
**Last active:** 2026-04-22

## Locked Truth
- Inkwell Core is closed and device-verified.
- Daily-use capture, inbox, detail, settings, and system-health surfaces are the stable baseline.
- The first post-core package is `Inkwell Daily Driver v1`.
- Daily Driver v1 starts with notes, tasks, lists, ideas, search, detail/edit, and sync/account state.
- Project pages and direct remote-control features are secondary follow-ons, not the main package.

## Active Components
- **Capture + Share Intent Flow** | Phase: Core baseline | State: Stable
- **Inbox + Detail UI** | Phase: Core baseline | State: Stable
- **Settings + System Health** | Phase: Core baseline | State: Stable
- **Inkwell Daily Driver v1** | Phase: D2 defined | State: Awaiting approved D2 design output

## Planning Gaps
- Save approved D2 capture-design output in this repo.
- Implement D2 Type-First Capture Refinement after design proof is available.

## Open Decisions
- Which exact approved design artifact set should serve as D2 visual input before implementation?

## Recent Milestones
- 2026-04-24 - Misplaced Dashboard handoff localized into Inkwell docs and D2 Type-First Capture Refinement defined.
- 2026-04-22 - D1 visual proof captured on `Medium_Phone_API_36.1` in `.visual-qa/reviews/d1-navigation-shell-2026-04-22-pass2/`.
- 2026-04-22 - D1 navigation-shell code implemented and verified by test, lint, release build, and debug install; visual proof was initially blocked until the emulator was unlocked.
- 2026-04-22 - Daily Driver v1 PRD accepted and D1 navigation-shell slice chosen.
- 2026-03-28 - Tyler device verification passed and Inkwell Core was closed.
- 2026-03-28 - Pinned-first inbox sorting landed.

## Dependencies
- **Upstream:** Nexus capture/list/inbox APIs, Android app code, and business-rules docs must agree.
- **Downstream:** secondary project pages, feedback capture, remote session controls, notifications, and widgets wait behind the Daily Driver v1 definition.
- **Global:** Device proof matters more than unit-test confidence for daily-driver mobile claims.

## Reference
- `tasks/prd-inkwell-daily-driver-v1.md` - first post-core package PRD.
- `documentation/16-daily-driver-v1-scope.md` - scope boundary between main app goals and secondary Nexus goals.
- `documentation/17-daily-driver-v1-slice-1-navigation-shell.md` - first implementation slice packet.
- `documentation/18-inkwell-readiness-audit-and-handoff.md`, `documentation/19-daily-driver-v1-design-prep.md`, and `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md` - localized handoff, design prep, and D2 packet.
- `documentation/02-capture-flow.md` - capture contract and flow.
- `documentation/03-sync-strategy.md` - sync behavior.
- `documentation/06-ui-architecture.md` - screen and navigation model.
- `documentation/08-business-rules.md` - live product rules.

## AI Watchouts
- Do not confuse "core closed" with "project done forever."
- Do not start with project pages; Daily Driver v1 starts with the main notes/tasks/lists app.
- Keep remote execution out of Inkwell until prompt submission, output visibility, guardrails, and action logging exist.
- Use fresh device evidence before reopening stable core behavior.

<!-- QUICK-RESUME-UPDATED: 2026-03-28 I17-phase-closed -->
## Historical Quick Resume
**Last Active:** 2026-03-28
**Current Phase:** Inkwell Core — **CLOSED** (I6.4–I15, verified 2026-03-28)
**Status:** Core phase complete. Tyler device verification passed. No open work items in core scope.

### Closed Core (I6.4–I15)
- UI overhaul: all 5 daily-use screens (Inbox, Detail, Capture, Settings, System Health)
- Slice 3 end-to-end: DTO → Room v6 → sync → detail/inbox UI → capture authoring → share-intent autofill
- Capture validation: send disabled when empty, snackbar fallback
- Pinned-first inbox sorting: all views, consistent across tabs/search
- `shared` semantics: system-derived (share intent + server CaptureMetadata)
- 370+ unit tests across 41 files; 14 review screenshots
- Tyler device verification passed 2026-03-28

### Deferred Backlog (non-core, not blocking)

| Item | Category | Notes |
|------|----------|-------|
| LockScreen polish | Low-priority polish | Minimal biometric prompt, system-driven, rarely seen |
| Share intent with images | Deferred edge case | Only `text/*` MIME types parsed |

*These items are optional future work. Neither is required for the core product to function correctly.*

**⚠️ Known test gotcha (Android 16):**
- Always uninstall release APK before running `connectedAndroidTest` (signature mismatch)
- `Espresso.closeSoftKeyboard()` deadlocks on Android 16 — use `composeRule.waitForIdle()` instead
- Runtime permissions must be pre-granted via `GrantPermissionRule` (camera, media, notifications)

### Session 2026-03-28 — I17: Inkwell Core Phase Closure
- ✅ Tyler device verification passed — all checks in `I12-VERIFICATION-FLOW.md` confirmed
- ✅ PROJECT.md marked **CLOSED** — core phase I6.4–I15 complete and verified
- ✅ `13-ui-overhaul-brief.md` marked CLOSED with verification date
- ✅ `15-pinned-sorting-decision.md` marked verified on-device
- ✅ `I12-VERIFICATION-FLOW.md` marked PASSED
- No code changes — closure documentation only
- Files modified: `PROJECT.md`, `13-ui-overhaul-brief.md`, `15-pinned-sorting-decision.md`, `I12-VERIFICATION-FLOW.md`

### Session 2026-03-28 — I16: Final Device Verification + Phase Closure
- ✅ Verification flow updated: now covers pinned sorting (step 1+3), capture validation (step 3), share-intent autofill (step 4)
- ✅ `13-ui-overhaul-brief.md` completion summary updated through I15 (was I12): 12 features listed, 2 deferred items
- ✅ `PROJECT.md` Quick Resume rewritten as closure state (not milestone stream)
- ✅ Device disconnected during session — pinned-sorting screenshot (`inkwell_i15_inbox_pinned`) not captured
- ✅ Evidence map finalized (see report below)
- Files modified: `I12-VERIFICATION-FLOW.md`, `13-ui-overhaul-brief.md`, `PROJECT.md`

### Session 2026-03-28 — I15: Implement Pinned-First Inbox Sorting (Option A)
- ✅ 4 NoteDao queries updated: `ORDER BY created DESC` → `ORDER BY pinned DESC, created DESC`
  - `getInboxNotes()` — main inbox (All tab + type-filtered tabs)
  - `getPendingSyncNotes()` — Pending tab
  - `searchNotes()` — LIKE search (1-2 char queries)
  - `searchNotesFts()` — FTS search (3+ char queries)
- ✅ 10 new tests in `PinnedSortingTest`: core ordering, tab filtering preserves order, search respects order, edge cases
- ✅ Quality gates: all tests passing, clean compile
- ✅ Business Rule 15 added to `08-business-rules.md` (subsequent rules renumbered)
- ✅ `15-pinned-sorting-decision.md` updated: Option A marked as implemented
- No UI changes, no migration, no new preferences
- No unexpected side effects — in-memory `maybeFilterByType()` preserves DB sort order
- Files modified: `NoteDao.kt`, `08-business-rules.md`, `09-testing-strategy.md`, `15-pinned-sorting-decision.md`, `PROJECT.md`
- Files created: `PinnedSortingTest.kt`

### Session 2026-03-28 — I14: Inbox Pinned Sorting Decision Packet
- ✅ Created `documentation/15-pinned-sorting-decision.md`
- ✅ Analyzed 4 options: (A) pinned floats in all views, (B) All tab only, (C) user toggle, (D) visual only
- ✅ Scored on 6 dimensions: scanability, tab consistency, mental model, implementation, surprise risk, product fit
- ✅ Recommendation: **Option A** — pinned floats to top in all views (4 SQL changes, 15 min implementation)
- ✅ Implementation shape documented: exact SQL changes, test plan, migration/user-facing considerations
- ✅ No-regret prep work assessed: none exists (entire change is 4 SQL clauses)
- Awaiting Tyler decision before implementation
- Files created: `15-pinned-sorting-decision.md`
- Files modified: `PROJECT.md`

### Session 2026-03-28 — I13: Capture Empty-Text Validation + Safe Capture Rules
- ✅ `CaptureUiState.isValid` expanded: Task/Note/Idea valid with text OR sourceUrl OR attachments; List valid with name AND real items
- ✅ Send button now **disabled** when `!isValid` (was only disabled during submission)
- ✅ `CaptureViewModel.onCapture()` snackbar fallback: "Add some content to capture" if somehow called while invalid
- ✅ List validation tightened: `listItems.lines().any { it.isNotBlank() }` (was `listItems.isNotBlank()`)
- ✅ 28 new tests in `CaptureValidationTest`: whitespace-only, title-only, sourceUrl-only, attachment-only, metadata-only, combined sources, list edge cases (blank lines, one real item among blanks), share-intent URL-only
- ✅ Quality gates: all tests passing, clean compile
- ✅ Business Rule 14 added to `08-business-rules.md` (subsequent rules renumbered 15–20)
- Design decisions:
  - sourceUrl alone IS valid content (protects share-intent URL-only flow)
  - Attachments alone ARE valid content (photo capture without text is reasonable)
  - Cosmetic metadata alone (pinned/color/tags/priority) is NOT valid (prevents junk captures)
  - List requires at least one non-blank item line (was just `isNotBlank()` check)
- Files modified: `CaptureUiState.kt`, `CaptureViewModel.kt`, `CaptureToolbar.kt`, `02-capture-flow.md`, `08-business-rules.md`, `09-testing-strategy.md`, `PROJECT.md`
- Files created: `CaptureValidationTest.kt`

### Session 2026-03-28 — I12: UI Closeout Pack + Device Evidence
- ✅ Built and installed latest debug APK on Pixel 10 Pro XL (Android 16)
- ✅ **7 review screenshots captured via ADB:**
  - `inkwell_i10_capture_default_2026-03-28.png` — Capture screen with I6.7 overhaul
  - `inkwell_i10_capture_metadata_expanded_2026-03-28.png` — Expanded toolbar with all icons visible
  - `inkwell_i10_capture_extras_panel_2026-03-28.png` — ExtrasPanel with pin toggle, URL field, color picker
  - `inkwell_i10_capture_pinned_2026-03-28.png` — Pin toggle ON, URL field, color picker visible
  - `inkwell_i10_capture_color_2026-03-28.png` — Orange color selected, "Orange" chip visible
  - `inkwell_i9_settings_default_2026-03-28.png` — Settings with I9 overhaul (bold title, uppercase sections, restrained health row)
  - `inkwell_i9_systemhealth_default_2026-03-28.png` — System Health with I9 overhaul (tighter StatusRows, compact StatBlock)
  - `inkwell_i12_inbox_current_2026-03-28.png` — Current Inbox state with all type badges, pending indicators
- ✅ Tyler device-verification flow written: `.visual-qa/reviews/I12-VERIFICATION-FLOW.md` (~5 min on-device)
- ✅ Docs reconciled into closeout state:
  - `13-ui-overhaul-brief.md`: completion summary table, deferred backlog, test coverage update, status COMPLETE
  - `09-testing-strategy.md`: test counts updated (39 files, 330+ tests)
  - `PROJECT.md`: clean Quick Resume with categorized backlog table
- ✅ Remaining backlog categorized: 2 product decisions, 1 bug/gap, 1 deferred edge case
- No code changes — docs + evidence only
- Files modified: `PROJECT.md`, `09-testing-strategy.md`, `13-ui-overhaul-brief.md`
- Files created: `I12-VERIFICATION-FLOW.md`, 8 review screenshots

### Session 2026-03-28 — I11: Share Intent Source URL Autofill + Shared Semantics
- ✅ `ShareIntentParser.kt` enhanced: URL extraction via regex (`https?://` first match), body cleaning (URL-only → strip from body, prose+URL → preserve)
- ✅ `ShareData` expanded with `sourceUrl: String?` field
- ✅ `MainActivity.kt`: passes `sharedSourceUrl` and `isFromShareIntent` to `CaptureNavHost`
- ✅ `CaptureNavHost.kt`: threads `sharedSourceUrl` and `isFromShareIntent` to `CaptureScreen`
- ✅ `CaptureScreen.kt`: pre-fills `sourceUrl` from share data, calls `onShareIntentReceived()` for shared flag
- ✅ `CaptureUiState.kt`: added `shared` (Boolean) field
- ✅ `CaptureViewModel.kt`: added `onShareIntentReceived()` (sets shared=true), threaded `shared` into `onCapture()`
- ✅ `shared` semantics defined: "this capture originated from an Android share intent" — system-derived, not user-editable
- ✅ 22 tests in `ShareIntentParserTest`: URL extraction (https, http, query params, fragments, multiple, none, mailto/ftp exclusion), body cleaning, integration scenarios
- ✅ Quality gates: all tests passing, clean compile
- Design decisions:
  - URL extraction: simple first-match regex, predictable and explainable
  - Body cleaning: URL-only text → body stripped (title suffices); prose+URL → full text preserved
  - `shared` not user-editable: system-derived flag with clear automatic meaning
  - Manual sourceUrl entry from I10 still works — share-intent auto-fill is additive
- Files modified: `ShareIntentParser.kt`, `MainActivity.kt`, `CaptureNavHost.kt`, `CaptureScreen.kt`, `CaptureUiState.kt`, `CaptureViewModel.kt`, `02-capture-flow.md`, `04-data-model.md`, `06-ui-architecture.md`, `13-ui-overhaul-brief.md`, `PROJECT.md`
- Files modified (tests): `ShareIntentParserTest.kt` (rewritten with 22 tests)

### Session 2026-03-28 — I10: Capture Metadata Authoring (color / pinned / sourceUrl)
- ✅ `CaptureUiState.kt`: added `pinned` (Boolean), `sourceUrl` (String), `color` (CaptureColor?), `hasExtrasMetadata` computed property
- ✅ `CaptureColor` enum: curated 6-color palette (Red/Orange/Yellow/Green/Blue/Purple) with hex + Compose Color
- ✅ `ToolbarPanel.Extras`: new panel accessed via `...` (MoreHoriz) icon — visible for all capture types
- ✅ `ExtrasPanel` composable: pin toggle row, source URL text field, color picker with circle swatches
- ✅ `CaptureViewModel`: `onPinnedToggle()`, `onSourceUrlChange()`, `onColorChange()` methods
- ✅ `CaptureViewModel.onCapture()`: threads `color.hex`, `pinned`, `sourceUrl` into `CaptureRepository.capture()`
- ✅ `ActiveMetadataChips`: pinned/sourceUrl/color shown as dismissible chips
- ✅ `CaptureScreen`: wired all new callbacks to toolbar and chips
- ✅ Batch mode: extras preserved across batch captures (intentional sticky metadata)
- ✅ 14 new tests: `CaptureExtrasTest` — state defaults, hasExtrasMetadata, CaptureColor enum, mutations, batch/reset
- ✅ Quality gates: all tests passing, clean compile
- Design decision: `shared` is NOT editable at capture time — server-authoritative, no clear user action meaning
- Slice 3 milestone: **complete end-to-end** (I7: DTO → I8: Room+sync+UI → I10: capture authoring)
- Files modified: `CaptureUiState.kt`, `CaptureViewModel.kt`, `CaptureToolbar.kt`, `CaptureScreen.kt`, `04-data-model.md`, `06-ui-architecture.md`, `13-ui-overhaul-brief.md`, `PROJECT.md`
- Files created: `CaptureExtrasTest.kt`

### Session 2026-03-28 — I9: Settings + System Health Visual Overhaul
- ✅ **SettingsScreen.kt** fully redesigned — presentation-only, all behavior preserved
  - Bold-dark title (headlineSmall.Bold) matching Inbox/Detail/Capture
  - Flat TopAppBar (containerColor = surface)
  - System Health entry: restrained navigation row with forward arrow (was hero card)
  - Section headers: uppercase tracking labels (11sp, Bold, letterSpacing 1sp) — Detail pattern
  - All cards: RoundedCornerShape(10dp), padding 14h x 10v
  - Section spacing: 10dp (was 16dp)
  - "Last synced" as metadata row with label/value hierarchy
  - Buttons: 40dp height, labelMedium text
  - Version branded: "Inkwell 2.3.0"
- ✅ **SettingsConnectionCard.kt** unified — both connected/disconnected states on surfaceContainerLow
  - Connected: inline green dot + TextButton disconnect (was OutlinedButton on primaryContainer)
  - Disconnected: inline amber dot + "Not connected" (was Warning icon on tertiaryContainer)
  - Status indicators: 6-8dp dots (subtler)
- ✅ **SettingsComponents.kt** updated
  - SectionHeader: uppercase tracking label (was bar + text)
  - SettingsToggle: bodyMedium.Medium label (was bodyLarge), 2dp vertical padding (was 4dp)
- ✅ **ExportSection.kt** tightened — RoundedCornerShape(10dp), 14h x 10v padding, 40dp buttons, "JSON"/"CSV" labels
- ✅ **SystemHealthScreen.kt** fully redesigned — instrument panel language
  - Bold-dark title matching all accepted screens
  - SectionLabel: uppercase tracking labels (matching Detail/Settings)
  - SubsectionLabel: 10sp Bold primary for Devices/Folders subsections
  - HealthCard: RoundedCornerShape(10dp), padding 14h x 10v, spacedBy(4dp)
  - StatusRow: bodySmall for both sides (was bodyMedium), placeholder dimming
  - Status dots: 6dp (was 8-10dp)
  - StatBlock: 18dp icon (was 24dp), titleSmall.Bold (was titleMedium)
  - Folder progress: 3dp height (was 4dp)
  - Restart button: 40dp height, labelMedium text
  - Dividers: outlineVariant at 0.3 alpha (subtler)
  - Section spacing: 10dp (was 16dp)
- ✅ Quality gates: all tests passing, clean compile
- Design milestone: **All 5 daily-use screens now share the accepted Inkwell visual language**
  - Inbox: 24/30 PASS (I6.4)
  - Detail: 27/30 PASS (I6.5)
  - Capture: 26/30 PASS (I6.7)
  - Settings: I9 (visual-only, behavior preserved)
  - System Health: I9 (visual-only, behavior preserved)
- Files modified: `SettingsScreen.kt`, `SettingsConnectionCard.kt`, `SettingsComponents.kt`, `ExportSection.kt`, `SystemHealthScreen.kt`, `06-ui-architecture.md`, `13-ui-overhaul-brief.md`, `PROJECT.md`

### Session 2026-03-28 — I8: Slice 3 Local Persistence + Detail Surfacing
- ✅ Room schema v5→v6 migration: added `color` (TEXT), `pinned` (INTEGER), `source_url` (TEXT), `shared` (INTEGER) columns
- ✅ `NoteEntity` updated with 4 new fields, all with safe defaults (null/false)
- ✅ `InboxSyncEngine`: `shared` consumed from server `CaptureMetadata`; `color`/`pinned`/`sourceUrl` preserved as local-only during sync
- ✅ `ResolvedCaptureMetadata` expanded with `shared` field
- ✅ `CaptureRepository`: all 4 fields persisted in both online and offline capture paths
- ✅ Detail screen: status indicator row shows "Pinned" (with pin icon) and "Shared" (with share icon) inline with pending banner
- ✅ Detail screen: `ColorDot` renders color swatch in metadata card; `SourceUrlRow` renders tappable source URL
- ✅ All 4 type-dispatched detail views (Task/Note/List/Idea) show color + sourceUrl in their Details/Status cards
- ✅ Inbox screen: pin icon shown in timestamp row for pinned cards (subtle, not noisy)
- ✅ 12 new tests: `Slice3PersistenceTest` — entity defaults, sync shared consumption, server-over-local priority, fallback paths
- ✅ Existing I7 tests pass (SyncCaptureMetadataTest, CaptureRequestSlice3Test)
- ✅ Quality gates: all tests passing, clean compile
- Design decisions:
  - `color`/`pinned`/`sourceUrl` are local-only — server does not return these in detail response; preserved through sync
  - `shared` is server-authoritative — consumed from `CaptureMetadata.shared` during sync, falls back to local
  - UI surfacing is restrained: no inbox card color tinting (would be noisy), no shared badge in inbox (low signal)
  - Source URL is tappable in detail (opens browser via `LocalUriHandler`)
- Deferred: Capture screen does not yet let user set color/pinned/sourceUrl; inbox sorting by pinned not implemented
- Files modified: `NoteEntity.kt`, `AppDatabase.kt`, `DatabaseModule.kt`, `InboxSyncEngine.kt`, `CaptureRepository.kt`, `NoteDetailScreen.kt`, `InboxScreen.kt`, `04-data-model.md`, `06-ui-architecture.md`, `PROJECT.md`
- Files created: `Slice3PersistenceTest.kt`, `app/schemas/io.inkwell.data.local.AppDatabase/6.json`

### Session 2026-03-26 — I7: Nexus captureMetadata + Slice 3 DTO Expansion
- ✅ Added `CaptureMetadata` + `CaptureMetadataItem` DTOs to `NoteDetailResponse.kt`
- ✅ `InboxSyncEngine.resolveCaptureMetadata()` — consumes server `captureMetadata` as source of truth
  - Normalizes brainstorming ideas: server `captureType="task"` + `kind="brainstorming"` → local `captureType="idea"`
  - Serializes server list items to local ChecklistItem format
  - Preserves local checked state during sync (merges by item text match)
  - Falls back to existing local metadata if server lacks `captureMetadata` (backward compatible)
- ✅ Expanded `CaptureRequest.kt` with Slice 3 fields: `color`, `pinned`, `sourceUrl`, `shared`
- ✅ Threaded Slice 3 fields through `CaptureRepository.capture()` → `CaptureRequest` construction
- ✅ Updated `BrowseType.kt` doc comment — removed stale "server doesn't return captureMetadata" note
- ✅ 14 new tests: 9 sync metadata tests (SyncCaptureMetadataTest) + 5 DTO serialization tests (CaptureRequestSlice3Test)
- ✅ Quality gates: all tests passing, clean compile
- Deferred: Slice 3 fields lack Room columns and UI support (DTO/network layer only)
- Files modified: `NoteDetailResponse.kt`, `InboxSyncEngine.kt`, `CaptureRequest.kt`, `CaptureRepository.kt`, `BrowseType.kt`, `documentation/04-data-model.md`, `PROJECT.md`
- Files created: `SyncCaptureMetadataTest.kt`, `CaptureRequestSlice3Test.kt`

### Session 2026-03-26 — I6.7: Capture Visual Overhaul (Pilot Slice 3)
- ✅ `CaptureScreen.kt` + `CaptureToolbar.kt` visual polish — presentation-only, all behavior preserved
- Visual changes:
  - **Connection banner**: matches Inbox style — compact red-tinted "Not connected / Settings →"
  - **Type switcher**: tighter horizontal padding (12→16dp), consistent with page margins
  - **Writing surface**: RoundedCornerShape(12dp) (was 16dp), tighter vertical padding (8→4dp)
  - **Placeholder text**: bold Title at 18sp, subtler body hint at bodyMedium, lower alpha
  - **Line height**: 24sp (was 26sp) for tighter text rhythm
  - **Toolbar bottom sheet**: 20dp corner radius (was 28dp), smaller drag handle (28x3dp vs 32x4dp)
  - **Metadata chips**: 6dp horizontal spacing (was 8dp), 11sp label text, tighter vertical arrangement
- ✅ Quality gates: all tests passing, clean compile
- ✅ 3 review screenshots: default, list mode, metadata expanded
- Attachment state screenshot deferred (requires gallery/camera interaction via ADB)
- Files modified: `CaptureScreen.kt`, `CaptureToolbar.kt`, `documentation/06-ui-architecture.md`, `PROJECT.md`

### Session 2026-03-26 — I6.7r: Capture Review + Three-Screen Pilot Acceptance
- ✅ Structured Capture review verdict: **PASS (26/30)**
- Criteria:
  - Information hierarchy: 4/5
  - Urgency differentiation: 4/5
  - Interaction feedback: 5/5
  - Density vs clarity: 4/5
  - Accessibility baseline: 4/5
  - Anti-pattern check: 5/5
- INFO only:
  - Attachment state not reviewed in this pass (manual device verification still useful)
  - List mode retains stock `OutlinedTextField` styling (optional future polish)
- ✅ Three-screen daily-use overhaul accepted overall:
  - Inbox: 24/30 PASS
  - Detail: 27/30 PASS
  - Capture: 26/30 PASS
- Next product priority shifted back to functional roadmap:
  - consume server `captureMetadata` in synced detail flow
  - bundle Slice 3 DTO expansion (`color`, `pinned`, `sourceUrl`, `shared`)

### Session 2026-03-26 — I6.6: Package Rename Cleanup
- ✅ Removed 7 stale `uidump*.xml` debug artifacts (all from 2026-03-03)
- ✅ Updated `INSTALL.md`: "Obsidian Capture" → "Inkwell", `com.obsidiancapture` → `io.inkwell`, `android/` path → root
- ✅ Updated `documentation/11-deployment-release.md`: APK output path, package name, schema dir, historical APK note
- ✅ Updated `ARCHITECTURE.md`: source path `com/obsidiancapture/` → `io/inkwell/`
- ✅ Updated `memory/MEMORY.md`: package and testInstrumentationRunner references
- ✅ Left intentionally unchanged:
  - `app/google-services.json` — Firebase project ID is `obsidian-capture-11a09` (can't rename)
  - `prd.json` — historical Ralph PRD, references are in story notes
  - `llm-audit-action-plan.md` — historical audit action plan
  - `CODEX-AUDIT-BRIEF.md` — explicitly documents the rename commit
  - `PROJECT.md` session logs — historical record of the rename and pre-rename work
  - `app/schemas/com.obsidiancapture.data.local.AppDatabase/` — Room schema history (pre-rename DB versions)
- Files removed: `uidump.xml` through `uidump7.xml` (7 files)
- Files modified: `INSTALL.md`, `documentation/11-deployment-release.md`, `ARCHITECTURE.md`, `memory/MEMORY.md`, `PROJECT.md`

### Session 2026-03-26 — I6.5: Detail Visual Overhaul (Pilot Slice 2)
- ✅ **NoteDetailScreen.kt** fully redesigned — presentation-only, all behavior preserved
- Visual changes:
  - **Title bar**: type badge (matching inbox colors) + type name instead of "X Detail" text
  - **Pending state**: inline "Pending sync" amber label at top of content (not buried in cards)
  - **Section labels**: uppercase tracking text (SCHEDULE, STATUS, DETAILS) replace icon+text card headers
  - **Metadata cards**: `MetadataCard` with RoundedCornerShape(10dp), tighter padding (14h x 10v)
  - **Metadata rows**: bodySmall with 3dp vertical padding (was bodyMedium with 4dp)
  - **Title**: headlineSmall bold with tight letter-spacing (was headlineMedium)
  - **Action buttons**: 44dp height (was 48dp), outline border 50% alpha, SemiBold "Done"
  - **Checklist**: "Saved locally" at 10sp, tighter card padding (12dp)
  - **Idea framing**: "Brainstorm" label in amber matching idea badge color
  - **Tags**: 12sp label text, 6dp chip spacing
- ✅ Quality gates: all tests passing, clean compile
- ✅ 4 review screenshots in `.visual-qa/reviews/`
- Files modified: `NoteDetailScreen.kt`, `documentation/06-ui-architecture.md`, `documentation/13-ui-overhaul-brief.md`, `PROJECT.md`

### Session 2026-03-26 — I6.5r: Detail Review + Pilot Acceptance
- ✅ Structured review verdict: **PASS** (27/30)
- Tier 1 gate cleared: Information hierarchy 5, Urgency differentiation 4, Accessibility baseline 4
- Tier 2+3 cleared: Interaction feedback 4, Density vs clarity 5, Anti-pattern check 5
- Only non-blocking finding: optional refinement for `"Auto"` placeholder styling in task metadata
- Pilot accepted overall:
  - Inbox pilot: **PASS** (24/30)
  - Detail pilot: **PASS** (27/30)
- Inbox + Detail are now the accepted visual pattern source for future Inkwell screens

### Session 2026-03-26 — I6.4b: Inbox Polish (Pending-State + Note Preview)
- ✅ **F-01 (MEDIUM) fixed:** Pending-sync cards now get a 5% amber background tint — subtly warmer than synced cards, visible at scan speed without being noisy
- ✅ **F-03 (LOW) fixed:** NOTE-type cards now show 2-line body preview (other types stay 1-line), using BrowseType check
- ✅ Quality gates: all tests passing, clean compile
- ✅ 2 updated review screenshots: `inkwell_inbox_all_default_2026-03-26.png`, `inkwell_inbox_tasks_populated_2026-03-26.png`
- Both review findings from I6.4r are now closed
- Files modified: `InboxScreen.kt`, `documentation/06-ui-architecture.md`, `documentation/13-ui-overhaul-brief.md`, `PROJECT.md`

### Session 2026-03-25 — I6.4a: Inbox Slice Closeout
- ✅ Fixed empty-state review screenshot: `inkwell_inbox_empty_state_2026-03-25.png` now shows Ideas tab empty (was search-empty)
- ✅ Retained search-empty as bonus artifact: `inkwell_inbox_search_empty_2026-03-25.png`
- ✅ Updated `documentation/06-ui-architecture.md` with I6.4 overhaul status
- ✅ Updated `documentation/13-ui-overhaul-brief.md`: Inbox slice marked SHIPPED, review verdict recorded, next slice identified
- ✅ Marked idea item "Done" on device to produce clean empty-state tab
- No visual redesign changes — presentation code untouched
- Files modified: `documentation/06-ui-architecture.md`, `documentation/13-ui-overhaul-brief.md`, `PROJECT.md`
- Files in `.visual-qa/reviews/`: empty_state replaced, search_empty added (6 total)

### Session 2026-03-25 — I6.4: Inbox Visual Overhaul (Pilot Slice 1)
- ✅ **InboxScreen.kt** fully redesigned — presentation-only, zero behavior changes
- Visual changes:
  - **Header**: bold "Inbox" with tighter letter-spacing, sync button hidden during search
  - **Connection banner**: compact single-line with "Settings →" action text, error-tinted background
  - **Tab row**: counts inline with names ("Tasks 4"), no badge circles; bold selected tab; no divider
  - **Card density**: tighter padding (14dp→10dp vertical, 8dp→4dp card spacing), 7 items visible vs 6 before
  - **Type badge**: larger (11sp bold), stronger fill (0.18 alpha), rounded-4dp shape
  - **Body preview**: single-line max; lists show "Milk · Eggs · Bread" dot-separated
  - **Sync indicators**: hidden when synced, 14dp pending/error icons only when needed
  - **Timestamps**: shortened ("1h" not "1h ago"), 11sp
  - **Tags**: inline in metadata row as text, not separate row
  - **Empty state**: smaller diamond (80dp vs 120dp), bolder title, shorter copy, "Inbox clear" wording
  - **Swipe dismiss**: rounded clip on background
  - **FAB**: explicit primary/onPrimary colors
  - **List padding**: 80dp bottom to clear FAB
- ✅ Quality gates: all unit tests passing, clean compile
- ✅ 5 review screenshots captured in `.visual-qa/reviews/`
- Files modified: `InboxScreen.kt`, `PROJECT.md`

### Session 2026-03-25 — I6.3a: Idea Classification Fix + Baseline Refresh
- ✅ **Bug fix:** `CaptureViewModel.kt` line 219: `CaptureType.IDEA -> "task"` changed to `-> "idea"`
- ✅ 7 new unit tests in `CaptureTypeMappingTest.kt` covering all 4 type mappings + kind mappings
- ✅ All tests pass, clean compile
- ✅ Refreshed baselines:
  - Shot 8 (`detail_idea`) replaced: now shows "Idea Detail" with "Brainstorm / Idea" label
  - Shot 9 (`inbox_ideas_populated`) added: Ideas tab with correctly classified idea item
- ✅ `capture-metadata.md` updated with refresh notes
- Files modified: `CaptureViewModel.kt`, `capture-metadata.md`, `PROJECT.md`
- Files created: `CaptureTypeMappingTest.kt`, `inkwell_inbox_ideas_populated_2026-03-25.png`

### Session 2026-03-25 — I6.3: Baseline Screenshot Capture
- ✅ Built and installed debug APK (io.inkwell v2.4.0, versionCode 12) on Pixel 10 Pro XL (Android 16)
- ✅ Created 6 test items via on-device capture: 2 tasks, 2 notes, 1 list (4 items), 1 idea
- ✅ Captured 8 baseline screenshots via `adb exec-out screencap -p`:
  - 4 inbox states (All, Tasks, Lists, Ideas/empty)
  - 4 detail states (Task, Note, List, Idea/brainstorm)
- ✅ `capture-metadata.md` written with device/build/shot details
- ⚠️ **Classification bug found:** `CaptureViewModel` sends `captureType: "task"` for `CaptureType.IDEA` (line 219). BrowseType classifier prioritizes `captureType` over `kind`, so ideas are classified as tasks. Shot 8 shows "Task Detail" with Kind="brainstorming" instead of "Idea Detail".
- No source code modified
- Files created: 8 `.png` baselines + `capture-metadata.md` in `.visual-qa/baselines/`
- Files modified: `PROJECT.md`

### Session 2026-03-25 — I6.2: Baseline Structure + Capture Plan
- ✅ Created `.visual-qa/baselines/`, `.visual-qa/reviews/`, `.visual-qa/archive/` directories
- ✅ Created `documentation/14-ui-baseline-capture-plan.md` — 8-screenshot shot list, capture order, ADB commands, metadata template
- Screenshots not yet captured — requires debug APK install + test data population on device
- No source code modified
- Files created: `documentation/14-ui-baseline-capture-plan.md`, `.visual-qa/` directory tree
- Files modified: `PROJECT.md`

### Session 2026-03-25 — I6.1: UI Overhaul Brief
- ✅ Created `documentation/13-ui-overhaul-brief.md` — Inkwell-specific overhaul brief adapted from workspace template
- Scope: Inbox + Detail screens (first pilot); Capture/Settings/SystemHealth deferred
- Documented 9 UX strengths to preserve and 10 weaknesses to improve
- 6 open questions for Tyler: aesthetic direction, visual anchor, competitor refs, card density, sync display, color palette
- External reference slots are placeholders — Tyler supplies links before implementation
- Baseline capture plan ready for I6.2 (8 screenshots across inbox/detail states)
- Recommended first implementation slice: inbox card redesign (type badge prominence, density, sync labels)
- No source code or `.visual-qa/` files were modified
- Files created: `documentation/13-ui-overhaul-brief.md`
- Files modified: `PROJECT.md`

### Session 2026-03-25 — I5b: Chrome Extension Token Remediation (Verified)
- ✅ Verified: extension already remediated in a prior Nexus session
- ✅ No hardcoded token in any extension file (grep confirmed)
- ✅ `chrome.storage.local` is the only active storage path for `authToken` and `serverUrl`
- ✅ `chrome.storage.sync` references are migration-only (read old → write local → remove sync)
- ✅ `host_permissions` scoped to `https://tyler-capture.duckdns.org/*` only
- ✅ Nexus `documentation/08-token-auth.md` already has I5b section documenting the shipped model
- ✅ Nexus `documentation/07-web-ui.md` already describes extension with `chrome.storage.local` + manual token
- ✅ Syntax check: all 3 JS files pass `node --check`, manifest.json valid
- ✅ Both critical audit findings (Android I5a + Chrome I5b) are now closed
- No files modified (extension was already in target state)

### Session 2026-03-25 — I5a: Android Per-Device Auth Remediation
- ✅ **CRITICAL FIX:** Removed `BuildConfig.DEFAULT_AUTH_TOKEN` from `build.gradle.kts` — app no longer ships with a shared bearer secret
- ✅ Removed all 4 `BuildConfig.DEFAULT_AUTH_TOKEN` fallback references in `PreferencesManager` — blank token = unauthenticated, no silent fallback
- ✅ Deleted stale Google auth code: `GoogleAuthDto.kt`, `GoogleAuthDtoTest.kt`, `exchangeGoogleToken()` method in `CaptureApiService`
- ✅ Removed `UnauthenticatedClient` qualifier and HttpClient provider from `NetworkModule` (only existed for Google auth exchange)
- ✅ Removed Credential Manager dependencies (`credentials`, `credentials-play`, `googleid`) from `build.gradle.kts`
- ✅ Settings connection card: token entry is now always visible (collapsible toggle removed — manual token is the only auth path)
- ✅ Rewrote `documentation/01-authentication-security.md` — removed false Google Sign-In claims, documents actual per-device manual token model
- ✅ Rewrote `documentation/10-security-checklist.md` — corrected `DEFAULT_AUTH_TOKEN` removal date, added Chrome extension as remaining CRITICAL item
- ✅ Codex follow-up cleanup removed stale Credential Manager keep rules from `app/proguard-rules.pro` and removed stale `GoogleAuthDtoTest` mention from `documentation/09-testing-strategy.md`
- ✅ Quality gates: all unit tests passing, clean compile
- Files modified: `build.gradle.kts`, `PreferencesManager.kt`, `CaptureApiService.kt`, `NetworkModule.kt`, `SettingsConnectionCard.kt`, `documentation/01-authentication-security.md`, `documentation/10-security-checklist.md`, `PROJECT.md`
- Files deleted: `GoogleAuthDto.kt`, `GoogleAuthDtoTest.kt`
- ⚠️ Chrome extension still ships shared bearer token — requires separate I5b pass in the extension repo

### Session 2026-03-25 — I4: Local List Checkbox Interaction
- ✅ New `ChecklistItem` data class + `ChecklistItems` parser/serializer with dual-format support
  - Legacy: `["a","b"]` → all unchecked (backward compatible)
  - Structured: `[{"text":"a","checked":true}]` → written on first toggle
- ✅ `NoteDao.updateListItemsJson()` — local-only DAO method (does NOT mark pending_sync)
- ✅ `InboxRepository.updateListItemsJson()` — local persistence path
- ✅ `NoteDetailViewModel.onToggleListItem(index)` — optimistic UI + Room persistence
- ✅ `ChecklistItemRow` composable — real Checkbox, strikethrough on checked, dimmed text
- ✅ Progress counter ("N/M done") in list header
- ✅ "Checklist state saved locally" cue below items
- ✅ Sync preservation verified: `InboxSyncEngine` already carries forward local `listItemsJson`
- ✅ 18 new unit tests: legacy/structured parse, serialize, round-trip, toggle, edge cases
- ✅ Quality gates: all unit tests passing, clean compile
- Files modified: `ChecklistItem.kt` (new), `NoteDao.kt`, `InboxRepository.kt`, `NoteDetailViewModel.kt`, `NoteDetailScreen.kt`, `ChecklistItemsTest.kt` (new), `documentation/04-data-model.md`, `documentation/06-ui-architecture.md`, `documentation/08-business-rules.md`, `PROJECT.md`

### Session 2026-03-25 — I3: Type-Specific Detail Views
- ✅ Detail screen refactored: single monolithic body → 4 type-dispatched composables
  - **Task:** body → tags → schedule card (date/time/calendar) → status card (priority, kind, source, GCal, sync)
  - **Note:** body (prominent, 10-line edit min) → tags → compact "Details" card
  - **List:** list info card (name, persistent) → read-only checklist with item dividers → body → tags → compact details
  - **Idea:** "Brainstorm / Idea" italic label → body (prominent, 8-line edit min) → tags → compact details
- ✅ Extracted shared composables: `EditableTitle`, `EditableBody`, `EditableTags`, `ActionButtons`, `SourceRow`, `SyncStatusLine`
- ✅ Title bar uses `BrowseType.singularLabel` (replaces old `captureTypeLabel` helper)
- ✅ Removed old `CaptureInfoSection` and `captureTypeLabel` — fully replaced by type dispatch
- ✅ List detail: read-only checkbox placeholders with item dividers, item count header
- ✅ Dialog text updated to generic "item" instead of "note"
- ✅ Quality gates: all unit tests passing, clean compile
- Files modified: `NoteDetailScreen.kt`, `documentation/06-ui-architecture.md`, `documentation/08-business-rules.md`, `PROJECT.md`
- Codex source verification confirmed the type-dispatched layouts/doc updates on 2026-03-25; next recommended full-client slice is I4 local list checkbox interaction

### Session 2026-03-24 — I2: Type-Aware Inbox Navigation
- ✅ New `BrowseType` enum + `NoteEntity.browseType` extension property with documented 5-step heuristic
- ✅ Inbox tabs replaced: `All / Review / Pending` → `All / Tasks / Notes / Lists / Ideas / Pending`
- ✅ `ScrollableTabRow` replaces fixed `TabRow` (6 tabs don't fit a fixed row)
- ✅ Per-tab badge counts derived from single `getInboxNotes()` Flow grouped by BrowseType
- ✅ Search applies within the selected tab's type filter
- ✅ Type-specific empty states per tab
- ✅ Removed dead code: `NoteDao.getReviewQueue()`, `InboxRepository.getReviewQueue()`
- ✅ 16 new BrowseType classifier unit tests (explicit types, inference, priority, labels)
- ✅ Updated InboxUiStateTest for new 6-tab model (tab existence, browseType mapping, countForTab)
- ✅ Quality gates: all unit tests passing, no new lint issues
- Files modified: `BrowseType.kt` (new), `InboxUiState.kt`, `InboxViewModel.kt`, `InboxScreen.kt`, `NoteDao.kt`, `InboxRepository.kt`, `InboxUiStateTest.kt`, `BrowseTypeTest.kt` (new), `documentation/06-ui-architecture.md`, `documentation/08-business-rules.md`, `PROJECT.md`

### Session 2026-03-24 — I1: Capture-Type Persistence + Browse Foundation
- ✅ Room schema v4→v5: added `capture_type`, `list_name`, `list_items_json`, `persistent` columns to `NoteEntity`
- ✅ `CaptureRepository` now persists all capture-type metadata for both online and offline captures
- ✅ `InboxSyncEngine` preserves local capture-type metadata during sync (server detail API does not return these fields)
- ✅ Inbox cards are type-aware: type badge (Task/Note/List/Idea), list item preview (first 3 items), persistent badge
- ✅ NoteDetail shows "Capture Info" read-only section with type, list name, numbered items, persistent flag
- ✅ Detail screen title bar is type-aware ("Task Detail", "List Detail", etc.)
- ✅ Quality gates: all unit tests passing, no new lint issues
- ⚠️ Server blocker: `NoteDetailResponse`/`NoteFrontmatter` does not expose `captureType`, `listName`, `items`, or `persistent` — synced notes infer type from `kind` field only. Full round-trip requires server-side changes.
- ⚠️ Edit constraint: `NoteUpdateRequest` only supports `status`/`tags`/`body`/`title` — capture-type metadata is read-only in detail view
- Files modified: `NoteEntity.kt`, `AppDatabase.kt`, `DatabaseModule.kt`, `CaptureRepository.kt`, `InboxSyncEngine.kt`, `InboxScreen.kt`, `NoteDetailScreen.kt`, `documentation/04-data-model.md`, `PROJECT.md`

### Session 2026-03-15 — Comprehensive Ecosystem Audit Pass 2 (Verified Findings)
- Deliverable written: `C:\Users\tyler\Documents\Claude Projects\Inkwell\COMPREHENSIVE-AUDIT-FINDINGS.md`
- `CODEX-AUDIT-BRIEF.md` fully normalized as the Codex <-> Claude <-> GPT-5.4 Pro handoff file; open findings, non-findings, follow-up questions, and remediation tracks now align with Pass 2
- Companion summary written: `C:\Users\tyler\Documents\Claude Projects\Inkwell\CODEX-AUDIT-BRIEF-UPDATE-SUMMARY.md`
- Final finding count: 12 total
  - 2 Critical, 7 High, 2 Medium, 1 Low
- Highest-priority verified issues:
  - Android still embeds and silently falls back to a shared bearer token (`DEFAULT_AUTH_TOKEN`)
  - Chrome extension ships/stores the bearer token in `chrome.storage.sync` and requests broad host permissions
  - Multipart capture remains lossy across Android/web/extension/server for attachments, list captures, and schedule metadata
  - Server multipart attachment flow can save files under a UID that differs from the final note UID
  - Schedule metadata still does not round-trip consistently through capture -> markdown -> registry -> worker/API
- Important Pass 2 outcomes:
  - Processor cold-start note indexing issue confirmed; watcher coverage only partially mitigates it after startup
  - SPA inbox summary contract mismatch confirmed for tasks/notes views
  - Web offline queue still drops attachments
  - Android startup still does not retry normal FCM device registration
  - `sendWithoutRequest { true }` was not retained as a standalone finding; stale Google-auth code/docs were downgraded to a low-severity maintainability/productization issue

### Session 2026-03-15 — Comprehensive Ecosystem Audit Pass 1 (Inkwell + Nexus)
- Deliverable written: `C:\Users\tyler\Documents\Claude Projects\Inkwell\COMPREHENSIVE-AUDIT-PASS1.md`
- Outcome: 11 confirmed findings, 5 pass-2 verification items
- Most important confirmed issues:
  - Android APK still embeds and silently falls back to `DEFAULT_AUTH_TOKEN`
  - Chrome extension ships/stores the bearer token and has wildcard host permissions
  - Multipart capture contract is lossy across Android/web/extension/server
  - Server attachment files can be stored under a UID that does not match the returned note UID
  - Scheduled-task metadata is not persisted end-to-end for `/api/capture`
- Additional confirmed issues:
  - Processor cold-start scan ignores `Inbox/Notes`
  - Web task/note views expect `/api/inbox` fields the API does not return
  - Lists SPA mutates files just to read list contents
  - Web offline queue drops attachments
  - Android device registration is not retried on normal app startup
  - nginx body-size limit blocks intended attachment sizes

### Session 2026-03-15 — Deep Composite Audit (Security + Concurrency + Performance) — ALL 10 ITEMS RESOLVED
Perspectives: Android Security Engineer, Concurrency & Sync Architect, Android Performance Engineer
- ✅ **C-1 (HIGH):** Extracted `InboxSyncEngine` — shared fetch→stale detection→concurrent detail→bulk upsert→tombstone sweep. SyncWorker + InboxRepository both delegate to it. Eliminates code drift.
- ✅ **C-2 (MED):** Upserts now use `noteDao.upsertAll()` (single transaction) instead of serial `forEach { upsert() }`
- ✅ **C-3 (MED):** InboxRepository.syncInbox() now runs tombstone sweep (via shared engine)
- ✅ **C-4 (MED):** `triggerImmediateSync/Upload` changed from `REPLACE` to `KEEP` — no longer cancels in-flight syncs
- ✅ **P-1 (MED):** SyncWorker.updateWidgets() uses `getInboxNotesCount()` instead of loading full entity list
- ✅ **S-1 (MED):** `authToken` converted from one-shot `flow{}` to reactive `MutableStateFlow` — all collectors see updates immediately
- ✅ **P-4 (LOW):** Auth token migration runs once at init, not on every collection
- ✅ **S-3 (LOW):** Multipart filename sanitized (strips quotes, newlines, backslashes)
- ✅ **P-3 (LOW):** CaptureViewModel coach mark flows consolidated into single `combine()` collector
- ✅ **C-4b:** Immediate upload also changed to KEEP for consistency
- ✅ Quality gates: 294/294 tests passing, lint clean
- Files modified: SyncWorker, InboxRepository, SyncScheduler, CaptureApiService, PreferencesManager, CaptureViewModel, InboxRepositoryTest, SyncWorkerIntegrationTest + new InboxSyncEngine

### Session 2026-03-14 — LLM Audit Pipeline (3-provider, all 12 findings resolved)
- ✅ 3-provider audit: Gemini 3.1 (8.0/10), Codex GPT-5.4 #1 (7.0/10), Codex GPT-5.4 #2 (6.0/10) → **weighted avg 7.0/10** (up from 5.4 on 2026-02-28)
- ✅ **AP-4 (HIGH):** CaptureRepository.capture() now routes to multipart when attachmentUris present — online captures with attachments no longer silently drop files
- ✅ **AP-5 (HIGH):** InboxRepository.syncInbox() uses Instant.parse() for timestamp comparison (was string comparison)
- ✅ **AP-1 (MED, 2x):** SyncWorker explicit CancellationException catch — structured cancellation respected
- ✅ **AP-2 (MED, 2x):** UploadWorker 401 now clears token + posts auth-expired notification (mirrors SyncWorker)
- ✅ **AP-6 (MED):** InboxRepository refactored: bulk getAllByUids() + concurrent async/awaitAll detail fetches (no more N+1)
- ✅ **AP-7 (MED):** Both widgets wired with Glance clickable actions (capture → obsidiancapture://capture, inbox → obsidiancapture://inbox)
- ✅ **AP-3 (LOW, 2x):** NotificationActionReceiver calls finish() on all paths
- ✅ **AP-8 (LOW):** CaptureRepository logs gated on BuildConfig.DEBUG
- ✅ **AP-9 (LOW):** CaptureScreen uses default Scaffold insets (removed WindowInsets(0))
- ✅ **AP-10 (LOW):** PreferencesManager.setServerUrl() returns Boolean instead of throwing
- ✅ **AP-11 (LOW):** Testing strategy doc updated (36 unit / 4 instrumented / 294+ tests)
- ✅ **AP-12 (LOW):** Tests updated to verify fixed behavior (NotificationActionReceiverTest, SyncWorkerIntegrationTest, InboxRepositoryTest)
- ✅ Quality gates: 294/294 tests passing, lint clean
- Files modified: SyncWorker, UploadWorker, NotificationActionReceiver, CaptureRepository, InboxRepository, QuickCaptureWidget, InboxCountWidget, CaptureScreen, PreferencesManager, SettingsViewModel, 09-testing-strategy.md, + 3 test files

### Session 2026-03-10 — v2.3.0 release prep + server smoke test
- ✅ Bumped versionCode 10→11, versionName 2.2.0→2.3.0 (commit 37a994d)
- ✅ Built release APK: `app/build/outputs/apk/release/app-release.apk` (4.8MB)
- ✅ Server multipart smoke test confirmed:
  - `POST /api/capture` multipart → `201 Created`
  - File saved at `/vault/Attachments/{uid}/test.png`
  - Markdown has `attachments: [test.png]` frontmatter + `![[test.png]]` wikilink
- ⚠️ Phone disconnected — APK not yet installed. Use: `adb install -r app/build/outputs/apk/release/app-release.apk`

### Session 2026-03-10 — Attachment upload wired end-to-end
- ✅ `NoteEntity.attachmentsFromJson()` helper added
- ✅ `CaptureApiService.captureWithAttachments()` — sends multipart/form-data to existing server endpoint
  - Reads URI bytes via ContentResolver, resolves display filenames via OpenableColumns
  - Tags sent as comma-separated string (server multipart format)
  - 120s timeout for file uploads
- ✅ `UploadWorker.uploadNewCapture()` — routes to multipart path when attachmentUris non-empty
- ✅ No server changes needed (capture-server.ts already handles multipart on POST /api/capture)
- ✅ All 294 unit tests passing, lint clean (commit c2634a2)

### Session 2026-03-10 — Instrumented test fixes (Android 16)
- ✅ Added GrantPermissionRule to all 3 test classes (camera/media/notifications)
- ✅ Added androidx.test:rules dep
- ✅ Replaced Espresso.closeSoftKeyboard() with composeRule.waitForIdle() in Inbox/SettingsScreenTest
- ✅ 17/17 instrumented tests passing (commit d1b51a0)

### Session 2026-03-10 — IDEA type, attachment picker, coach marks (autonomous)
- ✅ Fixed `AttachmentPicker` camera URI state bug: `var cameraImageUri` → `remember { mutableStateOf<Uri?>(null) }` — survives recompositions properly
- ✅ Fixed lint error: added `<uses-feature android:name="android.hardware.camera" android:required="false" />` to manifest
- ✅ Fixed `CoachMarkManagerTest` compile error: added `testImplementation(libs.androidx.test.core)` + `androidx-test-core` to libs.versions.toml
- ✅ Quality gates: 294 unit tests passing, lint clean
- ✅ Committed: 31 files, 965 insertions (commit 75baec0)
- ⚠️ Attachment upload is local-only (phase 1): URIs stored in NoteEntity.attachmentUris as JSON, NOT sent to server. Server-side multipart endpoint needed for phase 2.
- ✅ Release APK built: `app/build/outputs/apk/release/app-release.apk` (ready to install)
- ⚠️ Install blocked this session: Windows Defender locks build intermediates during Gradle builds. Workaround: delete `app/build/intermediates` and `app/build/outputs/apk/release` mid-build, then rerun `./gradlew packageRelease`. Phone also dropped USB mid-session.

### Session 2026-03-10 — Auth fix + UI polish
- ✅ Google Sign-In removed — token baked into `BuildConfig.DEFAULT_AUTH_TOKEN` from `local.properties`
- ✅ `GoogleSignInManager.kt` deleted; `SettingsViewModel`, `ConnectionCard`, `SettingsUiState` cleaned up
- ✅ Server `/api/auth/google` response fixed: `{success:true}` → `{token:...}` (both server + Android)
- ✅ SSH hook: allows `138.197.81.173`, blocks all other SSH — documented in global CLAUDE.md + MEMORY.md
- ✅ UI: `FontFamily.Serif` on all headline/title styles — editorial journal feel across all screens
- ✅ UI: Inbox empty state — Obsidian diamond focal point with amber glow ring, serif headline
- ✅ UI: Bottom nav selected item renders in amber with warm indicator pill

### Session 2026-03-06 — Capture Type Toggle (Ralph Loop)
- All 5 stories passed (US-001 through US-005)
- Added 3-segment toggle (Task/Note/List) to CaptureScreen
- CaptureRequest DTO: added captureType, listName, items, persistent fields
- SmartToolbar hides irrelevant panels per type (NOTE hides priority/date/calendar, LIST hides all except tags)
- LIST mode: dedicated List Name + Items fields with persistent toggle
- NOTE mode: simplified capture, kind locked to "note"
- Reset and share intent handling preserved
- 6 files modified, compileDebugKotlin passes

### Session 2026-03-06 — Codex 5.3 Audit + Auth Fix
- ✅ Codex 5.3 audit via Monica.im: **8.61/10** (Arch 8.7, Quality 8.4, Testing 9.1, Security 8.9, Perf 8.2, Docs 7.3)
- ✅ **Fixed auth header bug (HIGH):** Added `sendWithoutRequest { true }` to Ktor bearer auth in NetworkModule.kt — token now sent proactively instead of requiring 401 challenge first
- ✅ **Hardened error handling:** Added `SerializationException` catch in SyncWorker before generic Exception — non-InboxResponse JSON no longer crashes sync
- ✅ **Documented sync policy:** Updated documentation/03-sync-strategy.md with formal conflict policy (LWW + pending protection), auth/error handling section, tombstone sweep docs

### Session 2026-03-05 — Test Coverage Sprint (Ralph Loop)
- ✅ All 11 stories in `prd.json` complete (US-000 through US-010)
- ✅ Added test deps: MockK 1.13.13, Robolectric 4.14.1, Turbine 1.2.0, work-testing (version catalog)
- ✅ 4 new test files: SyncWorkerIntegrationTest, CaptureMessagingServiceTest, CaptureMessagingTokenTest, InboxRepositoryTest
- ✅ Extended 4 existing test files: SyncConflictTest (+7), NotificationActionReceiverTest (+6), DeepLinkTest (+12), WidgetStateTest (+5)
- ✅ 34 test files total (was 30), all passing. Quality gates (test + lint) green.
- Note: JAVA_HOME must point to Android Studio JBR (`/c/Program Files/Android/Android Studio/jbr`), not the JRE at default JAVA_HOME
- Note: Hilt `@AndroidEntryPoint` components (CaptureMessagingService, NotificationActionReceiver) can't be instantiated in Robolectric without full Hilt test setup — tests replicate routing logic instead

### Session 2026-03-04 — UI Testing + Server Crash Fix
- ✅ All 4 screens screenshot-verified: Capture, Settings, Inbox (empty state), System Health
- ✅ Version 2.2.0 (versionCode 10) confirmed on device
- ✅ Settings: Connected (green) with token entered manually; Push Notifications ON; Haptic ON; Biometric OFF
- ✅ Fixed server crash: `email-commander.ts` `ImapFlow` socket timeout was escaping `poll()` try/catch as an uncaught exception → `process.exit(1)`. Fixed by adding `client.on('error', ...)` listener in `createImapClient()`. Source patched + running container patched + container stable.
- ⚠️ SyncWorker failing: `Illegal input: Fields [items, totalCount, syncToken] required but missing at path: $` — server returns error JSON (not InboxResponse). Root cause: auth token may not be in Authorization header. NOT yet fixed.

### Session 2026-03-03 — Install v2.2.0 on Phone
- ✅ Fixed `build.gradle.kts` signing path: `localProps.getProperty()` fallback + `../keys/release.keystore` (was `keys/`)
- ✅ Built signed `app-release.apk` (v2.2.0, SHA-256 verified)
- ✅ Installed on phone `58100DLCQ00724`
- ✅ Fixed `docker-compose.yml`: removed `- ANDROID_FINGERPRINT` passthrough from `environment` block (was overriding `env_file` with null)
- ✅ Fixed `server.ts`: added `/.well-known/assetlinks.json` route BEFORE the `/api/` gate (was falling through to 404)
- ✅ Server image rebuilt (Docker build cache hit after power cycle — ~2 min). Both endpoints verified live.

### Session 2026-03-02 — Post-Audit Feature Plan (v2.2.0)
**Phase 1 — Tombstone Sync** ✅
- `Obsidian-Dashboard-Desktop/src/registry.ts` — added `queryDeletedItems()` method
- `Obsidian-Dashboard-Desktop/src/api-server.ts` — added `GET /api/inbox/deleted` route + handler; added `GET /.well-known/assetlinks.json` route + handler
- `app/src/main/kotlin/.../data/local/dao/NoteDao.kt` — added `deleteByUidsIfSynced()`
- `app/src/main/kotlin/.../data/remote/CaptureApiService.kt` — added `getDeletedInbox()`
- `app/src/main/kotlin/.../sync/SyncWorker.kt` — captures `priorSyncedAt` before sync, runs tombstone sweep after upserts

**Phase 2 — App Links** ✅
- `app/src/main/AndroidManifest.xml` — added HTTPS `autoVerify="true"` intent-filter for `tyler-capture.duckdns.org/app/`
- `app/src/main/kotlin/.../ui/navigation/DeepLink.kt` — added HTTPS URI constants + HTTPS branch in `parseToRoute()`
- `app/src/main/kotlin/.../ui/navigation/CaptureNavHost.kt` — added HTTPS `navDeepLink` alongside custom-scheme links on all 4 destinations
- `Obsidian-Dashboard-Desktop/infra/docker-compose.yml` — added `ANDROID_FINGERPRINT` passthrough env var with setup comment

**Phase 3 — Release Prep** ✅
- `app/build.gradle.kts` — versionCode 9→10, versionName 2.1.2→2.2.0

### Audit 2026-03-02 — Deep Composite Audit (Security + Coroutines + Sync) — ALL 15 ITEMS RESOLVED
Perspectives: Android Security Engineer, Coroutine & Concurrency Specialist, Data Integrity / Sync Architect
- **C-1 ✅** `pending_` orphan bug — `NoteDao.replacePendingWithServer()` `@Transaction` method added; `UploadWorker.uploadNewCapture()` uses it instead of bare upsert. Prevents infinite duplicate uploads.
- **H-1 ✅** JWT logged to logcat — `CaptureApiService.exchangeGoogleToken()` Log.d now gated on `BuildConfig.DEBUG`; logs only status code, not body.
- **H-2 ✅** BIOMETRIC_WEAK accepted — `BiometricAuthManager` now uses `BIOMETRIC_STRONG` only in both `canAuthenticate()` and `setAllowedAuthenticators()`; unused `BIOMETRIC_WEAK` import removed.
- **H-3 ✅** Notification ID collision (1001 in both SyncWorker + CaptureMessagingService) — `CaptureMessagingService` IDs moved to 2001/2002 range.
- **H-5 ✅** `UploadWorker` returned `Result.retry()` on blank server URL — changed to `Result.success()` (matches SyncWorker).
- **H-6 ✅** `CaptureMessagingService.serviceScope` never cancelled — `onDestroy()` override added.
- **M-1 ✅** ISO 8601 string comparison — `SyncWorker` now uses `isServerNewer()` private helper with `Instant.parse()`.
- **M-3 ✅** `getInbox()` called without explicit limit — `SyncWorker` now passes `limit = INBOX_FETCH_LIMIT` (200).
- **M-4 ✅** O(n) serial DB reads in stale detection — replaced with `noteDao.getAllByUids()` single bulk query + in-memory filter.
- **M-6 ✅** `setAuthToken("")` stored empty string — now calls `remove()` on the EncryptedSharedPreferences key.
- **L-2 ✅** `Json` instance created per `exchangeGoogleToken` call — `CaptureApiService` now injects the singleton `Json` from `NetworkModule`.
- **NoteDao** — added `deleteByUid()`, `getAllByUids()`, and `replacePendingWithServer()` `@Transaction` method.
- **SyncConflictTest** — updated to test `Instant.parse()` semantics; added mixed-precision and invalid-timestamp test cases.
- Quality gates: blocked by Android Studio file locks on build dir — code verified by analysis. Run `./gradlew :app:testDebugUnitTest` after closing Android Studio.

### Audit 2026-02-28 — LLM Pipeline (Codex + Gemini + Monica) — ALL 12 ITEMS RESOLVED
Weighted scores: Architecture 7, Code Quality 6, Testing 4, Security 5, Performance 6, Documentation 4 → **5.4/10**
- 3-provider pipeline: OpenAI Codex (full repo, 1.5×), Gemini 3 Pro, GPT-5.2 via Monica
- Reports: `llm-audit-report-2026-02-28.md`, `llm-audit-action-plan.md`, `ARCHITECTURE.md`, `llm-audit-scores.json`
- **Fixed (session 1 — commits 4ddf96b):** #1 CancellationException, #2 sendWithoutRequest, #3 BuildConfig token
- **Fixed (session 2 — commit e457cbd):**
  - #7 ✅ Parallel N+1 in SyncWorker — `coroutineScope { async/awaitAll }` for concurrent detail fetches
  - #8 ✅ `collectAsStateWithLifecycle()` in all 5 screens (+ `lifecycle-runtime-compose` dep)
  - #10 ✅ `MainViewModel` extracted from MainActivity — lock state + sync trigger
  - #3 ✅ `SyncWorkerResultTest` + `UploadWorkerErrorTest` (15 new logic tests)
  - #12 ✅ `ARCHITECTURE.md` written
  - #6 ✅ Already satisfied (SyncScheduler had EXPONENTIAL backoff)
  - #9 ✅ Already satisfied (CaptureToolbar had private panel composables)
- Quality gates (session 1): `./gradlew test` ✅, `./gradlew lint` ✅
- Quality gates (session 2): blocked by Android Studio file locks on build dir — code verified by analysis. Run `./gradlew test` after closing Android Studio.

### Session 2026-02-27 — Audit Remediation
- ✅ Build verified (debug APK builds successfully with JDK 21 from Android Studio)
- ✅ `CaptureScreen.kt` split: toolbar extracted to `CaptureToolbar.kt` (973 → 457+548 lines)
  - `SmartToolbar` + all panel composables → `CaptureToolbar.kt`
  - `KIND_OPTIONS`/`CALENDAR_OPTIONS`/`PRIORITY_OPTIONS` made `internal` (shared across files)
- ✅ Instrumented tests added: `androidTest/` directory created with Hilt setup
  - `HiltTestRunner.kt` — custom runner with `HiltTestApplication`
  - `CaptureScreenTest.kt` — 6 Compose UI tests for Capture screen
  - `InboxScreenTest.kt` — 5 Compose UI tests for Inbox screen
  - Added `hilt-android-testing`, `androidx.test:runner` deps + `kspAndroidTest`
  - Fixed: Hilt testing package is `dagger.hilt.*` not `com.google.dagger.hilt.*`
- Build system note: requires JDK 11+; use Android Studio JBR at `/c/Program Files/Android/Android Studio/jbr`

### Session 2026-02-27 (continued, round 3) — Instrumented Tests Passing
- ✅ 17/17 instrumented tests passing on Pixel 10 Pro XL (Android 16 / API 36)
- ✅ Android 16 compatibility: `espresso-core:3.7.0` + `runner:1.7.0` (fixes `InputManager.getInstance` removal)
- ✅ WorkManager in tests: `callApplicationOnCreate` override in `HiltTestRunner` initializes WM before Hilt injection
- ✅ `FocusRequester` race: guarded `requestFocus()` with try/catch in `CaptureScreen`
- ✅ Nav semantics fix: M3 `NavigationBarItem` icon contentDescription not in merged tree → use `onNodeWithText + useUnmergedTree=true`
- ✅ Title ambiguity: nav label + screen title both match → use `onAllNodesWithText(...)[0]`
- ✅ Scroll fix: `performScrollTo()` for below-fold elements (Haptic Feedback toggle)
- Committed: `728b240`

### Session 2026-02-27 (continued, round 2)
- ✅ `SettingsScreenTest.kt` added — 6 Compose UI tests for Settings screen (title, system health card, server connection section, sync now button, push notifications toggle, haptic feedback toggle)
- All 4 instrumented test classes compile cleanly; 27 unit tests still pass

### Session 2026-02-27 (continued)
- ✅ `SettingsScreen.kt` split (685 → 289+298+148 lines):
  - `SettingsConnectionCard.kt` — ConnectionCard + ConnectionStatusIndicator + GoogleSignInButton
  - `SettingsComponents.kt` — SectionHeader + SettingsToggle + SyncIntervalDropdown
- ✅ Deprecation fixed: `ClickableText` → `Text` with `LinkAnnotation.Url` in MarkdownText/MarkdownParser
  - Updated `MarkdownParserTest` to use `getLinkAnnotations()` API
- All 27 unit tests passing, build clean, zero compiler warnings

### Audit 2026-02-26 — Codex (GPT-4o)
Architecture 8, Code Quality 7, Testing 5, Security 7, Performance 6, Documentation 5 → **6.3/10**
- Zero instrumented tests (Espresso/Compose UI tests)
- CaptureScreen.kt + MainActivity.kt too large (1000+ lines) — split into smaller composables
- `allowBackup` was true (FIXED: now false)

## Project Info
- **App Name:** Inkwell
- **Package:** `io.inkwell`
- **Min SDK:** 26, Target SDK: 34
- **Server:** Obsidian Dashboard Desktop at `138.197.81.173`
- **Repo:** https://github.com/tylerlockridge/inkwell
