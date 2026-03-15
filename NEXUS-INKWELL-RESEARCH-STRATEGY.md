# Nexus + Inkwell Research Strategy

**Date:** 2026-03-15
**Input:** Verified Pass 2 audit findings (12 total: 2 Critical, 7 High, 2 Medium, 1 Low)
**Purpose:** Research-backed design directions for remediation, architecture streamlining, and product evolution

---

# 1. Executive Synthesis

The Nexus+Inkwell ecosystem is a technically ambitious personal platform with strong internal module design but weak cross-boundary governance. The verified audit confirms the system works well within each component but fails at the seams: auth distribution, capture contract consistency, attachment integrity, and API shape alignment.

**Most important design directions:**

1. **Replace shipped credentials with device enrollment** — A one-time pairing code flow eliminates the APK-embedded token and extension-seeded secret without requiring a full identity provider. This is the single highest-impact change.

2. **Adopt a canonical capture schema enforced via Zod** — Define one `CaptureRequest` schema in TypeScript, generate JSON Schema from it, and use that schema to validate both JSON and multipart submissions server-side. Send multipart metadata as a single JSON part + file parts. Generate Kotlin data classes from the JSON Schema for Android contract alignment.

3. **Fix attachment commit sequencing with a two-phase model** — Stage uploads to a temp directory keyed by client-provided UUID, then move to the final UID directory only after the note is committed. This is a targeted fix, not a new architecture.

4. **Unify the scanner and watcher directory sets** — The cold-start rebuild must cover the same paths the watcher covers. This is a one-function fix with outsized impact on registry correctness.

5. **Separate summary and detail DTOs intentionally** — The inbox API should return a defined summary shape; detail views should use `/api/note/:uid`. Stop using one endpoint for both purposes.

**Biggest must-fix areas:** Auth credential distribution (F-001, F-002), capture contract lossy multipart (F-003), attachment UID sequencing (F-004), nginx upload limit (F-009).

**Most promising architecture opportunity:** A shared Zod schema package that generates JSON Schema, TypeScript types, and (via tooling) Kotlin DTOs. This solves F-003, F-005, F-007 simultaneously and prevents future contract drift.

**Highest-confidence recommendation:** Do NOT introduce a full identity provider, CRDT engine, or framework migration. The fixes needed are targeted: enrollment flow, schema package, attachment staging, scanner path alignment, and DTO separation. Each can be implemented independently.

---

# 2. Research-Aligned Restatement of the Platform Problem

Nexus+Inkwell is a **strong self-hosted personal platform** built by a solo developer that has accumulated real architectural depth: Room with FTS, WorkManager sync, Docker hardening, structured logging, 1700+ tests across both projects. It is not a toy.

The transition challenge is that **cross-client contract discipline has not kept pace with feature breadth.** The system has four capture clients (Android, SPA, Chrome extension, Email Commander), three sync surfaces (WorkManager, manual pull-to-refresh, FCM push), and a file-backed registry that serves as both source of truth and query index. Each of these was built to work, but they were not built to share one enforced contract.

The result is a platform where:
- Auth works but is distributed via embedded secrets rather than enrollment
- Capture works but loses metadata when attachments are involved
- Sync works but the registry can be incomplete after restart
- The API works but clients assume different response shapes
- Attachments work but can be stored under orphaned UIDs

None of these are catastrophic in solo use. All of them would be trust-breaking for any broader audience. The path forward is not a rewrite but a **contract-first discipline pass** that aligns what already exists.

---

# 3. Research Findings by Theme

## 3.1 Security and Auth Modernization

### Why it matters
F-001 (Critical) and F-002 (Critical) are the top professionalization blockers. The bearer token is baked into the APK, seeded into `chrome.storage.sync`, and used as the sole authentication mechanism. Any extracted APK or synced browser profile grants full API access with no revocation path.

### External patterns studied
- **Device enrollment codes** (Tailscale, WireGuard, MDM solutions): One-time pairing codes that bind a device to a server instance without distributing long-lived secrets.
- **OAuth for mobile apps** (RFC 8252, Curity best practices): PKCE-based flows for native apps, but requires an authorization server.
- **Token rotation** (OneUptime, Auth0 patterns): Refresh token rotation with short-lived access tokens.
- **Chrome extension auth** (Google Identity Platform docs, Firebase MV3 patterns): `chrome.identity.launchWebAuthFlow()` for OAuth, or server-issued session cookies via `chrome.cookies`.
- **Device-bound request signing** (Microsoft pattern): Cryptographic binding of requests to device keys.

