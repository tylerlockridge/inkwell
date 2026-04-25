# D1 Navigation Shell Visual Proof

Date: 2026-04-22
Device: `Medium_Phone_API_36.1` / `emulator-5554`
Build: debug install from current workspace

This folder is the canonical D1 visual proof set. The earlier
`d1-navigation-shell-2026-04-22` folder was blocked by Android's handwriting
input panel and is not the acceptance proof.

## Captures

| File | Expected state | Result |
|------|----------------|--------|
| `01-capture.png` | Capture bottom-nav item selected | Pass |
| `02-inbox.png` | Inbox route selected, `All` tab active | Pass |
| `03-tasks.png` | Tasks route selected, `Tasks` tab active | Pass |
| `04-notes.png` | Notes route selected, `Notes` tab active | Pass |
| `05-lists.png` | Lists route selected, `Lists` tab active | Pass |

Each screenshot has a matching `*-ui.xml` dump. UI dumps confirmed:

- The screen title matched the expected route.
- The correct bottom-nav item was selected.
- `Capture`, `Inbox`, `Tasks`, `Notes`, and `Lists` were visible as bottom-nav labels.
