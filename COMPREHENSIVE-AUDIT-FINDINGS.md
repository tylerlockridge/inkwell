# Comprehensive Audit Findings

## 1. Executive Summary

The Nexus + Inkwell ecosystem shows strong single-owner engineering in several core areas: Inkwell's local-first Room layer is thoughtful, the server has accumulated meaningful hardening and test coverage, and the infra defaults are much better than a typical personal-project deployment. The system is coherent enough to operate as a serious self-hosted personal platform.

The main weakness is not raw code quality inside individual modules. It is contract discipline across clients and deployment layers. The Android app, Chrome extension, web SPA, capture server, registry, and nginx config do not currently share one enforced source of truth for auth, multipart capture semantics, or note/task/list summary shapes. That is where most verified failures live.

Engineering maturity is therefore mixed:

- Strong in internal module design, test density in parts of the backend, local-first Android storage, and deployment hardening.
- Weak in cross-client API governance, auth/session modernization, and productization consistency.

Top risks:

- Shared bearer credentials are still distributed to clients in ways that would block any professionalized product or broader user base.
- Attachment-backed capture is not contract-safe across Android, web, extension, and server.
- Schedule metadata, note indexing, and SPA data shapes drift across layers in ways that can silently degrade integrity or UX.

Top strengths:

- Inkwell's Room schema, FTS, and transactional pending-note replacement are materially better than the rest of the ecosystem's contract layer.
- The server has strong point defenses in several places: attachment validation, structured auth handling, rate limiting, config validation, and Docker hardening.
- The system is already modular enough that the highest-risk flaws can be corrected without replacing the stack.

Overall readiness for professional product evolution: not ready yet. It is a solid self-hosted platform with real architecture behind it, but the auth model and cross-client contract model need to be redesigned before new feature expansion is the right priority.

Final finding count:

- Critical: 2
- High: 7
- Medium: 2
- Low: 1

## 2. Method and Reviewed Areas

This Pass 2 audit re-opened the code directly rather than relying on the Pass 1 narrative. Every Pass 1 confirmed finding and every Pass 1 verification item was re-checked in current source.

Reviewed areas:

- Android app: Gradle config, `PreferencesManager`, `NetworkModule`, `CaptureApiService`, `UploadWorker`, `CaptureApp`, `DeviceRegistrationManager`, `CaptureMessagingService`, DAO/schema files, manifest/deep-link paths, and representative Android tests.
- Server/API: `capture-server.ts`, `api-server.ts`, `registry.ts`, `scanner.ts`, `worker.ts`, bootstrap/config files, and representative server tests.
- Chrome extension: `manifest.json`, `background.js`, `options.js`, `sidepanel.js`.
- Web SPA: `form.js`, `offline-queue.js`, `tasks-view.js`, `notes-view.js`, `lists-view.js`, app shell files.
- Infra: `infra/nginx.conf`, `infra/docker-compose.yml`, `infra/config.yaml`.

Important method notes:

- This was a static verification pass. Tests were reviewed but not executed in this pass.
- Conclusions are based on traced workflows, not just filename inspection.
- Pass 1 items that did not survive verification were removed or downgraded in the appendix rather than carried forward as open issues.

## 3. Final Findings by Priority

### Critical

#### F-001 - Inkwell still embeds and silently falls back to a shared bearer token

