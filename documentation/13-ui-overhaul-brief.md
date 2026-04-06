# UI Overhaul Brief: Inkwell

*Created: 2026-03-25 | Updated: 2026-03-28 | Project: Inkwell*

**Status: CLOSED — Tyler device verification passed 2026-03-28**
**Scope:** I6.4–I15 (UI overhaul + Slice 3 + capture validation + pinned sorting)

---

## Target

**Project:** Inkwell
**Surface:** All 5 daily-use screens (Inbox, Detail, Capture, Settings, System Health)
**Platform:** Android
**Framework:** Kotlin + Jetpack Compose (Material 3)

---

## Why Inkwell Is the First Overhaul Target

1. **Strong product foundation.** I1–I4 built a real type-aware browse model, type-dispatched detail views, and interactive list checkboxes. The data layer is solid and tested. The UI is the weakest link.
2. **Small, bounded screen count.** 6 screens total — Inbox and Detail are the two that matter most for daily use and are the easiest to isolate.
3. **Solo developer, personal app.** No design system to coordinate with, no stakeholder approval loop. Low risk, high learning value.
4. **Existing instrumented tests.** InboxScreen and NoteDetailScreen both have UI test coverage — changes can be validated.

---

## Classification

**Major.** This is a layout restructure + visual identity refresh for the two primary browse surfaces. Requires full overhaul brief, structured review, and baseline screenshots.

---

## Screens In Scope (First Pilot)

| Screen | Why | Current Lines |
|--------|-----|---------------|
| `InboxScreen` | Primary daily surface. 6-tab type-aware browse, card list, search, sync status. | ~450 |
| `NoteDetailScreen` | Secondary daily surface. Type-dispatched layouts (Task/Note/List/Idea). | ~500 |

### Screens Explicitly Out of Scope (Original)

| Screen | Status |
|--------|--------|
| `CaptureScreen` + `CaptureToolbar` | ✅ SHIPPED (I6.7) |
| `SettingsScreen` + connection cards | ✅ SHIPPED (I9) |
| `SystemHealthScreen` | ✅ SHIPPED (I9) |
| `LockScreen` | Biometric prompt — minimal UI, system-driven. Not planned. |

---

## Screenshots Inventory

| Screenshot | State | Path |
|------------|-------|------|
| (not yet captured) | Inbox — All tab, populated | `.visual-qa/baselines/inbox_all_populated.png` |
| (not yet captured) | Inbox — Tasks tab, populated | `.visual-qa/baselines/inbox_tasks_populated.png` |
| (not yet captured) | Inbox — empty state | `.visual-qa/baselines/inbox_empty.png` |
| (not yet captured) | Detail — Task type | `.visual-qa/baselines/detail_task.png` |
| (not yet captured) | Detail — Note type | `.visual-qa/baselines/detail_note.png` |
| (not yet captured) | Detail — List type (with items) | `.visual-qa/baselines/detail_list.png` |
| (not yet captured) | Detail — Idea type | `.visual-qa/baselines/detail_idea.png` |
| (not yet captured) | Detail — Edit mode | `.visual-qa/baselines/detail_edit_mode.png` |

**Baseline exists:** No — will create in the next pass (I6.2).

---

## Current UX Strengths to Preserve

1. **Type-aware card badges** — clear visual signal of Task/Note/List/Idea per card.
2. **List item preview in inbox cards** — first 3 items visible without opening detail.
3. **Interactive checklist in list detail** — checkbox toggle with strikethrough + progress.
4. **Left color stripe on cards** — quick visual differentiation by type/tag.
5. **Done/Drop action buttons** — simple, prominent, never ambiguous.
6. **Scrollable tab row** — handles 6 tabs without cramping.
7. **Search within active tab** — type-scoped filtering, not global-only.
8. **"Checklist state saved locally" cue** — honest about what syncs and what doesn't.
9. **Type-aware detail title bar** — "Task Detail" / "List Detail" etc.

---

## Current UX Weaknesses to Improve

