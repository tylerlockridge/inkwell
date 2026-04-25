# Inkwell Readiness Audit And Handoff

*Created: 2026-04-21 in Obsidian-Dashboard-Desktop | Localized: 2026-04-24 | Project: Inkwell*

---

## Purpose

This document localizes the Inkwell readiness handoff that was first written in
`Obsidian-Dashboard-Desktop`. It is now the Inkwell-side source for the scope
correction that starts Daily Driver v1.

The key correction is:

- Inkwell is primarily a clean note-taking, brainstorming, list, and task app.
- Mobile project pages are important, but they are a secondary Nexus integration.
- Remote-control features must not lead the app until guardrails and output
  visibility exist.

---

## Source Handoff

Original source:

- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\documentation\40-inkwell-readiness-audit-and-handoff.md`

This local file preserves the product decisions so future Inkwell work can resume
inside this repo without relying on the accidental Dashboard thread.

---

## Readiness Findings

### Mobile Integration

The Nexus backend already exposes the core daily-driver APIs Inkwell needs:

- `POST /api/capture`
- `GET /api/inbox`
- `GET /api/note/:uid`
- `PATCH /api/note/:uid`
- `GET /api/lists`
- `GET /api/list/:uid`

The Android app should start from the existing capture, inbox, detail, list, and
sync contracts instead of inventing a new backend contract.

### Product Framing

Daily Driver v1 should feel closer to a polished Google Keep-style mobile app than
to a desktop command center.

The first mobile pass starts with:

- quick capture
- notes lane
- tasks lane
- lists lane
- ideas / brainstorming lane
- item detail and edit flow
- search and filters
- sync, account, and system state

### Project Pages

Project pages belong in Inkwell later, but behind the main note/task/list
experience.

When that package begins, it must use:

- `PROJECT.md` canonical quick fields
- `Status.md` as the human-readable project page
- `project-feedback-log.json` as the save-first feedback timeline
- dashboard-visible project filtering by default
- explicit empty states instead of guessed project summaries

### Remote Operations

Remote operations must stay passive until the safety surface exists.

Allowed first:

1. Show session state from `/api/sessions/status`.
2. Open the terminal when Tyler needs live control.
3. Log every project feedback entry before it becomes execution context.

Not allowed yet:

- direct proceed buttons
- blind prompt submission
- terminal output hidden from Tyler
- command execution without reviewed command packets and action logs

---

## Local Follow-On Docs

- `documentation/19-daily-driver-v1-design-prep.md` - design-tool input packet.
- `documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md` - D2 slice definition.
- `tasks/prd-inkwell-daily-driver-v1.md` - accepted Daily Driver v1 PRD.

---

## Readiness Verdict

Inkwell is ready to continue as its own project from the verified D1 navigation
shell. The next implementation work should not start with project pages. It should
continue the polished daily-driver flow, beginning with D2 Type-First Capture
Refinement.