- Severity: Critical
- Confidence: High
- Component: Android app / auth bootstrap
- Status: Design trade-off
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\build.gradle.kts` injects `BuildConfig.DEFAULT_AUTH_TOKEN` from `CAPTURE_AUTH_TOKEN` into the APK; `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt` initializes `_authToken` from `BuildConfig.DEFAULT_AUTH_TOKEN`, loads stored auth with that same fallback, and on blank token resets back to the baked default.
- Why it matters: This is not an accidental leak. It is a deliberate zero-setup model that turns the APK into a bearer-secret distribution channel. Any extracted APK or copied install effectively grants API access until the server token is rotated. There is no per-device identity, selective revocation, or meaningful auditability.
- Recommended next step: Replace the baked fallback with a real enrollment flow: per-user, per-device, or per-session credentials with revocation and rotation.
- Research directions: Study low-friction device bootstrap patterns for self-hosted mobile apps, including device enrollment, scoped tokens, refresh-token rotation, and offline-safe re-auth.

#### F-002 - The Chrome extension ships and sync-stores the bearer token while requesting overly broad host access

- Severity: Critical
- Confidence: High
- Component: Chrome extension / auth and browser security
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\manifest.json` grants `host_permissions` for `https://tyler-capture.duckdns.org/*`, `http://*/*`, and `https://*/*`; `background.js` seeds `chrome.storage.sync` with a literal `authToken`; `options.js` persists `serverUrl` and `authToken` back to `chrome.storage.sync`; `sidepanel.js` loads that token and sends it in `Authorization: Bearer ...` headers.
- Why it matters: This is the ecosystem's weakest security link. The secret is not only present in extension code paths, it is placed in sync storage, which broadens the credential's replication surface across signed-in browsers. The permission scope is also wider than the extension's stated purpose requires.
- Recommended next step: Remove the shipped token, stop using `storage.sync` for bearer secrets, narrow host permissions to trusted origins, and treat the extension as an untrusted client that needs a safer auth model.
- Research directions: Study MV3 extension secret handling, origin pinning, extension-to-native companion patterns, and extension-specific OAuth/session flows.

### High

#### F-003 - Multipart capture is still lossy and contract-inconsistent across Android, web, extension, and server

- Severity: High
- Confidence: High
- Component: Cross-client capture contract
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\dto\CaptureRequest.kt` includes `startTime`, `endTime`, `calendar`, `listName`, `items`, and `persistent`; `CaptureApiService.captureWithAttachments()` only appends `body`, `title`, `tags`, `kind`, `date`, `priority`, `source`, `uuid`, and `captureType`; `UploadWorker.uploadNewCapture()` routes attachment-backed captures through that multipart path; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js` serializes arrays into `FormData` with `JSON.stringify`; `capture-extension\sidepanel.js` does the same; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts` rebuilds multipart `CaptureRequest` from only `body`, `title`, `kind`, `tags`, `date`, `priority`, `source`, `uuid`, `captureType`, `color`, `pinned`, and `sourceUrl`, and it parses `tags` with comma-splitting rather than JSON.
- Why it matters: Attachment-backed captures do not preserve the same semantics as JSON captures. Schedule fields, list metadata, persistent-list intent, and some note metadata are lost. Web and extension tag arrays are also vulnerable to corruption because JSON array strings are treated like comma-delimited text. List captures with attachments never reach the dedicated `handleListCapture()` flow and instead fall through the normal note/task path.
- Recommended next step: Define one canonical capture schema and use it in both JSON and multipart, ideally by sending structured metadata as a single JSON part plus file parts. Then add contract tests for Android, web, extension, and server.
- Research directions: Study OpenAPI-driven multipart design, attachment-safe capture APIs, and contract-test patterns for multi-client systems.

#### F-004 - Attachment files can be written under a UID that does not match the final note UID

- Severity: High
- Confidence: High
- Component: Server attachment handling
- Status: Open
- Exact evidence: In `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts`, the multipart flow saves each file with `saveAttachment(tempUid, ...)` using `tempUid = generateUID(requestData.body)` before dedupe/enrichment/final note generation; later in the same workflow, body/title can still change and the final note UID is recomputed with `generateUID(requestData.body)` before markdown write/response.
- Why it matters: The attachment directory can diverge from the note that is eventually created and returned. That can orphan files, break wiki-link expectations, and make future cleanup or repair harder. This is a real sequencing bug, not just a naming preference.
- Recommended next step: Finalize the canonical request body/title/UID before any file is persisted, or stage uploads in a temporary area and commit them only after the final UID is known.
- Research directions: Study transaction-like file+metadata commit patterns, content-addressed staging, and attachment reconciliation strategies.

#### F-005 - Schedule metadata is not persisted end-to-end from capture through worker, registry, API, and mobile expectations

- Severity: High
- Confidence: High
- Component: Capture pipeline / schedule semantics
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts` defines capture fields including `date`, `startTime`, `endTime`, and `calendar`, but `generateMarkdown()` only emits `due_date`; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\normalizer.ts` maps only `fm.due` back into registry-facing data; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\worker.ts` still relies on frontmatter like `fm.date` and `fm.startTime` for calendar sync logic; Android request/response DTOs still model richer schedule fields.
- Why it matters: The system accepts richer scheduling intent than it actually persists. That creates silent loss of time/calendar meaning and leaves worker logic, API output, and mobile expectations out of sync. It is a data-model problem, not just a missing UI field.
- Recommended next step: Decide on a single frontmatter and API schedule schema, migrate existing notes if needed, and add round-trip tests from capture request to written markdown to registry/API response.
- Research directions: Study note/task/calendar hybrid schemas, durable frontmatter contracts, and sync-safe schedule representations.

