# Comprehensive Audit — Pass 1

## 1. Executive Summary
- Overall impression: the ecosystem has a solid local-first shape, decent test density in the Android app and Node backend, and some thoughtful hardening in storage, attachment validation, and container config. The main blockers are not “basic engineering missing”; they are cross-client contract drift, distributed shared secrets, and a few cold-start/data-loss edge cases that cut across Android, the server, the web SPA, and the Chrome extension.
- Most critical areas:
  - Shared bearer tokens are still embedded in distributed clients.
  - Multipart capture is not contract-consistent across Android, web, extension, and server.
  - Scheduled-task metadata is not persisted end-to-end.
  - Attachment storage can diverge from returned note UID.
  - The web SPA is reading API fields the server does not return.
- Strongest architectural areas:
  - Inkwell’s local persistence layer is materially stronger than average for an app at this stage.
  - The Android sync path is better factored now that `InboxSyncEngine` centralizes pull sync.
  - The Node attachment handler and Docker hardening posture show real defensive intent.

## 2. Audit Scope and Method
- Reviewed the primary brief: `C:\Users\tyler\Documents\Claude Projects\Inkwell\CODEX-AUDIT-BRIEF.md`.
- Reviewed project state/docs first, then read actual source in:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell`
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop`
- Traced these end-to-end flows in code:
  - Android capture -> Ktor client -> `/api/capture` -> vault file generation -> processor -> registry -> `/api/inbox` / `/api/note/:uid` -> Android sync/UI
  - Web SPA capture/offline queue -> `/api/capture` and `/api/inbox`
  - Chrome extension capture/settings -> `/api/capture`
  - Device registration / FCM token flow
  - Attachment upload and retrieval
  - List read/update flows
  - Processor initial scan/watch behavior
  - Nginx/Docker runtime posture
- Conclusions are based on direct code inspection plus spot-checking existing tests. I did not rely on repository summaries when determining findings.

## 3. Confirmed Findings

### P1-001 — Android APK still embeds and silently reuses the server bearer token
Severity: Critical  
Confidence: High  
Component: Inkwell Android auth/config

Evidence:
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\build.gradle.kts:33` injects `BuildConfig.DEFAULT_AUTH_TOKEN`.
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt:51` initializes `_authToken` from `BuildConfig.DEFAULT_AUTH_TOKEN`.
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt:62-63` reads the encrypted pref with `BuildConfig.DEFAULT_AUTH_TOKEN` as fallback.
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt:163` falls back to `BuildConfig.DEFAULT_AUTH_TOKEN` again when the saved token is blank.

Why it matters:
- The effective API credential is shipped inside the APK and reused automatically.
- Any extracted APK or leaked build artifact yields the bearer token needed to impersonate a client.
- This also prevents a clean separation between “configured” and “anonymous” client states.

Suggested direction:
- Remove the static bearer fallback from shipped builds.
- Move to per-user or per-device auth, or at minimum a bootstrap token exchanged for a revocable scoped credential.

### P1-002 — Chrome extension ships the bearer token, stores it in sync storage, and has wildcard host access
Severity: Critical  
Confidence: High  
Component: Chrome extension security

Evidence:
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\manifest.json:15-18` grants host access to `http://*/*` and `https://*/*`.
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\background.js:24-28` seeds `chrome.storage.sync` with a default `serverUrl` and hardcoded `authToken`.
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\background.js:50-71` reads that token and sends it in the `Authorization` header.
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\options.js:10-12,27` reads/writes `authToken` from `chrome.storage.sync`.
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\sidepanel.js:59-63,111-112,312-325` loads the token from sync storage and uses it for capture/defaults requests.

Why it matters:
- The token is not only embedded in the extension package; it is also synced through browser profile storage.
- The host scope is broader than the extension needs.
- This makes the extension the weakest auth link in the system.

Suggested direction:
- Remove the packaged token entirely.
- Restrict host permissions to the actual deployment origin.
- Stop storing long-lived bearer secrets in `chrome.storage.sync`.

### P1-003 — Multipart capture is lossy and inconsistent across Android, web, extension, and server
Severity: Critical  
Confidence: High  
Component: Cross-platform capture contract

Evidence:
- Android request model includes schedule/list fields:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\dto\CaptureRequest.kt:11-21`
- Android multipart sender only appends `body`, `title`, `tags`, `kind`, `date`, `priority`, `source`, `uuid`, `captureType`:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\CaptureApiService.kt:71-80`
- `UploadWorker` passes note schedule fields into the request, then routes attachment uploads through that multipart path:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\sync\UploadWorker.kt:116,118,125`
- Web SPA multipart uses `FormData` and stringifies arrays:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js:469-473`
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js:697-702`
- Chrome extension does the same:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\sidepanel.js:304-318`
- Server multipart parsing only reads a subset of fields and never reads `startTime`, `endTime`, `calendar`, `listName`, `items`, `persistent`, or `shared`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:735-749`
- Server multipart routing only distinguishes `note` vs “everything else”; it never calls `handleListCapture()` for multipart list captures:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:816-831`