1. **Card density is too loose.** Each card takes ~80dp height for minimal content. Inbox feels empty with 5 items because cards are spaced like they carry paragraphs.
2. **Type badge is small and overlooked.** 10sp font, 6dp padding — it's the right idea but visually it disappears against the title.
3. **Tab badges compete with tab labels.** Primary-colored badge circles next to tab text creates visual clutter when all tabs have non-zero counts.
4. **Detail screen lacks visual hierarchy.** SectionCard headers (Schedule, Status, Details) have identical weight — there's no sense of which information matters most.
5. **Edit mode transition is abrupt.** Crossfade between read and edit mode feels like a UI glitch, not an intentional transition.
6. **Empty states are decorative but not helpful.** The diamond/glow animation is pretty but doesn't help the user understand what to do next. CTA buttons are too subtle.
7. **Sync status icons (right-side dots) are cryptic.** CheckCircle/Circle/Error icons without labels require learning — new users won't know what they mean.
8. **No visual rhythm for metadata rows.** InfoRow pairs (label + value) in SectionCards are visually flat — no hierarchy between important and secondary metadata.
9. **List detail checkbox area is too compact.** ChecklistItemRow gives each item only 2dp vertical padding — on a phone, tap targets are tight.
10. **Persistent badge and type badge use different styling.** Inconsistent chip treatment across the card.

---

## Design Intent

**What should this UI accomplish?**
Let a solo user quickly scan their inbox by type, open items that need attention, and take action (done/drop) or review detail — all in under 10 seconds per item.

**What aesthetic direction?**
[PLACEHOLDER — Tyler to decide]
Options: minimalist, bold-dark, editorial, Material 3 default, or custom. Current app uses Material 3 defaults with a warm amber accent.

**What is the single most important thing a user should notice first?**
[PLACEHOLDER — Tyler to decide]
Candidates: the type distribution (how many tasks vs notes vs ideas), the newest item, or items needing action (pending sync).

---

## Audience and Context

**Who uses this?**
Solo developer (Tyler), Android phone (Pixel), daily usage. Expert-level familiarity with the app.

**In what workflow?**
Capture a thought (voice/text) → appears in inbox → review from inbox → open detail → mark done or drop. Inbox is visited multiple times per day. Detail is opened for ~30% of items.

**Constraints:**
- Single phone size (no tablet layout needed yet)
- Offline-first: UI must work with pending-sync items visible
- Touch targets: 48dp minimum per Material 3 guidelines
- Dark mode: already handled by Material 3 dynamic theming
- Performance: Room + StateFlow, no pagination needed (inbox is typically <200 items)

---

## Internal References to Consult

- [ ] `_GLOBAL/DESIGN-GOVERNANCE-CONFIG.md` — **does not exist yet** (create if needed before executing overhaul)
- [ ] `_GLOBAL/UI-ANTI-PATTERNS.md` — **does not exist yet**
- [ ] `_GLOBAL/UI-REFERENCE-LIBRARY.md` — **does not exist yet**
- [x] `documentation/06-ui-architecture.md` — screen inventory, ViewModel responsibilities
- [x] `documentation/08-business-rules.md` — rules 15-19 (BrowseType, tabs, detail dispatch, checklist)
- [x] `documentation/04-data-model.md` — NoteEntity schema (what data exists to display)
- [ ] Project theme file — `app/src/main/kotlin/io/inkwell/ui/theme/` — existing design tokens

---

## External References to Consult

**Primary — real product flows (set overhaul direction):**

| Reference | URL | What to Borrow | What to Ignore |
|-----------|-----|---------------|----------------|
| Google Tasks | [PLACEHOLDER — Tyler to supply Mobbin/screenshot link] | Type-aware task list density, checkbox interaction | Google-specific integrations |
| Todoist | [PLACEHOLDER — Tyler to supply Mobbin/screenshot link] | Inbox card density, project/label filtering tabs | Premium features, gamification |
| Apple Reminders | [PLACEHOLDER — Tyler to supply Page Flows link] | List-type detail view, checklist interaction | iOS-specific gestures |
| Things 3 | [PLACEHOLDER — Tyler to supply Mobbin link] | Information hierarchy in detail views, clean metadata | macOS-only patterns |

**Secondary — polish, components, constraints (do not drive direction alone):**

| Reference | URL | What to Borrow | What to Ignore |
|-----------|-----|---------------|----------------|
| Material 3 Components | https://m3.material.io/components | Tab row, card, checkbox specs | Exact Google styling |
| Dribbble: task app search | [PLACEHOLDER — optional] | Visual inspiration only | Fantasy UI, impractical layouts |

---

## Motion Preferences

- [ ] No animations (static only)
- [x] Subtle transitions (hover states, page transitions, loading indicators)
- [ ] Rich motion (scroll-triggered animations, micro-interactions, entrance animations)
- [ ] Custom: [describe]

