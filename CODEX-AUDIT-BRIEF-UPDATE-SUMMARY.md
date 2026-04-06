# CODEX Audit Brief Update Summary

## What was normalized

- Corrected auth-model wording so the brief consistently reflects the shipped shared bearer-token reality in Android and the Chrome extension.
- Corrected Android deep-link references to the currently supported set: `capture`, `inbox`, `note/{uid}`, and `system-health`.
- Corrected `/api/auth/google` wording to match current server behavior: cookie-oriented flow returning `{ success: true }`, not an Android token-return flow.
- Corrected upload-size claims so the brief now reflects Pass 2 verification that the main capture nginx host is still limited to `client_max_body_size 1m`.
- Corrected attachment-flow descriptions so the brief no longer implies multipart capture is equivalent to JSON capture.
- Corrected Chrome extension and SPA sections to reflect the verified security and product-surface issues: `chrome.storage.sync` token storage, broad host permissions, JSON-only offline queue, `/api/inbox` shape mismatch, and mutating list reads.
- Corrected security-model wording so the capture bearer token is no longer described as a server-only secret.
- Replaced the stale known-issues table with Pass 2-aligned follow-up questions and clearly labeled secondary operational context.

## New sections added

- `0.2 Current Open Findings`
- `0.3 Do Not Re-Audit as Open`
- `0.4 Latest Verified Status (Pass 2)`
- `8. Important Unknowns & Secondary Context`
- `9. Research Focus Recommendations`
- `10. Next-Step Remediation Handoff`

## Remaining intentional follow-ups

- Production timezone intent in `infra/config.yaml` remains a follow-up question, not a confirmed defect.
- Multipart memory profile after nginx upload-limit correction still needs runtime validation.
- Cold-start note-indexing behavior after real restarts still needs runtime validation.
- Real-world conflict frequency for list edits and other concurrent mutations still needs runtime validation.
- Redirect/proxy exposure risk for proactive bearer sending on Android remains a runtime follow-up question rather than a confirmed flaw.

## Notes for next model

- Use `C:\Users\tyler\Documents\Claude Projects\Inkwell\CODEX-AUDIT-BRIEF.md` as the navigation and status layer.
- Use `C:\Users\tyler\Documents\Claude Projects\Inkwell\COMPREHENSIVE-AUDIT-FINDINGS.md` for full evidence, severity, status, and recommended directions.
- Do not reopen items listed under `Do Not Re-Audit as Open` without new code or runtime evidence.
- Treat historical audit rows and commit/session notes as context only; they do not override the normalized open-finding table.
