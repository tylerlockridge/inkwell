# Inkwell + Nexus Platform — Codex Audit Brief

**Created:** 2026-03-15
**Last Updated:** 2026-03-15 (Pass 2 verified findings)
**Purpose:** Shared context document for cross-model audits (Codex, Gemini, Claude). Provides the complete picture of the Inkwell Android app and the Nexus server platform it connects to.
**Audience:** Any LLM performing code review, security audit, architecture analysis, or feature planning.

---

## 0. Audit Handoff Status

This file is the Codex <-> Claude <-> GPT-5.4 Pro audit coordination document. It is intended to be the single authoritative handoff file for future audit, research, and remediation-planning work.

Use order:

1. `COMPREHENSIVE-AUDIT-FINDINGS.md` is the authoritative Pass 2 verification source.
2. This brief is the normalized handoff and navigation layer.
3. `COMPREHENSIVE-AUDIT-PASS1.md` is discovery history, not the final state.
4. `PROJECT.md` files provide session chronology and operational context.

When any older snapshot text below conflicts with Pass 2, trust the Pass 2 findings and the normalized statements in this brief.

### 0.1 Primary Audit Artifacts

- **Pass 2 verified findings:** `C:\Users\tyler\Documents\Claude Projects\Inkwell\COMPREHENSIVE-AUDIT-FINDINGS.md`
- **Pass 1 discovery report:** `C:\Users\tyler\Documents\Claude Projects\Inkwell\COMPREHENSIVE-AUDIT-PASS1.md`
- **Inkwell session context:** `C:\Users\tyler\Documents\Claude Projects\Inkwell\PROJECT.md`
- **Nexus session context:** `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\PROJECT.md`

### 0.2 Current Open Findings

| ID | Severity | Title | Status |
|---|---|---|---|
| F-001 | Critical | Android app still embeds and silently falls back to a shared bearer token | Design trade-off |
| F-002 | Critical | Chrome extension ships/stores bearer token and requests broad host access | Open |
| F-003 | High | Multipart capture is lossy and contract-inconsistent across Android, web, extension, and server | Open |
| F-004 | High | Attachment files can be written under a UID that does not match the final note UID | Open |
| F-005 | High | Schedule metadata is not persisted end-to-end | Open |
| F-006 | High | Processor cold-start indexing still ignores notes | Partially mitigated |
| F-007 | High | SPA task and note views expect fields that `/api/inbox` does not provide | Open |
| F-008 | High | Web offline queue still drops attachments | Open |
| F-009 | High | nginx still blocks attachment sizes the app and server are designed to support | Open |
| F-010 | Medium | Android device registration is not retried on normal startup after failure | Open |
| F-011 | Medium | List read/update path is mutating and effectively last-write-wins | Open |
| F-012 | Low | Google auth/client-context story is stale and internally inconsistent | Partially mitigated |

### 0.3 Do Not Re-Audit as Open

- `sendWithoutRequest { true }` was re-checked in Pass 2 and is **not** an open finding without new evidence.
- Production timezone in `infra/config.yaml` remains a follow-up question, not a confirmed defect.
- Older assumptions that nginx had already been raised to 50 MB were disproven by Pass 2; use the current `infra/nginx.conf`, not session-memory assumptions.
- Historical findings previously marked resolved should not be reintroduced as open unless new code evidence reopens them.

### 0.4 Latest Verified Status (Pass 2)

Final verified result: 12 findings total, split across 2 Critical, 7 High, 2 Medium, and 1 Low. The dominant risks are shared credentials, cross-client capture contract drift, and deployment/API inconsistencies that silently change behavior between clients or environments. The strongest areas remain Inkwell's local-first storage/sync foundation, server-side defensive intent, and Docker hardening.

### 0.5 High-Value Verification Files

- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\local\PreferencesManager.kt`
- `C:\Users\tyler\Documents\Claude Projects\Inkwell\app\src\main\kotlin\io\inkwell\data\remote\CaptureApiService.kt`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\capture-server.ts`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\api-server.ts`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\registry.ts`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\src\scanner.ts`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\manifest.json`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\capture-extension\background.js`
- `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\nginx.conf`

---

## 1. Platform Overview

Inkwell is the Android companion app for **Nexus**, a personal productivity platform that captures notes/tasks to an Obsidian vault via a REST API. The system spans:

| Component | Location | Tech | Purpose |
|-----------|----------|------|---------|
| **Inkwell** (Android) | `C:\Users\tyler\Documents\Claude Projects\Inkwell` | Kotlin, Jetpack Compose | Mobile capture + inbox management |
| **Nexus Server** | `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop` | TypeScript, Node.js | API server, vault processor, GCal sync, email commands |
| **Nexus Web SPA** | `Obsidian-Dashboard-Desktop/src/capture-web/public/` | Vanilla JS, Material Design 3 | Browser-based capture + task manager (PWA) |
| **Chrome Extension** | `Obsidian-Dashboard-Desktop/capture-extension/` | Manifest v3, Vanilla JS | Browser side panel capture |
| **Droplet** | `138.197.81.173` (DigitalOcean) | Docker Compose, Nginx, Let's Encrypt | Production hosting (9 containers) |

**Domain:** `tyler-capture.duckdns.org` (HTTPS, auto-renewed certs)
**Solo developer project** — Tyler Lockridge. No shared repo access concerns.

---

## 2. Inkwell Android App

### 2.1 Quick Facts

| Field | Value |
|-------|-------|
| Package | `io.inkwell` |
| Application ID | `io.inkwell` |
| Min SDK | 26 |
| Target SDK | 35 |
| Compile SDK | 35 |
| Version | 2.4.0 (versionCode 12) |
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| DI | Hilt |
| HTTP Client | Ktor (OkHttp engine) |
| Database | Room (SQLite) with FTS4 |
| Background Work | WorkManager (periodic + one-shot) |
| Auth | Bearer token in EncryptedSharedPreferences with shared `DEFAULT_AUTH_TOKEN` fallback baked into the APK; stale Google-auth code/docs remain |
| Deep Links | `inkwell://capture`, `inkwell://inbox`, `inkwell://note/{uid}`, `inkwell://system-health` |
| App Links | `https://tyler-capture.duckdns.org/app/*` (autoVerify) |
| Repo | https://github.com/tylerlockridge/inkwell |

### 2.2 Source Structure