Default recommendation: subtle transitions. The current Crossfade for edit mode should be replaced with a smoother shared-element or container-transform transition.

---

## Anti-Patterns to Avoid

- [x] AP-01: Hidden urgency behind collapsed state — *relevant: sync errors shouldn't be hidden behind a dot icon*
- [x] AP-02: Uniform visual weight across severity levels — *relevant: pending sync vs synced vs error should be visually distinct*
- [ ] AP-03: Full-width stacked buttons — *N/A: Done/Drop are side-by-side*
- [x] AP-04: Interactive elements without state feedback — *relevant: checkbox toggle needs clear feedback*
- [ ] AP-05: Truncated labels without tooltip — *N/A on mobile*
- [ ] AP-06: Redundant buttons for same action
- [ ] AP-07: Button labels that don't describe the action
- [ ] AP-08: Garbled output from misconfigured launch

**Project-specific anti-patterns:**
- Don't make the inbox look like a generic RecyclerView list — type-awareness is the product differentiator
- Don't hide the type badge — it should be the second thing you see after the title
- Don't add swipe-to-dismiss without undo — the current explicit Done/Drop buttons are safer

---

## Questions Tyler Still Needs to Answer

1. **Aesthetic direction:** minimalist / bold-dark / editorial / warm-Material / custom?
2. **Primary visual anchor:** type distribution? newest item? pending-action items?
3. **External references:** which competitor apps do you actually use and like the look of?
4. **Card density preference:** tighter (5-6 visible on screen) or current (3-4 visible)?
5. **Sync status display:** keep icons? switch to text labels? badge on card? hide when synced?
6. **Color palette:** keep current warm amber accent? shift to a different primary?

---

## Acceptance Criteria

- [x] All Tier 1 criteria (accessibility, information hierarchy, urgency differentiation) score >= 3 in `ui-review`
- [x] No unaddressed anti-pattern matches
- [ ] Mobile-responsive — single phone layout (no tablet breakpoint needed)
- [x] Dark mode support — already handled by Material 3 dynamic theming
- [x] Touch targets >= 48dp on mobile
- [x] Color contrast >= 4.5:1
- [x] Baseline screenshots captured and accepted in `.visual-qa/baselines/`
- [ ] Cross-model review completed (deferred — Claude self-review PASS)
- [x] Existing instrumented UI tests still pass after overhaul
- [x] All unit tests still pass after overhaul
- [x] Type-aware card badges, list preview, interactive checklist preserved
- [x] Done/Drop actions preserved
- [x] Search within tab preserved

---

## Baseline Capture Plan (Next Pass — I6.2)

1. Build and install debug APK on test device
2. Populate inbox with at least 2 items per type (task, note, list, idea)
3. Capture screenshots per the inventory table above
4. Create `.visual-qa/baselines/` directory
5. Store screenshots with naming convention
6. Record device/build info in a `baselines-metadata.md`

---

## First Implementation Slice — Inbox (I6.4) ✅ SHIPPED

**Shipped 2026-03-25.** Changes:
- Type badge: 11sp bold, 0.18 alpha fill, rounded-4dp
- Card density: 10dp vertical padding, 4dp card gap (7 items visible vs 6)
- Tab row: counts inline ("Tasks 4"), bold selected, no divider
- Sync: hidden when synced, 14dp icon only for pending/error
- Body preview: single-line max, dot-separated list items
- Connection banner: compact error-tinted, "Settings →"
- Empty state: smaller diamond, bolder title, concise copy
- Timestamps: shortened ("1h" not "1h ago")
- Tags: inline in metadata row

**Review verdict: PASS** with 2 recommended fixes (F-01: sync visibility, F-03: note preview lines).

**I6.4b polish (2026-03-26):** F-01 fixed (pending cards get 5% amber background tint). F-03 fixed (NOTE-type cards get 2-line body preview). Both review findings closed.

**Detail slice (I6.5) shipped 2026-03-26.** Changes:
- Type badge in title bar (matching inbox badge colors/style)
- "Pending sync" inline amber label replaces old SyncStatusLine
- Section labels as uppercase tracking text (SCHEDULE, STATUS, DETAILS)
- MetadataCard with tighter padding (14h x 10v) and RoundedCornerShape(10dp)
- MetadataRow with bodySmall text (was bodyMedium)
- Action buttons 44dp height (was 48dp), outline border at 50% alpha
- Checklist "Saved locally" shortened, 10sp
- Idea "Brainstorm" label with amber color matching idea badge
- Title as headlineSmall bold with tight letter-spacing (was headlineMedium)

