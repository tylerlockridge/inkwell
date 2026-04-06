# Baseline Capture Metadata

- **Date:** 2026-03-25 (initial) + 2026-03-25 (I6.3a refresh)
- **Device:** Pixel 10 Pro XL (58100DLCQ00724)
- **Android:** 16 (API 36)
- **App version:** 2.4.0 (versionCode 12)
- **Build type:** debug
- **Package:** io.inkwell
- **Theme:** dark (Material 3 dynamic)
- **Branch:** master
- **Commit:** 9168be2 (initial), post-I6.3a fix (refresh)
- **Screenshots:** 9

## Shot List

| # | File | State | Notes |
|---|------|-------|-------|
| 1 | `inkwell_inbox_all_default_2026-03-25.png` | All tab, 6 items | Pre-fix: 2 tasks, 2 notes, 1 list, 1 idea (misclassified as Task) |
| 2 | `inkwell_inbox_tasks_populated_2026-03-25.png` | Tasks tab, 4 items | Pre-fix: includes idea item due to captureType="task" |
| 3 | `inkwell_inbox_lists_populated_2026-03-25.png` | Lists tab, 1 item | List preview shows first 3 items + "+1 more" |
| 4 | `inkwell_inbox_empty_state_2026-03-25.png` | Ideas tab, empty | Pre-fix: diamond/glow empty state (no ideas classified yet) |
| 5 | `inkwell_detail_task_2026-03-25.png` | Task Detail | Schedule + Status cards, Done/Drop buttons, "Pending sync" |
| 6 | `inkwell_detail_note_2026-03-25.png` | Note Detail | Compact "Details" card, Kind="note" |
| 7 | `inkwell_detail_list_2026-03-25.png` | List Detail | List Info card, 4 checklist items (unchecked), "Checklist state saved locally" |
| 8 | `inkwell_detail_idea_2026-03-25.png` | **REFRESHED (I6.3a):** Idea Detail | Shows "Idea Detail" title, "Brainstorm / Idea" label, compact Details card |
| 9 | `inkwell_inbox_ideas_populated_2026-03-25.png` | **NEW (I6.3a):** Ideas tab, 1 item | Post-fix: idea correctly classified with "Idea" badge |

## I6.3a Refresh Notes

- **Bug fixed:** `CaptureViewModel.kt` line 219 changed `CaptureType.IDEA -> "task"` to `-> "idea"`
- Shot 8 replaced: now shows actual "Idea Detail" layout (was "Task Detail" pre-fix)
- Shot 9 added: Ideas tab with 1 correctly classified idea item
- Shot 4 retained: still shows the empty state UI pattern (valid baseline for empty-state overhaul)
- Shots 1-2 are pre-fix state: the old "Voice_capture_with_whisper" idea still has `captureType="task"` in Room (persisted before fix)