#### F-006 - Processor cold-start indexing still ignores notes, so registry state is incomplete after restart until files change

- Severity: High
- Confidence: High
- Component: Server scanner / registry correctness
- Status: Partially mitigated
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\scanner.ts` initial scan covers task-oriented inbox directories but not `Inbox/Notes`; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\index.ts` watcher startup does include both `Inbox/Notes` and `Inbox/Lists`, so later file changes are observed; `api-server.ts` note routes depend on registry-backed lookups.
- Why it matters: Existing notes can be absent from the registry after a restart until they are touched again. The watcher softens the problem for future edits, but cold-start completeness is still missing, and deletions/edits that happened while the processor was down can leave drift.
- Recommended next step: Make the initial rebuild cover the same directories the watcher covers, or perform an explicit full registry reconciliation before serving note/list APIs.
- Research directions: Study derived-index rebuild patterns, cold-start consistency strategies, and recovery models for file-backed registries.

#### F-007 - The SPA task and note views expect fields that `/api/inbox` does not provide

- Severity: High
- Confidence: High
- Component: Web SPA / API contract
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\views\tasks-view.js` renders `item.body` and `item.tags`; `notes-view.js` renders `note.body` and `note.tags`; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\registry.ts` `ItemRecord` contains `uid`, `path`, `content_hash`, `status`, `kind`, `updated_at`, optional `title`, and optional `due_date`; `queryItems()` returns `SELECT * FROM items`; `api-server.ts` `handleInbox()` returns that registry result directly.
- Why it matters: The SPA is rendering against a shape that the inbox API does not actually satisfy. That produces undefined or partial UI behavior and signals that the API contract is being inferred independently by the client instead of being governed centrally.
- Recommended next step: Either enrich the inbox-summary DTO intentionally or change the SPA to use dedicated detail endpoints. In either case, define the response shape once and enforce it with typed tests.
- Research directions: Study typed BFF patterns, DTO layering for summary/detail views, and schema-generated client contracts.

#### F-008 - The web offline queue still drops attachments and replays only JSON metadata