```
Inkwell/
├── app/
│   ├── build.gradle.kts              # Build config, signing, R8/ProGuard
│   ├── proguard-rules.pro            # Keep rules for Ktor, Room, serialization
│   ├── google-services.json          # Firebase (FCM push notifications)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml   # Permissions, activities, receivers, providers
│   │   │   ├── kotlin/io/inkwell/
│   │   │   │   ├── CaptureApp.kt              # Application class (Hilt, WorkManager)
│   │   │   │   ├── MainActivity.kt            # Single activity, biometric lock, deep links
│   │   │   │   ├── MainViewModel.kt           # Lock state, startup sync trigger
│   │   │   │   ├── auth/
│   │   │   │   │   └── BiometricAuthManager.kt  # BIOMETRIC_STRONG prompt wrapper
│   │   │   │   ├── data/
│   │   │   │   │   ├── coach/CoachMarkManager.kt  # First-run tooltip state
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt         # Room DB (v4, FTS4)
│   │   │   │   │   │   ├── PreferencesManager.kt  # DataStore + EncryptedSharedPrefs
│   │   │   │   │   │   ├── dao/NoteDao.kt         # Room DAO (18 queries)
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── NoteEntity.kt       # Primary entity (20 columns)
│   │   │   │   │   │       └── NoteFtsEntity.kt    # FTS4 virtual table
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── CaptureApiService.kt    # All HTTP calls (13 endpoints)
│   │   │   │   │   │   └── dto/                    # 11 request/response DTOs
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── CaptureRepository.kt    # Capture logic (online/offline)
│   │   │   │   │       └── InboxRepository.kt      # Sync, status updates, search
│   │   │   │   ├── di/
│   │   │   │   │   ├── AppModule.kt          # Hilt bindings
│   │   │   │   │   ├── DatabaseModule.kt     # Room provider
│   │   │   │   │   └── NetworkModule.kt      # Ktor HTTP clients (auth + unauth)
│   │   │   │   ├── notifications/
│   │   │   │   │   ├── CaptureMessagingService.kt    # FCM handler (new_capture, sync_required, sync_error)
│   │   │   │   │   ├── NotificationActionReceiver.kt # Mark Done / Retry Sync from notification
│   │   │   │   │   ├── NotificationChannels.kt       # 4 channels
│   │   │   │   │   └── DeviceRegistrationManager.kt  # FCM token → server registration
│   │   │   │   ├── share/ShareIntentParser.kt  # ACTION_SEND handler
│   │   │   │   ├── sync/
│   │   │   │   │   ├── InboxSyncEngine.kt   # Shared sync logic (fetch→stale detect→concurrent detail→bulk upsert→tombstone sweep)
│   │   │   │   │   ├── SyncWorker.kt        # Periodic background sync (delegates to InboxSyncEngine)
│   │   │   │   │   ├── UploadWorker.kt      # Pending note upload (new + updates)
│   │   │   │   │   └── SyncScheduler.kt     # WorkManager scheduling (periodic + immediate)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/LockScreen.kt
│   │   │   │   │   ├── capture/             # CaptureScreen, CaptureToolbar, CaptureViewModel, CaptureUiState
│   │   │   │   │   ├── inbox/               # InboxScreen, InboxViewModel, InboxUiState
│   │   │   │   │   ├── detail/              # NoteDetailScreen, NoteDetailViewModel
│   │   │   │   │   ├── settings/            # SettingsScreen, SettingsViewModel, ConnectionCard, Components, Export
│   │   │   │   │   ├── health/              # SystemHealthScreen, SystemHealthViewModel
│   │   │   │   │   ├── navigation/          # CaptureNavHost, DeepLink, Screen
│   │   │   │   │   ├── components/          # AttachmentPicker, AttachmentPreview, CoachMark, MarkdownText
│   │   │   │   │   └── theme/               # CaptureTheme, Color, Type, Animation, Haptics
│   │   │   │   ├── util/MarkdownParser.kt
│   │   │   │   └── widget/
│   │   │   │       ├── QuickCaptureWidget.kt          # 4x1 Glance widget
│   │   │   │       ├── InboxCountWidget.kt            # 2x1 Glance widget
│   │   │   │       ├── QuickCaptureWidgetReceiver.kt
│   │   │   │       ├── InboxCountWidgetReceiver.kt
│   │   │   │       └── WidgetStateUpdater.kt
│   │   │   └── res/                          # Drawables, layouts, strings, XML configs
│   │   ├── test/kotlin/io/inkwell/           # 36 unit test files (294 tests)
│   │   └── androidTest/kotlin/io/inkwell/    # 4 instrumented test files (17 tests)
├── keys/release.keystore                      # Signing keystore (gitignored)
├── local.properties                           # Signing creds + auth token (gitignored)
├── documentation/                             # 12 feature docs
├── CLAUDE.md                                  # Project instructions for Claude Code
├── PROJECT.md                                 # Session history + quick resume
└── ARCHITECTURE.md                            # High-level architecture doc
```

### 2.3 Key Architectural Patterns

**Offline-First Capture:**
1. User creates note → `CaptureRepository.capture()` tries server first
2. If server unreachable → saves locally with `pending_` UID prefix + `pendingSync=true`
3. `UploadWorker` (periodic) picks up pending notes and uploads
4. On success → `replacePendingWithServer()` atomically swaps pending UID for server UID

**Sync Pipeline (InboxSyncEngine — shared by SyncWorker + InboxRepository):**
1. Fetch inbox listing from server (`GET /api/inbox?limit=200`)
2. Bulk DB lookup for local copies (`getAllByUids()`)
3. Filter to stale items (server timestamp newer than local, skip `pendingSync=true`)
4. Concurrent detail fetches (`async/awaitAll`)
5. Bulk upsert (`upsertAll()` — single transaction)
6. Tombstone sweep (`GET /api/inbox/deleted?since=<lastSync>`)
7. Record last sync timestamp

**Auth Token Lifecycle:**
- Stored in `EncryptedSharedPreferences` (AES-256-GCM)
- Exposed as `MutableStateFlow<String>` — reactive, all collectors see updates
- `BuildConfig.DEFAULT_AUTH_TOKEN` baked into APK as fallback
- On 401 from server → token cleared, auth-expired notification posted
- One-time migration from plaintext DataStore to encrypted prefs (runs at init)