Why it matters:
- Attachment-backed captures do not preserve the same semantics as JSON captures.
- Multipart list captures can degrade into ordinary tasks.
- Array fields are serialized differently by clients than the server expects, so tags/items are not reliably preserved.

Suggested direction:
- Define one canonical capture contract and enforce it across Android, SPA, extension, and server.
- Make multipart and JSON paths use the same field model and the same routing rules.
- Add end-to-end contract tests for each capture type with and without attachments.

### P1-004 — Server attachment files can be stored under a UID that does not match the returned note UID
Severity: High  
Confidence: High  
Component: Server attachment handling

Evidence:
- Multipart attachments are saved before final note processing:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:751-775`
- The attachment directory UID is computed from the pre-correction body:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:757-760`
- Dedupe check happens only after files are already saved:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:777-783`
- Final note UID is computed later, after spelling/enrichment may have changed the body:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:786-812`

Why it matters:
- The note can reference attachments that were saved under a different directory than the note UID implies.
- Duplicate UUID retries can still write files before returning the cached result.
- This is a correctness issue, not just cleanup debt.

Suggested direction:
- Compute the final UID before saving any attachment.
- Run dedupe before any attachment write.
- Add a test that asserts returned `uid` and attachment directory match exactly.

### P1-005 — Scheduled-task metadata is not persisted end-to-end for `/api/capture`
Severity: High  
Confidence: High  
Component: Server capture pipeline / Android sync contract

Evidence:
- Server capture contract claims schedule fields:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:145-148`
- `generateMarkdown()` only writes `due_date`, not `date`, `startTime`, `endTime`, or `calendar`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:522-535`
- Normalization only reads `fm.due`, not `fm.due_date`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\normalizer.ts:148-151`
- GCal worker only processes items when `fm.date` and `fm.startTime` exist, and reads `fm.calendar`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\worker.ts:149-180`
- Android note detail DTO expects `date`, `startTime`, `endTime`, and `calendar` back from the server:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\dto\NoteDetailResponse.kt:22-25`

Why it matters:
- A task created with scheduling metadata through `/api/capture` does not round-trip as the clients expect.
- The registry due-date field and the worker scheduling logic are using different frontmatter names.
- This breaks the “capture once, sync everywhere” model for scheduled work.

Suggested direction:
- Standardize frontmatter field names.
- Make capture generation, normalization, worker sync, and mobile DTOs all use the same schedule schema.

### P1-006 — Processor cold-start scan ignores `Inbox/Notes`, leaving note registry state incomplete after restart
Severity: High  
Confidence: High  
Component: Processor / registry / note APIs

Evidence:
- Initial scan only walks four inbox task folders:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\scanner.ts:109-115`
- The watcher does include notes and lists:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\index.ts:137-138`
- Note list/detail APIs depend on the registry:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:354-355`
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:409-411`

Why it matters:
- Existing note files present before processor startup are not guaranteed to be queryable through the API until they change on disk.
- This creates a cold-start blind spot for `/api/inbox?type=reference` and `/api/note/:uid`.

Suggested direction:
- Include `Inbox/Notes` in the initial scan path, or explicitly backfill note registry state before serving note APIs.

### P1-007 — Web task/note views expect `/api/inbox` fields the server does not return
Severity: High  
Confidence: High  
Component: Web SPA / API contract

Evidence:
- SPA task view expects `body`, `priority`, `tags`, and `due_date`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\views\tasks-view.js:88-128`
- SPA notes view expects `body`, `tags`, `color`, and `pinned`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\views\notes-view.js:108-140`
- Registry `ItemRecord` only defines `uid`, `path`, `content_hash`, `status`, `kind`, `updated_at`, optional `title`, and optional `due_date`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\registry.ts:3-11`
- `/api/inbox` returns `registry.queryItems(...)` directly:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:354-355`
- `queryItems()` selects `SELECT * FROM items`, not parsed note bodies/tags/colors:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\registry.ts:524-529`

Why it matters:
- The task/note views are coded against a richer payload than the API actually serves.
- This is a direct product gap, not a cosmetic issue.

Suggested direction:
- Either enrich `/api/inbox` to serve the fields those views need, or rewrite the views around the real API shape.
- Add client/server contract tests for each SPA view.

### P1-008 — The lists SPA mutates list files just to read list contents
Severity: Medium  
Confidence: High  
Component: Web SPA lists flow

Evidence:
- `lists-view.js` explicitly uses a rename-to-self workaround because there is no `GET /api/list/:uid`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\views\lists-view.js:122,127`
- `handleUpdateList()` always rewrites the file and bumps `fm.updated`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:829-848`