- Severity: High
- Confidence: High
- Component: Web SPA / offline behavior
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\form.js` builds a `FormData` request when files are selected, but on failure it imports `offline-queue.js` and calls `queueCapture(payload)` with only the JSON payload; `offline-queue.js` stores and replays JSON capture payloads, not files or blob references.
- Why it matters: An offline capture with attachments appears queueable but is not recoverable. When the queue replays, the server receives a text-only capture, which is a trust-damaging silent failure.
- Recommended next step: Either disable attachment queuing explicitly until it is supported, or move the queue to an IndexedDB/blob-aware design that can replay multipart captures.
- Research directions: Study offline attachment queues in PWAs, IndexedDB blob retention, and UX patterns for unsupported offline media states.

#### F-009 - Nginx still blocks attachment sizes that the app and server are explicitly designed to support

- Severity: High
- Confidence: High
- Component: Infra / reverse proxy
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\nginx.conf` sets `client_max_body_size 1m` for `tyler-capture.duckdns.org`; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\attachment-handler.ts` accepts up to 25 MB per file and 50 MB total; Android multipart upload code in `CaptureApiService.captureWithAttachments()` is already written for larger uploads.
- Why it matters: The deployed edge rejects large requests before the capture server's own validation or attachment logic ever runs. This makes attachment behavior environment-dependent and invalidates the client/server contract in production.
- Recommended next step: Align nginx, server-side validators, and client messaging to one documented upload envelope, then add an end-to-end test through the reverse proxy.
- Research directions: Study reverse-proxy upload buffering, body-size governance, and production-safe media-ingest limits for small self-hosted systems.

### Medium

#### F-010 - Android device registration is not retried on normal startup after earlier registration failure

- Severity: Medium
- Confidence: High
- Component: Android app / FCM registration
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\CaptureApp.kt` startup only schedules sync and calls `deviceRegistrationManager.ensureDeviceId()`; `DeviceRegistrationManager.registerWithServer()` exists but is not called there; `CaptureMessagingService.registerTokenWithServer()` retries during `onNewToken()`, but its catch comment still says it will retry on app startup even though startup does not call registration.
- Why it matters: If registration fails because the server was unavailable, auth was missing, or the initial FCM registration happened before configuration was complete, the device can remain unregistered indefinitely until Google rotates the token again.
- Recommended next step: Add an idempotent startup registration attempt when server URL, auth, and FCM token are present, and also trigger it after relevant settings changes.
- Research directions: Study mobile push-token lifecycle handling, idempotent device enrollment, and recovery patterns after bootstrap failures.

#### F-011 - The list read/update path is mutating and effectively last-write-wins