**Attachment Upload:**
- Files streamed via Ktor `ChannelProvider` (no full in-memory load)
- `resolveFileSize()` via `OpenableColumns.SIZE` or `AssetFileDescriptor`
- Falls back to `readBytes()` if size unknown
- Client-side upload code supports larger files, but Pass 2 verified two open issues:
  - the multipart capture contract is not equivalent to the JSON capture contract (`F-003`)
  - `infra/nginx.conf` still limits the main capture host to `client_max_body_size 1m` (`F-009`)

### 2.4 Test Coverage

| Category | Files | Tests | Framework |
|----------|-------|-------|-----------|
| Unit tests | 36 | 294 | JUnit4 + MockK + Robolectric + Turbine |
| Instrumented tests | 4 | 17 | Hilt + Compose UI testing |
| **Total** | **40** | **311** | |

**Quality gates:** `./gradlew test` + `./gradlew lint` (both clean)

### 2.5 Audit History

Historical audit/fix log only. Do not use older "resolved" wording below as the current open/closed state; use Section 0 and `COMPREHENSIVE-AUDIT-FINDINGS.md`.

| Date | Provider(s) | Score | Key Findings |
|------|-------------|-------|-------------|
| 2026-02-26 | Codex (GPT-4o) | 6.3/10 | Zero instrumented tests, large files, allowBackup=true |
| 2026-02-28 | Codex + Gemini + Monica | 5.4/10 | CancellationException, N+1 queries, no lifecycle awareness |
| 2026-03-02 | Claude (deep composite) | — | 15 items (all resolved): pending orphan, JWT logging, biometric weak |
| 2026-03-06 | Codex 5.3 | 8.6/10 | Auth header bug (sendWithoutRequest), serialization catch |
| 2026-03-14 | Gemini 3.1 + Codex x2 | 7.0/10 | 12 findings (all resolved): attachment routing, timestamp comparison, N+1 |
| 2026-03-15 | Claude (deep composite) | — | 10 items (all resolved): InboxSyncEngine extraction, reactive auth, streaming uploads |
| 2026-03-15 | Codex Pass 1 | — | Discovery report: 11 confirmed findings, 5 follow-up verification items |
| 2026-03-15 | Codex Pass 2 | — | Final verified state: 12 findings (2 Critical, 7 High, 2 Medium, 1 Low) |

### 2.6 Current Validated State (Most Relevant to Remediation)

1. **Shared-token fallback remains active** — `BuildConfig.DEFAULT_AUTH_TOKEN` is still the Android fallback path (`F-001`).
2. **Android local-first sync foundation is strong** — `InboxSyncEngine`, transactional pending-note replacement, and Room/FTS remain strengths.
3. **Attachment-backed capture is still not contract-safe** — multipart upload loses metadata that JSON capture preserves (`F-003`).
4. **Device registration recovery remains incomplete** — normal app startup still does not retry server registration (`F-010`).
5. **Deep links are `capture`, `inbox`, `note/{uid}`, and `system-health`** — older `settings` / `health` references should be treated as stale.
6. **Google auth is not a clean active Android flow** — server behavior is cookie-oriented, while Android still contains stale token-exchange code/docs (`F-012`).

---

## 3. Nexus Server Platform

### 3.1 Quick Facts

| Field | Value |
|-------|-------|
| Location | `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop` |
| Language | TypeScript (ES2022, NodeNext) |
| Runtime | Node.js 22+ |
| Database | SQLite (better-sqlite3, WAL mode) |
| Web Framework | Vanilla Node.js HTTP (no Express/Fastify) |
| Testing | Vitest (PROJECT.md records 1,405 passing backend tests as of 2026-03-15) |
| Deployment | Docker Compose (9 containers) on 1GB DigitalOcean droplet |
| Domain | tyler-capture.duckdns.org |
| Repo | https://github.com/tylerlockridge/claude-projects (subdirectory) |

### 3.2 Services

| Service | Entry Point | Purpose |
|---------|-------------|---------|
| **Capture Web** | `src/server.ts` | HTTP server, REST API, static SPA, webhooks |
| **Processor** | `src/index.ts` | Vault file watcher → frontmatter normalization → registry |
| **GCal Worker** | `src/worker.ts` | Registry ↔ Google Calendar bidirectional sync (30s poll) |
| **Email Commander** | `src/email-commander.ts` | Gmail IMAP poll (60s) for `CMD:` emails → execute commands |
| **Nginx** | `infra/nginx.conf` | TLS termination, reverse proxy, security headers |
| **Syncthing** | Docker container | Desktop ↔ droplet vault file sync |
| **Backup** | Docker container | Daily SQLite snapshots (7 retained) |
| **Certbot** | Docker container | Let's Encrypt renewal (12h cycle) |