Why it matters:
- Expanding a list in the UI is not read-only; it changes sync timestamps and rewrites the underlying file.
- That creates noisy churn and makes future conflict analysis harder.

Suggested direction:
- Add a real `GET /api/list/:uid` endpoint and stop using a mutating write path for reads.

### P1-009 — Web offline queue silently drops attachments
Severity: High  
Confidence: High  
Component: Web SPA offline behavior

Evidence:
- The form uses `FormData` when files are selected:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js:697-702`
- On failure it queues only the JSON payload:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js:760`
- The offline queue stores `payload` plus UUID and later replays `JSON.stringify(entry.payload)` to `/api/capture`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\offline-queue.js:64-86`
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\offline-queue.js:166-170`

Why it matters:
- Offline file captures are not actually recoverable.
- The failure mode is silent data loss of attachments while the text payload survives.

Suggested direction:
- Either block offline attachment submission explicitly or add real Blob/File persistence and replay support.

### P1-010 — Android device registration is not retried on normal app startup
Severity: Medium  
Confidence: High  
Component: Inkwell notifications / FCM registration

Evidence:
- App startup only schedules sync and ensures a device ID:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\CaptureApp.kt:39-40`
- `DeviceRegistrationManager.registerWithServer()` exists but is not used from normal startup:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\notifications\DeviceRegistrationManager.kt:31-41`
- Token registration is retried from `onNewToken()` only:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\notifications\CaptureMessagingService.kt:39-42`
- Registration failures there are swallowed:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\notifications\CaptureMessagingService.kt:186-198`

Why it matters:
- If the FCM token already exists, or the server URL changes, or a prior registration failed, the app has no normal cold-start recovery path.
- That can leave push registration stale without obvious user feedback.

Suggested direction:
- Register on app startup when server URL and token are present, with backoff/telemetry.

### P1-011 — Nginx blocks attachment sizes the app and server are designed to support
Severity: High  
Confidence: High  
Component: Deployment / nginx

Evidence:
- Main nginx server block sets `client_max_body_size 1m`:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\nginx.conf:45`
- Server attachment handling is built for much larger payloads:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\attachment-handler.ts:28`
- Android client explicitly streams files and uses long upload timeouts:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\CaptureApiService.kt:92-106`

Why it matters:
- The deployed reverse proxy is stricter than the server/application contract.
- In practice, legitimate attachment uploads above 1 MB will fail before the app/server code gets a chance to handle them.

Suggested direction:
- Align nginx request limits with the server’s per-file and total upload limits.
- Add a deployment-level test that fails when proxy and server limits drift.

## 4. Likely Risks / Needs Pass-2 Verification

### P1-R01 — Proactive bearer sending in Ktor may still be broader than intended
Hypothesis:
- The Android authenticated Ktor client may attach the bearer token more broadly than intended because it uses `sendWithoutRequest { true }`.

Why suspicious:
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\di\NetworkModule.kt:53-64`
- Current code appears safe if every request stays on the configured base URL with no cross-host redirects, but the implementation is explicitly “always send”.

Files/functions to verify next:
- `NetworkModule.kt`
- All Ktor call sites in `CaptureApiService.kt`
- Any redirect behavior in production infra

### P1-R02 — Android Google auth flow appears stale/dead, but should be verified before removal
Hypothesis:
- The Android Google auth client code is no longer compatible with the server and may now be dead code.

Why suspicious:
- Android still expects a token response:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\CaptureApiService.kt:238-255`
- Server now returns cookie auth success:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:910-936`
- Current code search found no active Kotlin call site for `exchangeGoogleToken(...)`.

Files/functions to verify next:
- `CaptureApiService.exchangeGoogleToken`
- any remaining Android auth UI/view-model code
- `ApiDtoTest.kt`
- `/api/auth/google` usage in web flows

### P1-R03 — List update concurrency likely devolves to last-write-wins with no merge protection
Hypothesis:
- Concurrent list edits from multiple clients can overwrite each other because list updates are read-modify-write file rewrites with no version or compare-and-swap guard.

Why suspicious:
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts:796-848`
- The route reads the file, mutates in memory, rewrites, and returns, but there is no ETag, updated check, or merge token.

Files/functions to verify next:
- `handleUpdateList()` in `api-server.ts`
- extension list write paths
- web list UI write paths
- any mobile list write clients added later

### P1-R04 — Multipart upload handling buffers the full request body in memory
Hypothesis:
- Once nginx limits are raised, concurrent large uploads may create avoidable memory pressure because multipart bodies are fully buffered before parsing.

Why suspicious:
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts:678-703`
- The server enforces a 50 MB request cap, but still builds the entire payload in memory.