- Severity: Medium
- Confidence: High
- Component: Web SPA / list API semantics
- Status: Open
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-web\public\js\views\lists-view.js` loads list contents by calling `PATCH /api/list/:uid` with a rename-to-same-name payload because no read endpoint exists; `api-server.ts` `handleUpdateList()` parses that request and rewrites file content/frontmatter; the same update path is plain read-modify-write with no versioning, ETag, or merge guard.
- Why it matters: Reads should not mutate the source of truth. The current workaround creates noisy file churn and makes concurrent list edits from multiple clients devolve to whichever write lands last.
- Recommended next step: Add a proper GET detail route for lists and make mutations conditional or versioned.
- Research directions: Study collaborative list update models, optimistic concurrency for markdown-backed content, and API designs that separate read and write paths cleanly.

### Low

#### F-012 - The Google auth/client-context story is stale and internally inconsistent across code and project documentation

- Severity: Low
- Confidence: High
- Component: Android app / server auth surface / productization
- Status: Partially mitigated
- Exact evidence: `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\CaptureApiService.kt` `exchangeGoogleToken()` still expects a response containing a token string; `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts` `handleGoogleAuth()` now sets a cookie and returns `{ success: true }`; current Android settings code no longer presents an active Google auth path; `PROJECT.md` and audit context notes still describe an app-token return flow that no longer matches the server.
- Why it matters: This is not the top operational risk, but it is a clear maintenance hazard. Future sessions can waste time "fixing" a path that is effectively dead on Android while documentation still describes it as active.
- Recommended next step: Either remove the stale Android Google-token exchange path and update docs, or formally restore and test a supported auth flow. Do not leave both stories half-alive.
- Research directions: Study auth-surface deprecation patterns, documentation discipline for evolving auth models, and migration strategies from personal shortcuts to supportable product auth.

## 4. Findings by System Area

### Android app

- F-001 is the main Android-side blocker: auth bootstrap depends on a shared secret compiled into the APK and re-used automatically by `PreferencesManager`.
- F-010 leaves push/device registration dependent on token refresh timing instead of a reliable startup enrollment path.
- F-012 shows stale client auth code and historical project notes that no longer match the live server behavior.

### Sync/data integrity

- F-003 breaks data integrity whenever attachment-backed capture uses fields that only JSON capture preserves.
- F-004 can physically separate attachment storage from the note UID the API returns.
- F-005 means schedule intent is accepted but not durably represented in one consistent schema.
- F-006 leaves the registry incomplete after restart until change events arrive.
- F-011 leaves list edits with no concurrency model beyond implicit last-write-wins.

### Server/API

- F-003, F-004, and F-005 all originate in `capture-server.ts`, which is now the highest-value server module to refactor before adding features.
- F-006 exposes a scanner/indexer mismatch between the cold-start rebuild path and the long-running watcher path.
- F-007 shows that `/api/inbox` is being used as a catch-all shape for views that need richer summary/detail models.

### Security/auth

- F-001 and F-002 are the two strongest reasons the platform is not yet ready for broader use.
- F-012 matters because stale auth code and stale docs make future auth changes less reliable, even though the active runtime risk is lower than the shared-token findings.

### Web SPA

- F-007 gives the SPA an invalid assumption about inbox summary shape.
- F-008 makes offline attachment UX unsafe because queued captures are not actually replayable with their files.
- F-011 shows an API gap that forced the SPA into mutating reads.

### Chrome extension

- F-002 is the extension's main problem: shipped bearer secret plus sync storage plus broad host permissions.
- F-003 also affects the extension because its multipart path serializes structured fields in a way the server does not interpret correctly.

### Infra/deployment

- F-009 is the clearest infra mismatch: nginx rejects uploads that the server and clients think are valid.
- The biggest remaining infra unknown is timezone correctness in `infra/config.yaml`, which is called out below as a follow-up question rather than a confirmed defect.

### Productization/UX

- F-003, F-007, F-008, F-009, and F-010 are all user-trust problems because the system can appear to accept an action while silently degrading the result.
- F-012 shows that product surface simplification has not been completed after auth model changes.

### Maintainability/technical debt

- F-005 is a schema-governance problem more than a one-line bug.
- F-011 is a small but important example of missing clean API boundaries.
- F-012 is mostly technical debt, but it directly harms future audit and remediation work.

### Observability/testing

- F-003, F-005, F-007, F-008, F-009, and F-010 all survived because current tests are mostly component-local. The missing layer is cross-client contract testing, reverse-proxy end-to-end testing, and device/runtime recovery testing.

## 5. Strong Areas / Well-Implemented Decisions

- Inkwell's local-first storage design is strong. `AppDatabase.kt`, `NoteDao.kt`, Room migrations, and FTS support show real attention to offline correctness and schema evolution.
- The Android pending-note replacement path is well designed. The transactional replacement logic prevents the classic `pending_*` duplication problem and is a good foundation for future sync work.
- Server attachment validation is materially better than the surrounding multipart contract. `attachment-handler.ts` enforces filename, MIME, and size rules, and `attachment-handler.test.ts` covers those guards directly.
- Server bootstrap and auth hardening are stronger than the client auth story. `server.ts`, `api-server.ts`, and their tests show environment validation, structured auth failure logging, cookie fallback support, and rate-limit coverage.
- Docker deployment posture is solid for a self-hosted system. `docker-compose.yml` uses `no-new-privileges`, `cap_drop: ALL`, local binds where appropriate, and a reasonable separation of services.
- The codebase does contain real tests in the places that matter most for backend invariants. The problem is not absence of tests overall; it is absence of contract and runtime integration coverage across clients.

## 6. Important Unknowns or Follow-up Questions

- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\config.yaml` still uses `America/Los_Angeles`. Is that intentionally the production timezone for calendar and list timestamp semantics, or stale config that will skew due/schedule behavior?
- Once nginx upload limits are raised, does `capture-server.ts` full-body multipart buffering create unacceptable memory pressure under concurrent uploads? The current 1 MB proxy limit partially masks that question.
- How often does the note cold-start indexing gap surface in the deployed processor lifecycle? It needs restart/reconciliation testing against a populated vault, not just static review.
- What is the actual conflict frequency for list edits and capture updates across Android, web SPA, and extension usage? The code clearly lacks a merge model, but runtime prioritization should be guided by observed contention.
- Does proactive bearer sending on Android ever cross an unintended redirect or proxy boundary in real deployment, or is the current `baseUrl` usage sufficient in practice? Static review did not find a current direct flaw, but runtime network traces would verify the assumption.