### 3.3 REST API Endpoints (consumed by Inkwell + Web SPA + Extension)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/api/capture` | Bearer | Create note (JSON or multipart with attachments) |
| GET | `/api/inbox` | Bearer | List items (paginated, filterable by status/since) |
| GET | `/api/inbox/deleted` | Bearer | Tombstone list for sync |
| GET | `/api/note/:uid` | Bearer | Full note detail (frontmatter + body) |
| PATCH | `/api/note/:uid` | Bearer | Update fields (status, title, body, tags) |
| GET | `/api/capture/defaults` | Bearer | Smart defaults (suggested tags, calendar) |
| GET | `/api/status` | Bearer | System health (processor, worker, syncthing) |
| POST | `/api/auth/google` | None | Google ID token verification; server sets `capture_auth` cookie and returns `{ success: true }` |
| POST | `/api/device/register` | Bearer | FCM device registration |
| DELETE | `/api/device/:id` | Bearer | Unregister device |
| GET | `/healthz` | None | Health check |
| GET | `/.well-known/assetlinks.json` | None | Android App Links verification |

Important Pass 2 caveats:

- `/api/capture` JSON and multipart are not currently contract-equivalent (`F-003`, `F-004`, `F-005`).
- `/api/inbox` is a summary feed and does not currently satisfy SPA task/note view assumptions (`F-007`).
- `/api/auth/google` is currently a browser-oriented cookie path; Android still has stale token-exchange expectations in older client code/docs (`F-012`).

### 3.4 Chrome Extension

**Location:** `capture-extension/`
**Manifest:** v3

| Component | File | Purpose |
|-----------|------|---------|
| Service Worker | `background.js` | Lifecycle, message routing, context menus |
| Side Panel | `sidepanel.html/js` | Capture form (auto-populates page title, URL, selection) |
| Options | `options.html/js` | Server URL + auth token config persisted via `chrome.storage.sync` |

Current audited posture:

- The extension seeds and reads a bearer token from `chrome.storage.sync` (`F-002`).
- `manifest.json` requests broader host access than the deployed capture flow needs (`F-002`).
- Attachment-backed extension capture also inherits the shared multipart contract problems (`F-003`).

### 3.5 Web SPA (PWA)

**Location:** `src/capture-web/public/`
**Stack:** Vanilla JS, Material Design 3, Service Worker

| Route | Page | Purpose |
|-------|------|---------|
| `/` | index.html | Capture form (primary) |
| `/tasks` | tasks.html | Task manager (list, add, mark done) |
| `/notes` | notes.html | Notes view |
| `/lists` | lists.html | Shared lists |
| `/ideas` | — | Ideas vault section |
| `/settings` | — | Theme, auth, PWA install |

**Features:** Offline queue for JSON captures (IndexedDB), dark mode, PWA installable, connection awareness banner

Current audited posture:

- Offline queue support is not attachment-safe; attachment-backed captures are not preserved for replay (`F-008`).
- `/tasks` and `/notes` currently expect fields not supplied by `/api/inbox` summary responses (`F-007`).
- `/lists` currently uses a mutating PATCH workaround to read list contents because there is no dedicated GET detail route (`F-011`).

### 3.6 Docker Architecture (droplet)

```
                    ┌─────────────────────────┐
                    │   nginx (80/443)         │
                    │   TLS + reverse proxy    │
                    └──────────┬──────────────┘
                               │
                    ┌──────────┴──────────────┐
                    │   capture-web (:3000)    │
                    │   API + SPA + webhooks   │
                    └──────────┬──────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
    ┌─────────┴───┐  ┌────────┴─────┐  ┌───────┴────────┐
    │  processor   │  │ gcal-worker  │  │ email-commander │
    │  vault watch │  │ GCal sync    │  │ Gmail IMAP      │
    └──────┬──────┘  └──────┬───────┘  └────────────────┘
           │                │
    ┌──────┴──────┐  ┌──────┴───────┐
    │  syncthing   │  │  registry.db │
    │  file sync   │  │  (SQLite)    │
    └─────────────┘  └──────────────┘
```

