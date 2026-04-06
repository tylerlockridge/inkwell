# Inkwell Final Verification Flow

*Target: 5-7 minutes on device. Covers all shipped features through I15.*
*Updated: 2026-03-28 (I16 closure)*
*Result: **PASSED** — Tyler verified 2026-03-28*

---

## 1. Inbox + Pinned Sorting (45 seconds)
- [ ] Open app → navigate to **Inbox**
- [ ] Confirm: bold "Inbox" title, tab row with inline counts, type badges, left color stripes
- [ ] If you have a pinned item: confirm it appears **above** newer unpinned items
- [ ] Swipe one card right → Done dialog appears (cancel)
- [ ] Switch to **Tasks** tab → pinned tasks should still float to top within filtered view
- [ ] Switch to **Ideas** tab → filtered or empty state

## 2. Detail + Slice 3 Metadata (45 seconds)
- [ ] Tap any item → **Detail** screen
- [ ] Confirm: type badge in title bar, uppercase section labels (SCHEDULE/STATUS/DETAILS)
- [ ] If pinned → "Pinned" label + pin icon at top
- [ ] If shared → "Shared" label + share icon at top
- [ ] If sourceUrl → tappable link in Details card
- [ ] If color → color dot in Details card
- [ ] Edit (pencil) → change title → save (check) → "Saved" snackbar
- [ ] Back

## 3. Capture + Extras + Validation (90 seconds)
- [ ] Navigate to **Capture**
- [ ] Confirm: type buttons (Task/Note/List/Idea), watermark, writing surface
- [ ] **Validation**: with empty text, send button should be **disabled/dimmed**
- [ ] Type a title + body
- [ ] Tap **[+]** to expand → icons appear (tags, ..., schedule, type, calendar, priority)
- [ ] Tap **...** (extras) → confirm panel: pin toggle, URL field, 6-color picker
- [ ] Toggle **Pin ON** → "Pinned" chip appears
- [ ] Enter a URL → URL chip appears
- [ ] Tap a **color** → color label chip appears
- [ ] **Send** → "Captured and synced ✓" or "Saved locally"
- [ ] Open in **Inbox** → confirm item is at top (pinned-first sorting)
- [ ] Open in **Detail** → confirm pinned indicator, color dot, sourceUrl row

## 4. Share Intent (60 seconds)
- [ ] Open **Chrome** → navigate to any page
- [ ] Share → select **Inkwell**
- [ ] Confirm: title pre-filled, source URL auto-filled (expand extras or check chips)
- [ ] Confirm: body does NOT duplicate the URL when share was URL-only
- [ ] **Send** → open in Detail → "Shared" badge + sourceUrl row present

## 5. Settings + System Health (30 seconds)
- [ ] **Settings** tab: bold title, uppercase sections (CONNECTION, SYNC, NOTIFICATIONS, SECURITY)
- [ ] System Health row: restrained, with `>` arrow
- [ ] Connection card: same surface color in both states
- [ ] Tap **System Health** → bold title, uppercase sections, compact StatusRows

## 6. Quick Regressions (30 seconds)
- [ ] Pull-to-refresh in Inbox → syncs
- [ ] Search in Inbox → filters within tab
- [ ] Biometric toggle in Settings → toggles
- [ ] Sync Now → no crash

---

## Verdict
**PASSED — Inkwell core phase is CLOSED.** Tyler verified 2026-03-28.