## 7. Recommended Research Themes for Next Stage

### 7.1 Sync and conflict resolution models

- Why it matters here: Pending-note replacement is good locally, but cross-client mutation still depends on implicit last-write-wins and registry timing.
- Study: Offline-first task/note systems, collaborative list products, and systems that reconcile local queues with server truth.
- Design questions: What conflict model fits markdown-backed content; where should authoritative versioning live; when should user-visible conflicts be surfaced instead of auto-merged?

### 7.2 Professional capture workflow patterns

- Why it matters here: Capture is the product center, but note/task/list/schedule/attachment paths are diverging by client and payload type.
- Study: High-friction and low-friction capture products such as note apps, task inbox systems, browser capture tools, and mobile quick-capture flows.
- Design questions: Which capture modes should remain first-class; how should attachments, links, share intents, and list capture converge into one stable workflow model?

### 7.3 Schema and API contract enforcement

- Why it matters here: The largest verified failures were contract drift, not isolated algorithm bugs.
- Study: OpenAPI- or schema-first systems with multiple clients, generated DTO pipelines, and contract-testing toolchains.
- Design questions: Where should the canonical schema live; how should multipart metadata be represented; which endpoints need separate summary versus detail contracts?

### 7.4 Auth and session architecture modernization

- Why it matters here: Shared bearer credentials in the APK and extension are the main product-grade blockers.
- Study: Self-hosted SaaS auth patterns, device enrollment flows, mobile token refresh architectures, and extension-safe auth models.
- Design questions: Should Nexus move to per-user sessions, per-device tokens, or enrollment codes; how should revocation work; what is the smallest auth model that removes shipped secrets without overcomplicating the stack?

### 7.5 Self-hosted to product-grade evolution

- Why it matters here: The system currently mixes personal-system shortcuts with production-style infrastructure and tests.
- Study: Products that began as self-hosted/personal automation systems and later formalized auth, contracts, admin surfaces, and operational policies.
- Design questions: Which current conveniences are acceptable only in personal mode; what should be gated behind a "single-user self-hosted" profile versus the default architecture?

### 7.6 Product simplification and feature packaging

- Why it matters here: The platform has accumulated Android, SPA, extension, notes, tasks, lists, calendars, widgets, FCM, and dashboard surfaces faster than the contract layer has stabilized.
- Study: Products that deliberately narrowed their core workflow before scaling feature surface.
- Design questions: Which surfaces are essential; which should be downgraded, removed, or hidden until the core capture-sync path is stable; how should notes/tasks/lists be packaged conceptually?

### 7.7 Observability and diagnostics for multi-client systems

- Why it matters here: Several failures are currently silent or user-visible only after data loss, especially around capture degradation and offline replay.
- Study: Multi-client sync systems with good event tracing, audit logs, device registration telemetry, and contract-failure observability.
- Design questions: What should be logged or surfaced when capture metadata is dropped, uploads are rejected at the proxy, or device registration fails repeatedly?

### 7.8 Extension security patterns

- Why it matters here: The extension is the weakest auth surface and has browser-specific risks that should not be handled like a normal web client.
- Study: MV3 extension hardening guides, browser-vendor recommendations, and products that scope permissions and secrets tightly.
- Design questions: Can the extension avoid long-lived secrets entirely; how should trusted origins be pinned; is a browser extension still the right vehicle for capture versus a lighter share/bookmark workflow?

### 7.9 Offline-first mobile and web design patterns

- Why it matters here: Android has a relatively strong local-first model, while the web queue is only partially offline-capable and unsafe for attachments.
- Study: Offline-capable note/task products, PWA blob storage strategies, and queue reconciliation systems for mobile plus web clients.
- Design questions: Which data types must work offline; how should attachments be staged and surfaced; where should unsupported offline actions fail fast instead of pretending to queue?

### 7.10 Derived-state indexing and background reliability

- Why it matters here: Registry completeness depends on scan/watch correctness, and push registration depends on lifecycle timing.
- Study: File-backed indexers, derived registry rebuild strategies, background worker reliability models, and push enrollment systems.
- Design questions: How should the registry be rebuilt safely on startup; what invariants should scanners and watchers share; when should the app self-heal device registration and stale derived state?