### 3.7 Security Model

- **Auth:** Shared bearer token model is still active in Android + extension; server also supports cookie auth fallback for browser flows; Android Google token-return path is stale/inconsistent
- **Rate limiting:** 600 req/min API, 30 req/min capture, 60 req/min webhook, 10 auth failures/5min
- **Docker:** `cap_drop: ALL`, `no-new-privileges: true`, 127.0.0.1 binding (except nginx)
- **TLS:** TLSv1.2+, HSTS, X-Frame-Options: DENY, X-Content-Type-Options: nosniff
- **SQLite:** WAL mode, FK enforcement, path traversal guards
- **Secrets:** Server-side secrets live in `.env` on the droplet, but the capture bearer token is currently distributed to Android and the Chrome extension (`F-001`, `F-002`), so it should not be described as a server-only secret in practice

---

## 4. Cross-Platform Data Flow

```
┌──────────────┐    POST /api/capture     ┌──────────────┐
│  Inkwell App ├─────────────────────────►│ Capture Web  │
│  (Android)   │◄─────────────────────────┤ (API Server) │
│              │    GET /api/inbox         │              │
└──────┬───────┘                          └──────┬───────┘
       │                                         │
       │  FCM push                    Write to   │
       │  (new_capture,              vault disk   │
       │   sync_required)                        │
       │                                         ▼
       │                              ┌──────────────────┐
       │                              │    Syncthing      │
       │                              │  (droplet↔desktop)│
       │                              └────────┬─────────┘
       │                                       │
       │                                       ▼
       │                              ┌──────────────────┐
       │                              │   Obsidian Vault  │
       │                              │  (markdown files) │
       │                              └────────┬─────────┘
       │                                       │
       │                              ┌────────┴─────────┐
       │                              │    Processor      │
       │                              │  (vault watcher)  │
       │                              └────────┬─────────┘
       │                                       │
       │                              ┌────────┴─────────┐
       │                              │  Registry (SQLite)│
       │                              └────────┬─────────┘
       │                                       │
       │                              ┌────────┴─────────┐
       │                              │   GCal Worker     │
       │                              │  (30s poll)       │
       │                              └────────┬─────────┘
       │                                       │
       │                              ┌────────┴─────────┐
       │                              │  Google Calendar  │
       │                              └──────────────────┘
```

Current verified breakpoints in this flow:

- **Capture contract drift:** attachment-backed multipart capture does not preserve the same metadata as JSON capture (`F-003`, `F-005`).
- **Attachment commit integrity:** files can be saved under a temporary UID before the final note UID is known (`F-004`).
- **Registry completeness:** note indexing is incomplete after cold start until note files change (`F-006`).
- **API summary/detail mismatch:** SPA task/note surfaces assume richer `/api/inbox` data than the registry summary currently returns (`F-007`).

---

## 5. Key File Locations for Audit

### Inkwell (Android)

| Purpose | Path |
|---------|------|
| Build config + signing | `app/build.gradle.kts` |
| Manifest | `app/src/main/AndroidManifest.xml` |
| HTTP client | `app/src/main/kotlin/io/inkwell/data/remote/CaptureApiService.kt` |
| Auth token storage | `app/src/main/kotlin/io/inkwell/data/local/PreferencesManager.kt` |
| Sync engine | `app/src/main/kotlin/io/inkwell/sync/InboxSyncEngine.kt` |
| Background sync | `app/src/main/kotlin/io/inkwell/sync/SyncWorker.kt` |
| Upload worker | `app/src/main/kotlin/io/inkwell/sync/UploadWorker.kt` |
| Network/auth setup | `app/src/main/kotlin/io/inkwell/di/NetworkModule.kt` |
| Room DB + DAO | `app/src/main/kotlin/io/inkwell/data/local/dao/NoteDao.kt` |
| Capture flow | `app/src/main/kotlin/io/inkwell/data/repository/CaptureRepository.kt` |
| ProGuard rules | `app/proguard-rules.pro` |
| Deep links | `app/src/main/kotlin/io/inkwell/ui/navigation/DeepLink.kt` |
| Biometric auth | `app/src/main/kotlin/io/inkwell/auth/BiometricAuthManager.kt` |
| FCM handler | `app/src/main/kotlin/io/inkwell/notifications/CaptureMessagingService.kt` |
| Widgets | `app/src/main/kotlin/io/inkwell/widget/` |
| Startup registration | `app/src/main/kotlin/io/inkwell/CaptureApp.kt` + `app/src/main/kotlin/io/inkwell/notifications/DeviceRegistrationManager.kt` |

