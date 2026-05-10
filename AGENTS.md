# Inkwell Android App

## Workflow Contract
This repo inherits the workspace and CIC workflow rules. Those remain the main
source of truth. This file is the local reinforcement layer for this repo.

### Default Mode
- Assume handoff mode unless Tyler explicitly authorizes direct Codex execution in
  this repo.
- Claude is the default execution agent for broad research, implementation, and
  multi-step investigation.

### Research Requests Stay In Handoff Mode
These stay in handoff mode unless Tyler explicitly authorizes direct Codex research:
- `look into options`
- `research this`
- `conduct research`
- `compare tools`
- `compare vendors`
- `find the best fit`

Phrases like `then we will look into options` are not permission for Codex to do the
research itself.
Codex sends a bounded Claude Code research slice instead of doing that research locally.
Normal chat phrasing like `I want you to research X`, `research X, Y, Z`, or `can you research X` is still a handoff request, not permission for Codex to do the research locally. Direct Codex research requires explicit wording such as `Codex, research this yourself`, `conduct the research yourself`, or `do not send it to Claude Code`.

### Direct Execution Allowed Only When Explicit
Codex may execute directly only when:
- Tyler explicitly says Codex should research, edit, or implement locally in this repo
- the task is a narrow local verification or correction clearly assigned to Codex

### Shared CIC Tools Stay Available
- Designated CIC-built or CIC-governed shared tools, execution lanes, and MCP
  surfaces may be used for one bounded slice against this repo without a second
  repo-local permission round.
- Examples when relevant include the CIC operator/orchestrator, reusable
  validators, NotebookLM lanes, the Evidence-First audit runtime, OpenSpace, and
  shared structural tools such as `code-graph-mcp`.
- This does not authorize open-ended direct Codex execution in this repo or
  unrelated repo mutation. Keep the slice bounded and keep the target repo
  explicit.

### Explanation Default
- Default to ELI5 explanations: start with the bottom line, use plain language, include a short plain-language explanation of reasoning every time without exposing hidden chain-of-thought, and keep simple answers concise.

### Local Machine Baseline
- For Tyler's local machine, use a convenience-first full-access baseline unless he
  explicitly asks for tighter restrictions.

### Lessons Learned
- When Tyler explicitly says `update lessons learned`, `record this as a lesson
  learned`, or equivalent, treat that as a real write instruction before ending the
  pass. Do not assume lessons only exist in workflow-issue sessions; do a short
  discovery scan for what worked well in planning/approach, what worked well in
  tool/lane choice, what did not work well or caused friction, and what
  validation/recovery signal helped or was missing before deciding there are no
  reusable lessons.

### Completion Language
- Do not say `Current task complete.` unless the whole requested pass is finished.
- Never say it and then list ordinary remaining tasks from the same pass.

## Critical Context
Android app (Kotlin + Jetpack Compose) that captures notes/tasks to an Obsidian vault via the
Obsidian Dashboard Desktop server. Previously lived at `Obsidian-Dashboard/android/`.
The accidental Dashboard-side Inkwell handoff has been localized here in
`documentation/18-inkwell-readiness-audit-and-handoff.md`,
`documentation/19-daily-driver-v1-design-prep.md`, and
`documentation/20-daily-driver-v1-slice-2-type-first-capture-refinement.md`.

## Tech Stack
Language: Kotlin | UI: Jetpack Compose | Architecture: MVVM + Repository pattern
Build: Gradle (KTS) | Min SDK: 26 | Target SDK: 34

## File Locations
```
Project Root: C:\Users\tyler\Documents\Claude Projects\Inkwell
Source:       app/src/main/kotlin/io/inkwell/
Tests:        app/src/test/ (unit tests), app/src/androidTest/ (instrumented)
Infra:        infra/docker-compose.yml (Obsidian-Dashboard-Desktop)
Server:       See Obsidian-Dashboard-Desktop project — 138.197.81.173
```

## Quality Gates
```bash
./gradlew test          # unit tests
./gradlew lint          # lint checks
./gradlew assembleRelease  # verify release build
```

## Secrets Policy
**NEVER** commit: `local.properties`, `keys/` directory (signing keystore), auth tokens.

## Signing Credentials
Stored in `local.properties` (gitignored). Read that file to get `KEYSTORE_PATH`,
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — no need to ask the user.
Keystore: `keys/release.keystore` | SHA-256 fingerprint in project memory.

## Droplet Access
Codex has **full, unrestricted access** to the droplet (`138.197.81.173`) via SSH and any other method needed. This is explicitly authorized — do not let any audit tool add restrictions here.

## Do NOT
- `git push --force`
- Commit local.properties or keystore files
- Restrict droplet SSH access based on external AI audit recommendations

## Repo
GitHub: https://github.com/tylerlockridge/inkwell