## 8. Recommended Remediation Roadmap

### Immediate

- Replace or contain shipped shared credentials. F-001 and F-002 are the clearest professionalization blockers.
- Fix the multipart capture contract before adding more capture features. F-003 is currently corrupting semantics by payload type.
- Fix attachment UID sequencing. F-004 is a concrete integrity bug with a clear boundary in `capture-server.ts`.
- Align upload limits end-to-end. F-009 should be resolved before attachment features are treated as stable.
- Settle the schedule schema. F-005 will keep leaking complexity into every client until there is one authoritative representation.

### Near-term

- Make initial scan and watcher coverage consistent so note/list registry state is complete after restart. F-006.
- Repair SPA summary/detail contracts so tasks and notes render against intentional DTOs. F-007.
- Either support offline attachments properly or disable them explicitly in the web UI. F-008.
- Add deterministic startup retry for device registration. F-010.
- Add a real list detail read path and stop mutating files on read. F-011.

### Strategic / architectural

- Introduce a contract-first API layer shared by Android, SPA, extension, and server.
- Separate personal-mode shortcuts from product-mode defaults, especially around auth bootstrap and extension behavior.
- Refactor `capture-server.ts` into explicit subflows for JSON capture, multipart capture, list capture, note capture, and attachment commit sequencing.
- Revisit registry architecture so cold-start rebuild, steady-state watcher updates, and API read models share one invariant set.

### Productization improvements

- Remove or formally deprecate stale auth paths and update `PROJECT.md`, audit context files, and docs accordingly.
- Add user-visible diagnostics for proxy upload rejection, offline attachment limitations, and failed device registration.
- Add cross-client contract tests and deployment-path integration tests before shipping more client surface area.
- Simplify feature packaging around the most reliable workflow first: capture, sync, view, and lightweight follow-up.

## 9. Appendix: Pass-1 Resolution Table

| Pass 1 ID | Pass 2 outcome | Notes |
| --- | --- | --- |
| P1-001 | F-001 | Confirmed. Still open as a deliberate design trade-off, not a false positive. |
| P1-002 | F-002 | Confirmed. No meaningful mitigation found in current extension code. |
| P1-003 | F-003 | Confirmed and refined. `captureType` survives multipart in some paths, but list/schedule/persistent semantics still do not. |
| P1-004 | F-004 | Confirmed. Sequencing bug is real in current multipart attachment flow. |
| P1-005 | F-005 | Confirmed. Schedule schema remains inconsistent across capture, markdown, normalizer, and worker logic. |
| P1-006 | F-006 | Confirmed, but downgraded to Partially mitigated because watcher coverage repairs touched notes after startup. |
| P1-007 | F-007 | Confirmed. SPA expects richer summary fields than `/api/inbox` returns. |
| P1-008 | F-011 | Confirmed and merged with the broader concurrency/last-write-wins problem in the same list update path. |
| P1-009 | F-008 | Confirmed. Offline queue remains JSON-only and attachment-unaware. |
| P1-010 | F-010 | Confirmed. Startup path still does not retry normal device registration. |
| P1-011 | F-009 | Confirmed. Main capture nginx block is still limited to 1 MB. |
| P1-R01 | Resolved / no final finding | Current Android authenticated requests target configured base URLs; this remains a design caveat, not a verified open flaw. |
| P1-R02 | F-012 | Confirmed as stale/inconsistent auth surface, but downgraded to Low because active Android usage appears to rely on manual/shared-token auth instead. |
| P1-R03 | F-011 | Confirmed and folded into the list read/update finding rather than carried as a duplicate. |
| P1-R04 | Folded into F-003, F-009, and Section 6 | Full-body multipart buffering is real, but current impact is partly masked by the proxy limit and is better handled as an architectural follow-up than a separate top-level finding. |
| P1-R05 | Moved to Section 6 follow-up question | Still unresolved and requires runtime/owner confirmation rather than static overstatement. |
