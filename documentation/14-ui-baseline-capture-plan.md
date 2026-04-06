# UI Baseline Capture Plan

*Created: 2026-03-25 | Project: Inkwell | Overhaul pilot: Inbox + Detail*

---

## Purpose

Capture the current-state screenshots before any UI overhaul work begins. These baselines serve as the "before" reference for review, regression comparison, and cross-model audit.

---

## Prerequisites

Before capturing screenshots:

1. **Build and install debug APK** on test device
   ```bash
   JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew installDebug
   ```
2. **Populate test data** — the inbox must contain at least:
   - 2 tasks (one with priority/date, one minimal)
   - 2 notes
   - 1 list (with 3+ items, at least 1 checked)
   - 1 idea
   - 1 pending-sync item (disconnect server, capture offline, reconnect)
3. **Device:** Pixel phone, dark mode OFF (capture light mode baselines first)
4. **App version:** current debug build from the `master` branch at time of capture

---

## Shot List

Naming convention: `inkwell_{screen}_{state}_{date}.png`

All files go in `.visual-qa/baselines/`.

| # | Filename | Screen | State | Setup Notes |
|---|----------|--------|-------|-------------|
| 1 | `inkwell_inbox_all_default_{date}.png` | InboxScreen | All tab, populated, not refreshing | At least 6 items across types visible |
| 2 | `inkwell_inbox_tasks_populated_{date}.png` | InboxScreen | Tasks tab, populated | Switch to Tasks tab; 2+ task items visible |
| 3 | `inkwell_inbox_lists_populated_{date}.png` | InboxScreen | Lists tab, populated | Switch to Lists tab; list preview items visible |
| 4 | `inkwell_inbox_empty_state_{date}.png` | InboxScreen | Ideas tab, empty | Switch to Ideas tab with 0 idea items |
| 5 | `inkwell_detail_task_{date}.png` | NoteDetailScreen | Task detail, read mode | Open a task with priority, date, tags |
| 6 | `inkwell_detail_note_{date}.png` | NoteDetailScreen | Note detail, read mode | Open a note with body text and tags |
| 7 | `inkwell_detail_list_{date}.png` | NoteDetailScreen | List detail, checklist visible | Open a list with 3+ items, 1+ checked |
| 8 | `inkwell_detail_idea_{date}.png` | NoteDetailScreen | Idea detail, read mode | Open an idea/brainstorm item |

Replace `{date}` with the actual capture date in `YYYY-MM-DD` format (e.g., `2026-03-26`).

---

## Capture Order

1. Launch app, navigate to Inbox
2. Capture shot 1 (All tab)
3. Switch to Tasks tab → capture shot 2
4. Switch to Lists tab → capture shot 3
5. Switch to Ideas tab (should be empty) → capture shot 4
6. Switch back to All tab, tap a task item → capture shot 5
7. Back, tap a note item → capture shot 6
8. Back, tap a list item → capture shot 7
9. Back, tap an idea item → capture shot 8

---

## Capture Method

**Option A — ADB screenshot (preferred for consistency):**
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png .visual-qa/baselines/inkwell_inbox_all_default_2026-03-26.png
adb shell rm /sdcard/screenshot.png
```

**Option B — Device screenshot:**
Power + Volume Down, then transfer from device Photos/Screenshots to `.visual-qa/baselines/`.

---

## Metadata to Record

After capture, create `.visual-qa/baselines/capture-metadata.md`:

```markdown
# Baseline Capture Metadata

- **Date:** {date}
- **Device:** {model} ({device codename})
- **Android:** {version} (API {level})
- **App version:** {versionName} ({versionCode})
- **Build type:** debug
- **Theme:** light / dark
- **Branch:** master
- **Commit:** {short hash}
- **Screenshots:** 8
```

---

## After Capture

1. Verify all 8 files exist in `.visual-qa/baselines/`
2. Verify filenames match the shot list exactly
3. Visually confirm each screenshot matches its described state
4. Commit baselines to the repo (they are reference artifacts, not secrets)
5. Update `PROJECT.md` to record that baselines are captured and I6.3 (inbox card redesign) is ready to start

---

## Related Docs

- `documentation/13-ui-overhaul-brief.md` — overhaul scope, strengths/weaknesses, design questions
- `documentation/06-ui-architecture.md` — screen inventory and architecture