### Strongest lessons
1. **Tailscale's enrollment model** is the best analogy: a self-hosted coordination server issues a one-time key, the device presents it once, and receives a device-specific token. No shared secret ships in the binary.
2. **Chrome extensions should never store long-lived bearer tokens in sync storage.** Google's own docs recommend `chrome.identity` for OAuth or server-issued short-lived sessions. `storage.sync` replicates secrets to every signed-in browser instance.
3. **Refresh token rotation** (access token: 15min, refresh token: rotated on use) is the industry standard for mobile apps, but it requires server-side token state. For a solo self-hosted system, this may be overkill initially.

### Recommended direction: Device Enrollment with Scoped Tokens

**Flow:**
1. Server generates a one-time enrollment code (6-digit or short alphanumeric, expires in 10 minutes)
2. User enters the code in Inkwell settings or Chrome extension options page
3. Server validates the code and issues a device-specific token (UUID + HMAC-signed JWT or opaque token)
4. Device stores the token in EncryptedSharedPreferences (Android) or `chrome.storage.local` (extension — NOT sync)
5. Server tracks active device tokens in a `devices` table with last-used timestamps
6. Revocation: server deletes the device token; next request gets 401; client prompts re-enrollment

**Why this fits:**
- Removes shipped secrets from APK and extension
- No external identity provider needed
- Per-device revocation without rotating the server master secret
- Works for solo use; scales to multi-user later by adding a user column
- Enrollment code can be displayed in the server's web admin UI or generated via CLI

**Alternative considered:** Full OAuth2 with PKCE. Rejected as over-engineering for a solo self-hosted system. Can be added later if multi-user support is needed.

**Implementation complexity:** Medium. Requires new `/api/enroll` endpoint, device token table, enrollment code generation UI, and client-side enrollment flow in Android + extension.

**Impact:** Resolves F-001, F-002, and most of F-012. High security improvement, moderate UX improvement (one-time setup vs. copy-pasting tokens).

---

## 3.2 Cross-Client API/Schema Contract Discipline

### Why it matters
F-003 (High), F-005 (High), and F-007 (High) all stem from the same root cause: no single enforced schema definition shared across clients and server.