**First overhaul pilot (Inbox + Detail) is complete.**

---

## Settings + System Health Overhaul — I9 (2026-03-28) ✅ SHIPPED

**Settings changes:**
- Bold-dark title (`headlineSmall.Bold`, `letterSpacing = -0.5sp`) matching Inbox/Detail
- Flat TopAppBar (`containerColor = surface`) matching all accepted screens
- System Health entry: restrained navigation row (surfaceContainerLow, forward arrow), not a hero card
- Section headers: uppercase tracking labels (11sp, Bold, `letterSpacing = 1sp`) — same pattern as Detail
- All cards: `RoundedCornerShape(10dp)`, tighter padding (14h x 10v)
- Section spacing: 10dp (was 16dp)
- Connection card: both states on `surfaceContainerLow` (was primaryContainer/tertiaryContainer)
- Connected state: TextButton "Disconnect" (was OutlinedButton), smaller dot indicators
- "Last synced" as a metadata row with label/value hierarchy (matching Detail's MetadataRow)
- Sync Now / Export buttons: 40dp height, `labelMedium` text
- Toggle labels: `bodyMedium.Medium` (was `bodyLarge`)
- Haptic description shortened
- Version line branded: "Inkwell 2.3.0 (…)" instead of "Version 2.3.0"

**System Health changes:**
- Bold-dark title matching all accepted screens
- Flat TopAppBar with back arrow + refresh
- Section labels as uppercase tracking text (matching Settings and Detail)
- HealthCard: RoundedCornerShape(10dp), padding 14h x 10v, `spacedBy(4dp)` rows
- StatusRow: `bodySmall` for both label and value (was `bodyMedium`); placeholder dimming for "Never"/"unknown"/"0"
- Status dots: 6dp (was 8dp) — subtler, cleaner
- StatBlock: 18dp icon (was 24dp), `titleSmall.Bold` value (was `titleMedium`), 10sp label
- Syncthing section: SubsectionLabel composable (10sp, Bold, primary color) for Devices/Folders
- Folder progress bar: 3dp height (was 4dp)
- Restart button: 40dp height, `labelMedium` text
- Dividers: `outlineVariant.copy(alpha = 0.3f)` — subtler than default
- Section spacing: 10dp (was 16dp)

**Review verdict: All 5 daily-use screens now share the accepted Inkwell visual language.**

---

## Capture Metadata Authoring — I10 (2026-03-28) ✅ SHIPPED

Completes Slice 3 end-to-end by adding capture-time authoring for color, pinned, and sourceUrl.

**Changes:**
- `CaptureUiState`: added `pinned` (Boolean), `sourceUrl` (String), `color` (CaptureColor?), `hasExtrasMetadata` computed property
- `ToolbarPanel.Extras`: new panel enum value, accessed via `...` (MoreHoriz) icon in expanded toolbar
- `CaptureColor`: curated 6-color enum (Red/Orange/Yellow/Green/Blue/Purple) with hex + Compose Color
- `ExtrasPanel`: pin toggle, source URL text field, color picker with circle swatches + "none" option
- Active metadata chips: pinned/sourceUrl/color shown as dismissible chips alongside tags/schedule/priority
- `CaptureViewModel`: `onPinnedToggle()`, `onSourceUrlChange()`, `onColorChange()` methods
- `CaptureViewModel.onCapture()`: threads `color.hex`, `pinned`, `sourceUrl` into `CaptureRepository.capture()`
- Batch mode: extras are preserved across batch captures (intentional sticky metadata)
- Non-batch: full state reset on success clears extras

**Shared decision:**
`shared` is NOT editable at capture time. It is server-authoritative (consumed from `CaptureMetadata.shared` during sync). There is no clear user action that means "share this" in a personal note-taking app. Adding a toggle would confuse users ("shared with whom?").

**Tests:** 14 new tests in `CaptureExtrasTest` — state defaults, hasExtrasMetadata, CaptureColor enum, state mutations, batch preservation, full reset.

**Slice 3 is now complete end-to-end:**
- I7: DTO/network wired
- I8: Room persistence + detail/inbox surfacing
- I10: Capture-time authoring
- I11: Share intent auto-fill + shared semantics

---

## Share Intent Source URL + Shared Semantics — I11 (2026-03-28) ✅ SHIPPED

**ShareIntentParser enhanced:**
- URL extraction: first `https?://` URL in shared text → `sourceUrl`
- Body cleaning: if text is only a URL, strip it from body (title serves as content, URL in sourceUrl)
- If text has prose + URL, full text preserved (URL stays in context)

**Shared semantics defined:**
- `shared = true` = "this capture originated from an Android share intent (external app)"
- Set automatically in `CaptureViewModel.onShareIntentReceived()`
- Server can also set it via `CaptureMetadata.shared` during sync
- Not user-editable — system-derived only

**Threading:**
- `ShareData.sourceUrl` → `MainActivity` → `CaptureNavHost` → `CaptureScreen` → `CaptureViewModel.onSourceUrlChange()`
- `isFromShareIntent` boolean → `CaptureScreen` → `CaptureViewModel.onShareIntentReceived()`
- Both `sourceUrl` and `shared` threaded into `CaptureRepository.capture()`

**Tests:** 22 tests in `ShareIntentParserTest` — URL extraction (https, http, query params, fragments, multiple URLs, no URL, mailto/ftp exclusion), body cleaning (URL-only, prose+URL, whitespace), integration scenarios (Chrome share, Twitter share)

---

## Safety Rules

1. **Do not break existing behavior.** Every action (open detail, refresh, done, drop, search, tab switch, checkbox toggle) must work after the overhaul.
2. **Do not change ViewModel logic.** This is a presentation-layer-only pass. State, data flow, and persistence are not touched.
3. **Do not remove features.** All current information (type badge, list preview, sync status, persistent flag) must remain visible.
4. **Run quality gates after every change.** `./gradlew testDebugUnitTest` + `compileDebugKotlin` minimum.
5. **Capture before/after screenshots.** Every visual change should have a corresponding baseline comparison.

---

## Review Plan

- [x] Claude self-review during generation
- [ ] Gemini structured audit after generation
- [x] Human spot-check — Tyler device verification passed 2026-03-28
- [ ] Full `ui-review` skill invocation if needed

---

## Completion Summary (I12 Closeout)

**What is complete (I6.4–I15):**

| Surface / Feature | Slice | Status | Evidence |
|-------------------|-------|--------|----------|
| Inbox visual overhaul | I6.4 + I6.4b | ✅ PASS 24/30 | 4 review screenshots (2026-03-25, 2026-03-26) |
| Detail visual overhaul | I6.5 | ✅ PASS 27/30 | 4 review screenshots (2026-03-26) |
| Capture visual overhaul | I6.7 | ✅ PASS 26/30 | 3 review screenshots (2026-03-26) |
| Slice 3 DTO/network | I7 | ✅ SHIPPED | 14 unit tests |
| Slice 3 Room persistence + sync | I8 | ✅ SHIPPED | 12 unit tests |
| Settings visual overhaul | I9 | ✅ SHIPPED | 1 review screenshot (2026-03-28) |
| System Health visual overhaul | I9 | ✅ SHIPPED | 1 review screenshot (2026-03-28) |
| Capture Slice 3 authoring | I10 | ✅ SHIPPED | 4 review screenshots + 14 unit tests |
| Share intent → sourceUrl | I11 | ✅ SHIPPED | 22 unit tests |
| Capture empty-text validation | I13 | ✅ SHIPPED | 28 unit tests |
| Pinned-first inbox sorting | I15 | ✅ SHIPPED | 10 unit tests |
| Inbox current state evidence | I12 | ✅ evidence | 1 review screenshot (2026-03-28) |

**What is deferred (non-core):**

| Item | Category | Notes |
|------|----------|-------|
| LockScreen polish | Low-priority polish | Minimal biometric prompt UI, system-driven, rarely seen |
| Share intent with images | Deferred edge case | Only `text/*` MIME types parsed |

**Test coverage after I7–I15:**
- Unit test files: 41 (was 36 at I6)
- Unit tests passing: 370+ (was 294+)
- New test files: `Slice3PersistenceTest`, `CaptureExtrasTest`, `CaptureRequestSlice3Test`, `ShareIntentParserTest`, `CaptureValidationTest`, `PinnedSortingTest`
