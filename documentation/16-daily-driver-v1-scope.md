# Inkwell Daily Driver v1 Scope

*Created: 2026-04-22 | Project: Inkwell | Package: Inkwell Daily Driver v1*

---

## Purpose

This document names the first post-core Inkwell package and separates the main mobile app goals from the secondary Nexus project-page and remote-control goals.

The package is:

**Inkwell Daily Driver v1**

The product goal is simple: Inkwell should become Tyler's fast mobile app for notes, tasks, lists, and brainstorming before it becomes a mobile project command center.

---

## Roadmap Anchor

This scope starts from the localized Inkwell readiness packet:

- `documentation/18-inkwell-readiness-audit-and-handoff.md`

That packet says the next Inkwell work should start with the polished daily-driver flow and keep project pages as a secondary Nexus feature. The original Dashboard-side source is retained there as historical provenance.

---

## Main Scope

Daily Driver v1 covers the main mobile app:

- quick capture
- notes lane
- tasks lane
- lists lane
- ideas / brainstorming lane
- item detail and edit flow
- search and filters
- sync, account, and system state

This is the first post-core package. It builds on the closed Inkwell Core baseline instead of reopening core closure.

---

## Secondary Scope Parked For Later

These are still important Nexus goals, but they do not lead this package:

- mobile project pages
- dashboard-style project status
- feedback submission into `project-feedback-log.json`
- remote session control
- command prompt submission
- Codex / Claude Code proceed buttons
- terminal output streaming
- guardrail and action-log UX

These become follow-on packages after the main app goals are defined and accepted.

---

## Main Product Goals

1. Inkwell opens into a clear daily workspace.
2. Capture stays fast for tasks, notes, lists, and ideas.
3. Notes are easy to browse and reopen.
4. Tasks are easy to scan and act on.
5. Lists feel like real mobile checklists.
6. Search works across all item types without confusion.
7. Sync/account state is readable without learning mystery icons.
8. The app stays simple enough to use many times a day.

---

## Acceptance Shape

Daily Driver v1 is ready for implementation.

Readiness proof:

- `tasks/prd-inkwell-daily-driver-v1.md` is accepted as the package PRD.
- The first implementation slice is `D1 - Daily Workspace Navigation Shell`.
- The slice packet is `documentation/17-daily-driver-v1-slice-1-navigation-shell.md`.
- The second implementation slice is defined as `D2 - Type-First Capture Refinement`.
- The D2 packet is `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md`.
- Secondary project-page and remote-control goals remain outside the first implementation slice.

Daily Driver v1 is accepted after implementation only when:

- Android unit tests pass.
- Lint passes.
- Release or debug APK builds.
- A physical device or emulator screenshot set proves the main lanes and capture flow render correctly.
- Tyler confirms the app feels good enough to use as the daily notes/tasks/lists app.

---

## References

- `tasks/prd-inkwell-daily-driver-v1.md` - detailed package PRD.
- `documentation/17-daily-driver-v1-slice-1-navigation-shell.md` - first implementation slice packet.
- `documentation/18-inkwell-readiness-audit-and-handoff.md` - localized readiness handoff.
- `documentation/19-daily-driver-v1-design-prep.md` - design-tool input contract.
- `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md` - second implementation slice packet.
- `documentation/02-capture-flow.md` - current capture behavior.
- `documentation/03-sync-strategy.md` - current sync behavior.
- `documentation/04-data-model.md` - local fields already available for daily-driver UI.
- `documentation/06-ui-architecture.md` - current screen and navigation model.
- `documentation/08-business-rules.md` - type, search, sync, and checklist rules.
