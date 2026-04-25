# Daily Driver v1 Design Prep

*Created: 2026-04-24 | Project: Inkwell | Package: Inkwell Daily Driver v1*

---

## Purpose

This document localizes the design-tool input packet that was first drafted in the
Dashboard repo. It defines what Tyler should generate or approve before Claude Code
implements a larger visual pass.

This is a design-input contract, not a Compose layout spec.

---

## Product Goal

Inkwell should become Tyler's fast mobile app for notes, tasks, lists, ideas, and
share-intent capture.

The quality target is Google Keep-level speed and clarity, with Nexus-specific
support for sync state, type-aware capture, and later planning handoff.

---

## Required Surfaces

The first design packet should cover these mobile surfaces:

- quick capture
- inbox / unified item list
- task lane
- note lane
- list lane
- item detail and edit
- search and filters
- scheduling handoff entry points
- sync, account, and system status

---

## States To Represent

The design output should include enough states for implementation without guessing:

- empty
- partially filled draft
- save in progress
- saved successfully
- save failed
- offline draft
- populated list
- filtered view
- search active
- no results
- sync pending
- sync failed
- auth expired or disconnected

---

## Fields And Interactions

Core item fields:

- item type: task, note, list, idea
- title
- body/content
- checklist entries
- tags
- created / updated time
- pinned state
- archive / completion state
- source URL
- color
- sync status

Primary interactions:

- create new item
- switch capture type
- edit title/body/tags
- complete task
- archive item
- pin or unpin where supported
- search
- filter
- open detail
- see sync/account state

---

## Design-Tool Input

Use the Claude / Anthropic UI design tool with this intent:

- target surface: Android mobile app
- design mode: mobile
- product goal: fast personal capture and task management
- reference bar: Google Keep-level simplicity and speed
- required item types: task, note, list, idea
- required system states: loading, empty, error, offline, pending sync, failed sync
- scheduling rule: Google Calendar remains the calendar of record

---

## Required Outputs

Before implementation, preserve the approved design material in this repo as one
or more of:

- screenshots or screen exports
- a design brief
- a screen/state inventory
- component notes
- behavior notes
- design tokens or spacing rules if available

The output must be concrete enough that implementation does not need to invent the
visual hierarchy.

---

## Handoff Boundary

Tyler owns:

- generating or approving the visual direction
- deciding when the first pass feels daily-driver ready

Claude Code owns:

- implementing the approved design in the Android codebase
- mapping screens to real data and interactions

Codex owns:

- keeping the slice boundaries honest
- reviewing implementation against the approved design inputs
- preventing project pages or remote-control features from taking over Daily Driver v1

---

## Success Condition

This design-prep slice is successful when the approved design output is saved in
Inkwell and D2 can be implemented without guessing the capture screen structure,
visual density, or type-specific state treatment.