### Nexus Server

| Purpose | Path |
|---------|------|
| Main server | `src/server.ts` |
| REST API | `src/api-server.ts` |
| Capture handler | `src/capture-server.ts` |
| Vault processor | `src/index.ts` + `src/scanner.ts` |
| GCal sync | `src/worker.ts` + `src/gcal-*.ts` |
| Email Commander | `src/email-commander.ts` |
| SQLite registry | `src/registry.ts` |
| Config parser | `src/config.ts` |
| Token encryption | `src/token-manager.ts` |
| Attachment validation | `src/attachment-handler.ts` |
| Docker config | `infra/docker-compose.yml` |
| Nginx config | `infra/nginx.conf` |
| App config | `infra/config.yaml` |
| Chrome extension manifest | `capture-extension/manifest.json` |
| Chrome extension auth/bootstrap | `capture-extension/background.js` + `capture-extension/options.js` + `capture-extension/sidepanel.js` |
| Web SPA capture/offline queue | `src/capture-web/public/js/form.js` + `src/capture-web/public/js/offline-queue.js` |
| Web SPA tasks/notes/lists | `src/capture-web/public/js/views/tasks-view.js` + `src/capture-web/public/js/views/notes-view.js` + `src/capture-web/public/js/views/lists-view.js` |

---

## 6. Screenshots & Visual References

Inkwell UI screenshots are available in the project root:

| File | Shows |
|------|-------|
| `screen_capture.png` | Capture screen (main input) |
| `screen_inbox.png` | Inbox list view |
| `screen_settings.png` | Settings (connection, sync, biometric) |
| `screen_syshealth.png` | System health dashboard |
| `screen_token_dialog.png` | Auth token entry dialog |
| `screen_inbox_synced.png` | Inbox after successful sync |

---

## 7. Git History Summary

### Inkwell (`master` branch)

```
28c6b4d refactor: rename package com.obsidiancapture → io.inkwell
cbc32df feat: stream attachment uploads + Material You widget polish
83b216d fix: deep audit — InboxSyncEngine, reactive auth, camera permission, streaming uploads (the nginx 50MB claim was later disproven by Pass 2 verification)
398d448 fix: resolve all 12 LLM audit findings (3-provider pipeline)
37a994d chore: bump version to 2.3.0 (versionCode 11)
c2634a2 feat: attachment upload — send multipart/form-data when captures have attachments
75baec0 feat: IDEA type, attachment picker, coach marks (autonomous)
d1b51a0 fix: instrumented test fixes (Android 16)
e457cbd fix: resolve 6 audit findings (parallel sync, lifecycle, architecture)
4ddf96b fix: resolve 3 critical audit findings (CancellationException, auth, token)
```

### Nexus Server (recent)

```
Session 2026-03-15: assetlinks.json patched (io.inkwell)
Session 2026-03-13: Deploy tracks 1-3, deal-hunter cleanup
Session 2026-03-10: Nexus Web SPA, Dashboard UX v3, Security Hardening
Session 2026-03-06: LLM Audit #3 — 12 findings all resolved
```

These commit/session notes are historical context only. They do not override the current open-finding table in Section 0. Pass 2 specifically disproved some older assumptions, including the nginx upload-size claim and the coherence of the current auth story.

---

## 8. Important Unknowns & Secondary Context

### 8.1 Follow-up Questions Requiring Runtime Validation

- Is `C:\Users\tyler\Documents\Claude Projects\Obsidian-Dashboard-Desktop\infra\config.yaml` intentionally set to `America/Los_Angeles`, or is that stale production configuration affecting schedule semantics?
- Once nginx upload limits are corrected, does `src/capture-server.ts` full-body multipart buffering create unacceptable memory pressure under concurrent uploads?
- How often does the cold-start note indexing gap surface in the deployed processor lifecycle after real restarts?
- What is the actual conflict frequency for list edits and other concurrent multi-client mutations, and is explicit conflict UX needed or is conditional write/versioning enough?
- Do Android authenticated requests ever cross redirects or proxies in a way that broadens proactive bearer exposure, or is the current `baseUrl` usage sufficient in practice?