### External patterns studied
- **OpenAPI-first with codegen** (Chili Piper contract testing, openapi-stack): Define schemas in OpenAPI, generate TypeScript types and client SDKs.
- **Zod as infrastructure** (Aditya Dewaskar, PkgPulse comparison): Single Zod schema validates server input, generates JSON Schema, drives TypeScript types, and can generate OpenAPI docs.
- **TypeBox** (comparison with Zod): Produces JSON Schema natively, faster at runtime, but smaller ecosystem.
- **Contract testing** (Alex O'Callaghan, Chili Piper): Type-level and runtime assertions that API responses match declared schemas.
- **io-ts-http** (@api-ts/openapi-generator): TypeScript-first API contracts that generate OpenAPI specs.

### Strongest lessons
1. **Zod is the pragmatic choice for this stack.** It has 20M+ weekly downloads, produces JSON Schema via `zod-to-json-schema`, drives TypeScript types directly, and validates at runtime. TypeBox is faster but Zod's ecosystem is larger and the project already uses TypeScript.
2. **The multipart metadata problem is solved by sending structured fields as a single JSON part.** Instead of flattening every field into form-data key-value pairs (where arrays and nested objects break), send a `metadata` part containing JSON and separate `file[]` parts for attachments. This is what Notion, Slack, and Discord APIs do.
3. **Contract testing between TS server and Kotlin client** is best done via JSON Schema as the intermediate format. Tools like `jsonschema2pojo` or `quicktype` generate Kotlin data classes from JSON Schema, keeping DTOs aligned without manual synchronization.

### Recommended direction: Zod Schema Package + JSON Metadata Part

**Architecture:**
```
packages/capture-schema/
  src/
    capture-request.ts    # Zod schema (canonical)
    inbox-summary.ts      # Summary DTO schema
    note-detail.ts        # Detail DTO schema
    index.ts              # Exports all schemas + JSON Schema generation
```

**Server uses:**
- Import Zod schemas directly for request validation
- Both JSON and multipart requests validate against the same schema
- Multipart requests read the `metadata` JSON part and validate it identically to a JSON body

**Client alignment:**
- Generate JSON Schema files at build time (`zod-to-json-schema`)
- Android: use `quicktype` or manual alignment to generate Kotlin `@Serializable` classes from JSON Schema
- Extension/SPA: import the JSON Schema for client-side pre-validation (optional, lightweight)

**Multipart format change:**
```
POST /api/capture
Content-Type: multipart/form-data; boundary=...

--boundary
Content-Disposition: form-data; name="metadata"
Content-Type: application/json

{"body":"...", "title":"...", "tags":["work"], "startTime":"09:00", ...}
--boundary
Content-Disposition: form-data; name="files"; filename="photo.jpg"
Content-Type: image/jpeg

<binary>
--boundary--
```

**Implementation complexity:** Medium. New `packages/capture-schema/` directory, refactor `capture-server.ts` multipart handler to read JSON metadata part, update Android `captureWithAttachments()` to send metadata as JSON part, update extension/SPA form submission.

**Impact:** Resolves F-003, F-005 (schedule fields included in canonical schema), and F-007 (separate summary/detail schemas). Prevents all future contract drift.

---

## 3.3 Offline-First Sync and Conflict Handling

### Why it matters
Inkwell's local-first design is already strong (Room, FTS, transactional pending-note replacement). The gap is not the local layer but the sync contract: last-write-wins with no version tracking, no conflict detection, and the web offline queue dropping attachments (F-008).

### External patterns studied
- **Notion offline mode** (Raymond Xu, Dec 2025): SQLite-backed persistent storage with offline trees tracking *why* each page is offline. CRDT-based conflict resolution for text. Push-based updates for keeping pages fresh.
- **Offline-first mobile sync patterns** (educba, Sujith Reddy): Outbox pattern, idempotency keys, vector clocks, three-way merge.
- **CRDT toolkits for TypeScript** (Codastra): Yjs, Automerge, cr-sqlite for conflict-free replicated data types.
- **Practical conflict resolution** (Cursa course): Field-level LWW, conflict copies, user-prompted merge.

### Strongest lessons
1. **Notion's approach is instructive but too heavy for this system.** They needed CRDTs because of collaborative real-time editing. Nexus is single-user, which makes conflict resolution dramatically simpler — the conflict is between devices, not users.
2. **The right model for single-user multi-device is version-stamped LWW with conflict copies.** Add a monotonic version counter to each note. On sync, if the server version is higher and the local version has pending changes, create a conflict copy rather than silently overwriting. This is what Obsidian Sync does.
3. **The web offline queue problem (F-008) is best solved by IndexedDB blob storage.** Store the complete FormData (including file blobs) in IndexedDB when offline, and replay the full multipart request when connectivity returns. The Smashing Magazine article on offline image uploads provides a clean reference implementation.

### Recommended direction: Version Counter + Conflict Copies

**Changes:**
1. Add `version` integer column to `NoteEntity` (Room) and `items` table (SQLite registry)
2. Server increments version on every write; includes version in API responses
3. Client sends version in PATCH requests; server rejects if version doesn't match (409 Conflict)
4. On 409, client creates a local conflict copy with a `conflict_of` foreign key
5. User resolves conflicts manually in the inbox (simple "keep mine" / "keep server" / "keep both" UI)

**For web offline queue (F-008):**
- Store `{metadata: JSON, files: Blob[]}` in IndexedDB
- On replay, reconstruct the multipart request from stored blobs
- Show a badge for "X captures pending upload" when offline

**Implementation complexity:** Low-Medium. Version column migration, server version check in PATCH handler, conflict copy creation in sync engine, basic conflict resolution UI.

**Impact:** Resolves F-008 and strengthens the sync model. Does NOT require CRDTs, event sourcing, or any distributed systems complexity.

---

## 3.4 Capture Architecture and Attachment Handling

### Why it matters
F-003 (lossy multipart), F-004 (UID mismatch), and F-009 (nginx limit) all center on the capture-to-storage pipeline. This is the product's core workflow.

### External patterns studied
- **Two-phase upload** (REST file upload patterns, OneUptime): Upload files first to get references, then create the resource linking those references.
- **Presigned URL pattern** (AWS S3, Uploadcare): Client gets a signed URL, uploads directly to storage, then notifies the server.
- **Content-addressed storage** (Git, IPFS model): Files addressed by hash, not by parent resource ID.
- **Staging directory pattern** (common in email/attachment systems): Files land in a temp area, then move atomically to final location.

### Strongest lessons
1. **The staging directory pattern is the right fit for F-004.** It's the simplest model that prevents orphaned attachments. Files upload to `/vault/Attachments/.staging/{client_uuid}/`, and after the note UID is finalized, the directory is renamed to `/vault/Attachments/{final_uid}/`. If the note creation fails, a cleanup job sweeps staging directories older than 1 hour.
2. **Presigned URLs are overkill for this system.** That pattern is for distributed storage (S3). Nexus stores files on local disk behind a single Node.js process.
3. **nginx `client_max_body_size` must match the server's intended limit.** This is a one-line fix (`client_max_body_size 50m;`) but should be committed to the `infra/nginx.conf` in the repo, not just patched on the running container.

### Recommended direction: Staging Directory + Committed nginx Config

**Attachment flow:**
1. Client sends multipart with `uuid` field (client-generated)
2. Server saves attachments to `/vault/Attachments/.staging/{uuid}/`
3. Server processes capture request, computes final note UID
4. Server renames `.staging/{uuid}/` to `/vault/Attachments/{final_uid}/`
5. If rename fails (directory already exists), merge files into existing directory
6. Cleanup cron: delete `.staging/` directories older than 1 hour

**nginx fix:**
- Update `infra/nginx.conf`: `client_max_body_size 50m;` for the capture host
- Commit to repo so it persists across `docker compose` rebuilds
- Add a comment linking the limit to `attachment-handler.ts` constants

**Implementation complexity:** Low. Staging directory rename in `capture-server.ts`, nginx config change in repo, optional cleanup cron.

**Impact:** Resolves F-004 and F-009. Combined with the JSON metadata part (3.2), fully resolves the capture pipeline.

---

## 3.5 Unified Note/Task/List/Calendar Data Model

### Why it matters
F-005 (schedule metadata not persisted) reveals that the system accepts richer data than it stores. The item model is implicit and inconsistent across capture, frontmatter, registry, and API.

### External patterns studied
- **TaskNotes Obsidian plugin** (callumalpass): Each task is a markdown file with YAML frontmatter: `status`, `priority`, `due`, `start`, `end`, `calendar`, `tags`, `type`. Clean separation of metadata from content.
- **Notion block model** (HowWorks analysis): Everything is a block with a type property. Flexible but complex.
- **Anytype object model**: Every item is a typed object with relations. More structured than markdown but requires a custom editor.
- **Todoist data model**: Simple task with `content`, `due`, `priority`, `labels`, `project_id`. No start time, no calendar target.

### Strongest lessons
1. **The markdown+frontmatter model is correct for this system.** It preserves Obsidian interoperability, which is the platform's differentiation. The problem is not the model — it's that frontmatter fields are not governed by a schema.
2. **A canonical frontmatter schema should include:** `title`, `status`, `kind`, `tags[]`, `due_date`, `start_time`, `end_time`, `calendar`, `priority`, `source`, `created`, `updated`, `attachments[]`, `list_name`, `items[]`, `persistent`. These are the union of fields currently accepted by capture.
3. **The normalizer/scanner should enforce the canonical schema** during ingestion, not silently drop fields. If a field is present in frontmatter but not in the registry schema, it should at minimum be preserved in frontmatter even if the registry column doesn't exist yet.

### Recommended direction: Canonical Frontmatter Schema + Registry Alignment

**Define once (in the Zod schema package):**
```typescript
const FrontmatterSchema = z.object({
  title: z.string().optional(),
  status: z.enum(['open', 'done', 'dropped', 'archived']).default('open'),
  kind: z.enum(['one_shot', 'complex', 'brainstorming', 'note', 'list_item']).default('one_shot'),
  tags: z.array(z.string()).default([]),
  due_date: z.string().optional(),  // ISO date
  start_time: z.string().optional(), // HH:MM
  end_time: z.string().optional(),   // HH:MM
  calendar: z.string().optional(),
  priority: z.string().optional(),
  source: z.string().default('web'),
  created: z.string(),  // ISO datetime
  updated: z.string(),  // ISO datetime
  attachments: z.array(z.string()).default([]),
  list_name: z.string().optional(),
  persistent: z.boolean().optional(),
})
```

**Registry alignment:**
- Add `start_time`, `end_time`, `calendar` columns to the registry `items` table
- Normalizer maps these from frontmatter into registry columns
- Worker reads from registry columns, not from re-parsed frontmatter
- API returns them in the detail DTO

**Implementation complexity:** Medium. Schema definition, registry migration, normalizer update, worker update, API response update.

**Impact:** Resolves F-005. Combined with the Zod schema package, ensures every field round-trips from capture to markdown to registry to API to client.

---

## 3.6 Product Surface Simplification and Professionalization

### Why it matters
The platform has accumulated Android, SPA, Chrome extension, Email Commander, widgets, FCM push, GCal sync, and Syncthing monitoring. Several surfaces assume different API contracts (F-007), and some are partially broken (F-008 offline queue, F-011 list reads).

### External patterns studied
- **Readwise Reader**: Started as a read-later tool, expanded carefully. Core flow is clip → read → highlight → export. Extensions are minimal (just capture + save).
- **Raindrop.io**: Bookmark/capture manager. Extension is lightweight (save URL + tags). Mobile app is read-focused. No editing complexity in the extension.
- **Todoist**: Strict product pillars — inbox, today, upcoming. Extension is capture-only (quick add). No full task management in the extension.
- **Things**: No web app at all. Mac + iOS only. Extreme focus on native quality over platform breadth.

### Strongest lessons
1. **The best capture products keep extensions minimal.** Raindrop and Todoist extensions do ONE thing: capture. No inbox management, no editing, no sync status. This dramatically reduces the auth surface and contract surface of the extension.
2. **Products that evolve from personal tools succeed by narrowing before expanding.** Readwise Reader deliberately scoped their initial launch to read-later + highlights, deferring note-taking features that would compete with Obsidian/Notion.
3. **Email Commander is a power-user feature that should not block core stability.** It's clever but it adds another capture surface with its own contract assumptions. It should be explicitly flagged as "advanced/experimental" rather than a first-class capture path.

### Recommended direction: Three-Tier Product Surface

**Tier 1 — Core (must be rock-solid):**
- Capture (Android, SPA, Extension) → one canonical contract
- Inbox view (Android, SPA)
- Sync (WorkManager, manual refresh)
- Note/task detail view + status updates

**Tier 2 — Extended (solid but secondary):**
- GCal sync
- Widgets
- FCM push notifications
- Attachment upload
- Lists

**Tier 3 — Experimental (explicitly flagged):**
- Email Commander
- Syncthing monitoring UI
- Export (JSON/CSV)
- System health dashboard

**Extension simplification:**
- Remove inbox/task management from extension scope
- Extension does ONE thing: capture (page title, URL, selected text, optional tags)
- No attachment upload from extension (simplifies contract surface)
- Auth via device enrollment (see 3.1), not stored bearer token

**Impact:** Reduces the attack surface, contract surface, and maintenance surface. Focuses energy on making Tier 1 excellent.

---

## 3.7 Observability, Diagnostics, and Operational Trust

### Why it matters
Several audit findings (F-003, F-008, F-009, F-010) are silent failures. The user doesn't know their attachment was dropped, their upload was rejected by nginx, or their device registration failed.

### Recommended direction: Client-Side Sync Status + Server Audit Log

**Client-side (Inkwell):**
- Add a "Sync Status" section to System Health screen
- Show: last successful sync time, pending upload count, last upload error (if any), device registration status
- On 413 from server: show explicit "File too large" error instead of generic retry

**Server-side:**
- Existing structured logging is good; add a `capture_audit` log for every capture attempt: client type, payload type (JSON/multipart), fields present, fields dropped, attachment count, result
- Log device registration attempts and failures to the same audit trail
- Add `/api/admin/audit` endpoint (authenticated) to view recent capture/sync events

**Implementation complexity:** Low. Client-side is UI work. Server-side is log statements + optional admin endpoint.

---

## 3.8 File-Backed Registry/Index Reliability

### Why it matters
F-006 (High) shows that the registry is incomplete after cold start because the initial scan covers different directories than the watcher.

### External patterns studied
- **Palantir's index refresh semantics**: Defensive rebuild strategies that distinguish "known complete" from "best effort" states.
- **Notion's offline page tracking**: Explicit tracking of which records are downloaded and why, with reconciliation on reconnect.

### Recommended direction: Unified Directory Set + Startup Reconciliation

**Fix:**
1. Extract the directory list into a shared constant used by both `scanner.ts` initial scan and `index.ts` watcher setup
2. On startup, run a full scan of all watched directories before accepting API requests
3. After the full scan, compare registry UIDs against files on disk; mark registry entries with no file as "orphan candidates" (existing 7-day grace period applies)
4. Add a health check flag: `registryReady: boolean` that is `false` until the initial scan completes

**Implementation complexity:** Low. One shared constant, one startup scan call, one health flag.

**Impact:** Resolves F-006 completely.

---

# 4. Comparison Matrix

| Attribute | Nexus/Inkwell (current) | Todoist | Notion | Obsidian+Sync | Raindrop.io | Readwise Reader |
|-----------|------------------------|---------|--------|---------------|-------------|-----------------|
| **Auth model** | Shared bearer token in APK + extension | OAuth2 + API tokens | OAuth2 + workspace sessions | Obsidian account + E2E encrypted sync | OAuth2 | OAuth2 |
| **Capture flow** | 4 clients, inconsistent multipart | Quick add (text only) | Block creation API | File creation (local) | URL + metadata save | URL + full page archive |
| **Offline behavior** | Android: strong local-first; Web: partial (no attachments) | Full offline on mobile | Full offline (CRDT-based, Dec 2025) | Fully local, sync is optional | Read-only offline | Full offline reading |
| **Sync model** | Timestamp LWW, no versioning | Server-authoritative, ordered ops | Version vectors + CRDT | Encrypted patch-based sync | Server-authoritative | Server-authoritative |
| **Attachment handling** | Multipart upload, staging bug | No file attachments | Block-level file uploads | Vault-local files | Screenshot + file save | Page archive (images inline) |
| **Extension model** | Full capture + stored bearer token | Quick add only | Web clipper (save page as blocks) | No extension | Minimal save button | Highlight + save |
| **Data model** | Markdown + frontmatter + SQLite registry | JSON tasks with labels/projects | Typed blocks in PostgreSQL | Markdown files + optional DB plugins | Bookmarks + tags + collections | Documents + highlights + notes |
| **Onboarding** | Copy-paste bearer token | Email/password + OAuth | Email/password + OAuth + workspace invite | Account creation + device sync setup | OAuth | OAuth |
| **Product scope** | Capture + inbox + tasks + notes + lists + calendar + email + widgets + extension | Tasks + projects + filters + labels | Everything (docs, tasks, databases, wikis) | Notes + plugins (community scope) | Bookmarks + collections + tags | Read-later + highlights + notes |

### Key comparisons in prose

**Todoist** is the best model for Inkwell's capture extension: it does exactly one thing (quick add) with minimal permissions. Inkwell should adopt this pattern.

**Obsidian Sync** is the closest architectural peer: file-backed with a sync layer. Their conflict model (conflict copies with manual resolution) is directly applicable and already familiar to Inkwell's user base.

**Notion's offline work** shows that even a server-first product invested heavily in making local storage the primary read path. Their `offline_action` table tracking *why* each page is offline is a sophisticated pattern, but overkill for Inkwell's simpler model.

**Raindrop.io** demonstrates that a capture-focused product can feel polished with a narrow scope: save, organize, retrieve. No editing, no collaboration, no sync complexity.

---

# 5. Recommendations Mapped to Verified Findings

| Finding(s) | Research-Backed Direction | Alternative Considered | Why This Fits |
|-------------|-------------------------|----------------------|---------------|
| **F-001, F-002** (Critical) | Device enrollment with scoped tokens | Full OAuth2 with PKCE | Enrollment codes are lower complexity, no external IdP needed, sufficient for solo/small-team use |
| **F-003, F-005** | Zod schema package + JSON metadata multipart part | OpenAPI-first with codegen | Zod is more natural for this TS codebase; JSON metadata part solves the multipart lossy problem directly |
| **F-004** | Staging directory renamed to final UID after commit | Content-addressed storage (hash-based) | Staging+rename is simplest; content-addressing adds complexity without clear benefit for this use case |
| **F-006** | Shared directory constant + startup full scan | Periodic full reconciliation cron | Startup scan is simpler and covers the actual failure mode (cold start); cron adds ongoing overhead |
| **F-007** | Separate summary/detail Zod schemas | Enrich inbox summary to include all fields | Separation is cleaner and avoids bloating the list endpoint; detail endpoint already exists |
| **F-008** | IndexedDB blob storage for offline queue | Disable attachment queuing entirely | IndexedDB blob storage is well-supported in modern browsers; disabling is simpler but worse UX |
| **F-009** | Commit `client_max_body_size 50m` to `infra/nginx.conf` | Per-location override for `/api/capture` only | Global 50m is acceptable for a personal server; per-location is more precise but adds config complexity |
| **F-010** | Idempotent startup registration when server+token are configured | Periodic retry timer | Startup check is simpler and covers the main failure mode (server unavailable at first boot) |
| **F-011** | Add GET `/api/list/:uid` + conditional PATCH with version check | ETag-based concurrency | Version counter is simpler and aligns with the broader versioning recommendation |
| **F-012** | Remove stale `exchangeGoogleToken()` from Android; update docs | Restore and test the Google auth flow | Removing dead code is safer than reviving an untested path; can revisit if multi-user auth is needed |

---

# 6. Architecture Streamlining Recommendations

## Immediate corrections (days)

1. **Commit nginx fix** — `client_max_body_size 50m;` in `infra/nginx.conf` (F-009)
2. **Align scanner directories** — Share the directory list between scanner and watcher (F-006)
3. **Remove stale Google auth code** — Delete `exchangeGoogleToken()` from Android, update PROJECT.md (F-012)
4. **Add startup device registration retry** — Call `registerWithServer()` in `CaptureApp.onCreate()` when server/token/FCM token are present (F-010)
5. **Add GET `/api/list/:uid`** — Read-only endpoint for list detail; stop mutating on read (F-011)

## Near-term architecture improvements (weeks)

6. **Create `packages/capture-schema/`** — Zod schemas for `CaptureRequest`, `InboxSummary`, `NoteDetail`, `FrontmatterSchema`
7. **Refactor multipart capture** — JSON metadata part + file parts; validate against Zod schema
8. **Implement attachment staging** — `.staging/{uuid}/` → rename to `/{final_uid}/` after commit
9. **Add version column** — Registry + Room migration; server increments on write; PATCH requires version match
10. **Device enrollment flow** — `/api/enroll` endpoint, enrollment code generation, Android/extension enrollment UI

## Strategic product evolution (months)

11. **Registry schema expansion** — Add `start_time`, `end_time`, `calendar` columns; normalizer populates from frontmatter
12. **Web offline attachment queue** — IndexedDB blob storage + multipart replay
13. **Cross-client contract tests** — CI job that validates Android DTOs, SPA fetch calls, and extension requests against Zod schemas
14. **Extension scope reduction** — Capture-only extension with device enrollment auth
15. **Sync diagnostics UI** — Android system health shows sync status, pending count, last error, device registration state

---

# 7. Productization Opportunities

## Must-fix trust issues
- Silent attachment metadata loss on multipart capture → user thinks capture succeeded fully
- nginx rejecting uploads the UI says are supported → confusing error at wrong layer
- Offline queue pretending to save attachments → data loss on replay
- No way to know if device registration succeeded → push notifications silently absent

## UX/onboarding polish
- **Enrollment code flow** replaces copy-paste token entry (friendlier first-run experience)
- **Capture confirmation** should show what was actually saved (title, tags, attachment count) — not just "Captured"
- **Widget design** (already improved this session) should show real-time sync status
- **Extension** should be installable from Chrome Web Store with one-click enrollment

## Feature packaging improvements
- **Rebrand "System Health"** to **"Sync Status"** — more meaningful to users
- **Group settings** into "Connection" (server, auth, devices) and "Preferences" (haptics, biometric, sync interval)
- **Inbox badge** should reflect unsynced count, not just total count

## Differentiated feature ideas worth exploring (after foundation is stable)
- **Smart capture routing**: Auto-detect if text looks like a task, note, or list based on content
- **Capture templates**: Pre-filled tag/calendar/kind combos for common workflows (e.g., "Work meeting note")
- **Cross-device clipboard**: Share intent from any device → appears in inbox on all devices
- **Weekly digest**: Email summary of captured items, completed tasks, upcoming calendar items

## Ideas to defer
- Multi-user support (requires full auth redesign beyond enrollment)
- Real-time collaborative editing (requires CRDTs, fundamentally different architecture)
- AI-powered capture enrichment (interesting but not a foundation issue)
- Custom Obsidian plugin (Syncthing already handles vault sync)

---

# 8. Prioritized Roadmap

## Phase 1: Trust and Correctness (1-2 weeks)

**Goals:** Eliminate silent failures and shipped secrets.

**Workstreams:**
1. Commit nginx 50m fix to repo + redeploy
2. Align scanner/watcher directory sets
3. Remove stale Google auth code from Android
4. Add startup device registration retry
5. Add GET list detail endpoint
6. Implement device enrollment flow (server + Android + extension)

**Expected benefits:** No more shipped secrets, no more silent upload rejections, complete registry on cold start, reliable push registration.

**Risks:** Enrollment flow is the largest item; scope-control it to one-time code entry, not a full session management system.

## Phase 2: Contract and Architecture Alignment (2-4 weeks)

**Goals:** One enforced schema across all clients and payload types.

**Workstreams:**
1. Create Zod schema package with canonical capture, summary, and detail schemas
2. Refactor multipart capture to JSON metadata part + file parts
3. Implement attachment staging directory
4. Add version column to registry + Room; version-checked PATCH
5. Align frontmatter schema with registry columns (start_time, end_time, calendar)
6. Separate inbox summary DTO from note detail DTO

**Expected benefits:** Capture is lossless regardless of payload type or client. API contracts are typed and testable. Attachments never orphaned.

**Risks:** Multipart format change requires coordinated update across Android, SPA, and extension. Stage the rollout: server accepts both old and new format during transition.

## Phase 3: Product Polish and Selective Expansion (4-8 weeks)

**Goals:** The system feels like a product, not a personal tool.

**Workstreams:**
1. Web offline attachment queue (IndexedDB blob storage)
2. Cross-client contract tests in CI
3. Extension scope reduction to capture-only
4. Sync diagnostics UI in Android + web
5. Capture confirmation with full metadata display
6. Onboarding improvements (enrollment code, first-run guidance)

**Expected benefits:** Users can trust every capture surface. New clients can be added without contract drift. The product is ready for beta users beyond the developer.

**What should wait:** Multi-user auth, AI enrichment, collaborative editing, Obsidian plugin, public distribution.

---

# 9. Implementation Research Questions for Claude Code

These questions should drive the next execution prompts after this research phase:

1. **Enrollment flow implementation:** What is the minimal server-side schema for device tokens? How should enrollment codes be generated and displayed? What happens to existing `CAPTURE_AUTH_TOKEN` during migration — dual-auth period?

2. **Zod schema package structure:** Should it be a standalone npm package or a directory within the monorepo? How does `zod-to-json-schema` handle the union types needed for capture (task vs note vs list)? What's the build step to keep Kotlin DTOs in sync?

3. **Multipart metadata part:** Does Ktor's `submitFormWithBinaryData` support a JSON-typed form part natively, or does it need to be sent as a raw `ByteArray` with content-type headers? How should the SPA `FormData` API send a JSON part?

4. **Attachment staging directory:** Should staging use the client UUID or a server-generated UUID? How should the rename handle the case where the client UUID collides with an existing staging directory from a retry?

5. **Version column migration:** What should the initial version be for existing notes? Should Room and SQLite registry use the same version counter, or is the server version authoritative and the client tracks it separately?

6. **Scanner/watcher alignment:** What is the current directory list in `scanner.ts` vs `index.ts`? Is it a simple constant extraction or are there config-dependent paths that need to be resolved at startup?

7. **Extension permission narrowing:** Can `host_permissions` be reduced to just `https://tyler-capture.duckdns.org/*` without breaking the capture flow? Does the extension need `http://*/*` and `https://*/*` for anything?

8. **Offline blob queue:** What is the IndexedDB storage limit on Chrome and Android WebView? Should there be a maximum queued attachment size? How should the queue handle cleanup of blobs after successful upload?

9. **Contract test CI:** What tool should run cross-client contract validation? Can `quicktype` reliably generate Kotlin `@Serializable` classes from JSON Schema with nullable fields and default values?

10. **Backward compatibility during migration:** How long should the server accept the old flat-field multipart format alongside the new JSON metadata part format? Should the server auto-detect the format based on the presence of a `metadata` part?

---

# 10. Appendix: Sources and Relevance

| Source | Why It Mattered |
|--------|----------------|
| **Notion: "How we made Notion available offline"** (Dec 2025) | Best-in-class offline architecture for a productivity app. The `offline_action` table and push-based sync model informed the versioning and conflict copy recommendations. |
| **OneUptime: "Token Rotation Strategies"** (Jan 2026) | Practical breakdown of refresh token rotation. Confirmed that full rotation is overkill for solo-user but validated the device-scoped token model. |
| **Curity: "OAuth for Mobile Apps Best Practices"** | Authoritative reference for RFC 8252 (PKCE). Confirmed that a lighter enrollment model is acceptable when the threat model is personal-use. |
| **Google: "Signing in users from a Chrome extension"** | Official docs on MV3 auth patterns. Confirmed that `chrome.identity` or server-side sessions are preferred over stored bearer tokens. |
| **PkgPulse: "Zod vs TypeBox in 2026"** | Direct comparison for the schema validation decision. Zod's ecosystem advantage and JSON Schema output capability made it the clear choice. |
| **Aditya Dewaskar: "Zod as Infrastructure"** (Feb 2026) | Demonstrated the "one schema, every layer" pattern that directly maps to the cross-client contract problem. |
| **Chili Piper: "Contract Tests with TypeScript and OpenAPI Codegen"** | Real-world case study of contract drift between frontend and backend. Validated the need for schema-first API design. |
| **Smashing Magazine: "Building An Offline-Friendly Image Upload System"** (Apr 2025) | Practical reference for IndexedDB blob storage in offline PWA upload queues. Directly applicable to F-008. |
| **educba: "Offline-First Mobile Sync Patterns"** (Mar 2026) | Comprehensive overview of outbox, idempotency, and conflict resolution patterns. Validated version-stamped LWW as appropriate for single-user. |
| **TaskNotes Obsidian plugin** | Closest existing implementation of the frontmatter-as-schema pattern for tasks. Validated that the markdown+YAML model can handle the full item lifecycle. |
| **Palantir: "Defensive Databases: Index Refresh Semantics"** (Sep 2025) | Enterprise-grade thinking about derived index consistency. The "known complete vs best effort" distinction informed the startup scan recommendation. |
| **Tweeks: "Fixing Intermittent Auth Failures in Chrome MV3"** (Feb 2026) | Real extension auth debugging case study. Confirmed that MV3 service worker lifecycle makes stored tokens unreliable without careful state management. |

---

*This document is the research output. Implementation planning should use it alongside `COMPREHENSIVE-AUDIT-FINDINGS.md` and the `CODEX-AUDIT-BRIEF.md` handoff file.*