Files/functions to verify next:
- `handleMultipartCapture()` in `capture-server.ts`
- production memory limits in `docker-compose.yml`
- realistic concurrent upload volume

### P1-R05 — Production timezone configuration needs explicit confirmation
Hypothesis:
- The deployed timezone may be wrong for current expected behavior.

Why suspicious:
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\config.yaml:2` sets `America/Los_Angeles`.
- The current user/session context is Eastern, and multiple flows derive defaults and scheduling behavior from app config.

Files/functions to verify next:
- `infra/config.yaml`
- `capture-server.ts` schedule defaulting
- `worker.ts`
- expected user-facing timezone requirements

## 5. Strengths and Good Decisions
- Inkwell’s local storage split is sound. `PreferencesManager` combines DataStore for app settings with `EncryptedSharedPreferences` for secrets and exposes reactive flows rather than one-shot getters. Relevant file: `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt`.
- The Android persistence layer is better than average for a capture app. Room includes FTS plus triggers and a migration for attachment URI support:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\AppDatabase.kt:13,33-80`
- `NoteDao` uses transactions for the highest-risk sync replacement paths:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\dao\NoteDao.kt:13-25,96-104`
- The Android pull-sync architecture is cleaner after centralizing the logic in `InboxSyncEngine`, and there are real tests around tombstones and cancellation:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\sync\InboxSyncEngine.kt`
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\test\kotlin\io\inkwell\SyncWorkerIntegrationTest.kt`
- Server attachment handling has real defensive controls: MIME allowlist, per-file/total limits, and path checks, with dedicated tests:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\attachment-handler.ts:12,28,49-78`
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\test\attachment-handler.test.ts`
- API auth hardening is materially better than a bare personal project baseline. Cookie fallback, auth-failure logging, and 401 throttling are covered by tests:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\test\api-server.test.ts`
- Container hardening is not superficial. `docker-compose.yml` repeatedly uses `no-new-privileges` and `cap_drop`, and the capture-web container binds to localhost only:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\docker-compose.yml:70-71,114-115,156-157,201-205,255-256,295-296`

## 6. Test Gaps
- No end-to-end contract test proves multipart captures preserve `startTime`, `endTime`, `calendar`, `listName`, `items`, `persistent`, and `shared` consistently across Android/web/extension/server.
- No test asserts that attachment files are saved under the same UID directory the API returns to the client.
- No cold-start processor test covers existing `Inbox/Notes` files surviving a processor restart and remaining visible via `/api/inbox?type=reference` or `/api/note/:uid`.
- No SPA/API contract test verifies that `/api/inbox` returns the fields required by `tasks-view.js` and `notes-view.js`.
- The offline queue tests cover JSON payload replay only; they do not cover attachments or intentionally rejected attachment flows:
  - `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\test\offline-queue.test.ts`
- Android has tests around token registration helper logic, but not a startup-level test that proves device registration retries on app launch with an existing token:
  - `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\test\kotlin\io\inkwell\CaptureMessagingTokenTest.kt`
- I did not find extension-focused security tests for permission scope, token storage, or request origin constraints.
- I did not find deployment tests that keep nginx body limits aligned with server attachment limits.
- I did not find concurrency tests for simultaneous list edits from multiple clients.

## 7. Productization Gaps
- Shared long-lived auth secrets are still being distributed to end-user clients. That is the clearest blocker to professionalization.
- Cross-client API contracts have drifted enough that “same feature, different client” no longer means the same stored result.
- Documentation is materially out of sync with the code in several places, especially around auth behavior, Google sign-in, and some deep-link/settings details.
- Several critical flows fail silently or near-silently:
  - web offline attachments
  - FCM registration retry gaps
  - stale/dead auth paths
- Some key modules are too large and too multi-purpose for easy change confidence:
  - `src/api-server.ts`
  - `src/capture-server.ts`
  - `src/capture-web/public/js/form.js`
  - `capture-extension/sidepanel.js`
- The extension and SPA are currently weaker trust boundaries than the Android app and server core.

## 8. Suggested Pass-2 Investigation Plan
1. Build a contract matrix for `CaptureRequest` and verify every capture type across JSON and multipart from Android, web, and extension.
2. Reproduce attachment uploads end-to-end and verify returned UID, attachment directory, note frontmatter, and attachment retrieval all match.
3. Verify scheduled task round-trip: capture -> markdown/frontmatter -> processor/registry -> `/api/note/:uid` -> Android sync -> worker GCal eligibility.
4. Exercise cold-start behavior by restarting the processor with pre-existing notes and confirming registry/API visibility.
5. Run concurrency checks on list updates from two clients to determine whether explicit versioning is needed.
6. Validate auth cleanup direction: remove shipped secrets, decide whether Google auth should be removed or rebuilt, and scope extension permissions to the real deployment origin.