### 8.2 Secondary Operational Context (Not Part of the Final Open-Finding Set)

- Firebase/app-registration cleanup around `google-services.json` may still matter operationally for the `io.inkwell` package, but it was not carried as a Pass 2 open audit finding.
- Windows Defender locking Gradle intermediates and 1 GB droplet build-memory pressure remain useful environment notes, but they are operational constraints rather than final platform audit findings.
- Historical package-name and asset-links transition work may still appear in older session notes; do not treat those as current open issues unless fresh test or runtime evidence reopens them.

---

## 9. Research Focus Recommendations

Future research and planning should stay aligned to the verified Pass 2 findings:

1. **Auth and session architecture modernization** - focus on removing shipped shared credentials and clarifying browser/mobile/extension auth boundaries (`F-001`, `F-002`, `F-012`).
2. **Capture schema and multipart unification** - define one canonical capture contract across JSON, multipart, Android, SPA, and extension (`F-003`, `F-005`).
3. **Attachment integrity and upload-envelope alignment** - fix UID sequencing and align proxy/server/client upload behavior (`F-004`, `F-009`).
4. **Registry and background reliability** - close cold-start indexing gaps, device-registration recovery gaps, and mutation-concurrency gaps (`F-006`, `F-010`, `F-011`).
5. **API shape and product-surface cleanup** - separate summary/detail shapes intentionally and remove misleading or half-supported surfaces (`F-007`, `F-008`, `F-011`, `F-012`).
6. **Observability and contract testing** - add cross-client contract coverage, reverse-proxy E2E checks, and runtime diagnostics for degraded flows (`F-003` through `F-010`).

---

## 10. Next-Step Remediation Handoff

This section is the immediate planning bridge for Claude Code after research-prompt generation.

### Track A - Auth and Credential Modernization

- **Priority:** Immediate
- **Targets:** Android `BuildConfig.DEFAULT_AUTH_TOKEN` fallback, extension token bootstrap/storage, browser/mobile enrollment model, revocation and rotation story, stale Google-auth surface cleanup
- **Related finding IDs:** `F-001`, `F-002`, `F-012`

### Track B - Capture Contract Unification

- **Priority:** Immediate
- **Targets:** canonical capture schema, JSON/multipart parity, note/task/list/schedule field handling, contract tests across Android/SPA/extension/server
- **Related finding IDs:** `F-003`, `F-005`

### Track C - Attachment Integrity and Upload Path Alignment

- **Priority:** Immediate
- **Targets:** final-UID-before-write sequencing, attachment staging/commit model, proxy/server/client upload envelope alignment, user-facing error behavior for rejected uploads
- **Related finding IDs:** `F-004`, `F-009`

### Track D - Registry and Background Reliability

- **Priority:** Near-term
- **Targets:** cold-start note indexing, startup device-registration retry, list read/write separation, conditional writes or versioning for concurrent mutations
- **Related finding IDs:** `F-006`, `F-010`, `F-011`

### Track E - API Shape and Product Surface Cleanup

- **Priority:** Near-term
- **Targets:** `/api/inbox` summary vs detail DTO split, SPA task/note/list behavior cleanup, offline attachment UX decision, removal or formal deprecation of stale auth/product surfaces
- **Related finding IDs:** `F-007`, `F-008`, `F-011`, `F-012`

### Research should answer

- What is the minimum-friction replacement for shipped shared credentials that still fits a self-hosted single-user deployment model?
- What canonical capture schema should exist across JSON and multipart, and where should that schema be enforced so clients cannot drift?
- Should attachments be staged until the final note UID is known, or should the system move to a different attachment identity model entirely?
- What reliability model should govern registry rebuilds, startup repair, and background self-healing for device registration and note indexing?
- Which client surfaces should consume summary DTOs versus detail DTOs, and which product surfaces should be simplified or deferred until the core capture-sync path is stable?

---

*This document should be updated after each audit cycle so the open-finding table, follow-up questions, and remediation handoff remain aligned with the latest verified findings.*
