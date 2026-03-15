# Inkwell + Nexus Platform — Codex Audit Brief

**Created:** 2026-03-15
**Purpose:** Shared context document for cross-model audits (Codex, Gemini, Claude). Provides the complete picture of the Inkwell Android app and the Nexus server platform it connects to.
**Audience:** Any LLM performing code review, security audit, architecture analysis, or feature planning.

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
| Auth | Bearer token (EncryptedSharedPreferences) + optional Google Sign-In |
| Deep Links | `inkwell://capture`, `inkwell://inbox`, `inkwell://settings`, `inkwell://health` |
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
- Server accepts up to 25MB per file, 50MB total (nginx: 50MB `client_max_body_size`)

### 2.4 Test Coverage

| Category | Files | Tests | Framework |
|----------|-------|-------|-----------|
| Unit tests | 36 | 294 | JUnit4 + MockK + Robolectric + Turbine |
| Instrumented tests | 4 | 17 | Hilt + Compose UI testing |
| **Total** | **40** | **311** | |

**Quality gates:** `./gradlew test` + `./gradlew lint` (both clean)

### 2.5 Audit History

| Date | Provider(s) | Score | Key Findings |
|------|-------------|-------|-------------|
| 2026-02-26 | Codex (GPT-4o) | 6.3/10 | Zero instrumented tests, large files, allowBackup=true |
| 2026-02-28 | Codex + Gemini + Monica | 5.4/10 | CancellationException, N+1 queries, no lifecycle awareness |
| 2026-03-02 | Claude (deep composite) | — | 15 items (all resolved): pending orphan, JWT logging, biometric weak |
| 2026-03-06 | Codex 5.3 | 8.6/10 | Auth header bug (sendWithoutRequest), serialization catch |
| 2026-03-14 | Gemini 3.1 + Codex x2 | 7.0/10 | 12 findings (all resolved): attachment routing, timestamp comparison, N+1 |
| 2026-03-15 | Claude (deep composite) | — | 10 items (all resolved): InboxSyncEngine extraction, reactive auth, streaming uploads |

### 2.6 Recent Changes (This Session — 2026-03-15)

1. **InboxSyncEngine** extracted — shared sync logic eliminates code drift between SyncWorker and InboxRepository
2. **Reactive authToken** — `MutableStateFlow` replaces one-shot `flow{}`
3. **Camera permission** — runtime request before `IMAGE_CAPTURE` intent
4. **Nginx 50MB** — `client_max_body_size` bumped from 1MB
5. **Streaming uploads** — `ChannelProvider` replaces `readBytes()`
6. **Widget polish** — Material You, rounded cards, tonal buttons, pending sync badges
7. **Package rename** — `com.obsidiancapture` → `io.inkwell`, scheme `inkwell://`

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
| Testing | Vitest (1366 passing tests, 65 test files) |
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
| POST | `/api/auth/google` | None | Google Sign-In token exchange |
| POST | `/api/device/register` | Bearer | FCM device registration |
| DELETE | `/api/device/:id` | Bearer | Unregister device |
| GET | `/healthz` | None | Health check |
| GET | `/.well-known/assetlinks.json` | None | Android App Links verification |

### 3.4 Chrome Extension

**Location:** `capture-extension/`
**Manifest:** v3

| Component | File | Purpose |
|-----------|------|---------|
| Service Worker | `background.js` | Lifecycle, message routing, context menus |
| Side Panel | `sidepanel.html/js` | Capture form (auto-populates page title, URL, selection) |
| Options | `options.html/js` | Server URL + auth token config |

**Flow:** User clicks extension → side panel opens → form pre-filled → POST /api/capture → notification

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

**Features:** Offline queue (IndexedDB), dark mode, PWA installable, connection awareness banner

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

- **Auth:** Bearer token + optional Google Sign-In + cookie fallback
- **Rate limiting:** 600 req/min API, 30 req/min capture, 60 req/min webhook, 10 auth failures/5min
- **Docker:** `cap_drop: ALL`, `no-new-privileges: true`, 127.0.0.1 binding (except nginx)
- **TLS:** TLSv1.2+, HSTS, X-Frame-Options: DENY, X-Content-Type-Options: nosniff
- **SQLite:** WAL mode, FK enforcement, path traversal guards
- **Secrets:** All in `.env` on droplet, never committed

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
| Docker config | `infra/docker-compose.yml` |
| Nginx config | `infra/nginx.conf` |
| App config | `infra/config.yaml` |
| Chrome extension | `capture-extension/` |
| Web SPA | `src/capture-web/public/` |

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
83b216d fix: deep audit — InboxSyncEngine, reactive auth, camera permission, nginx 50MB
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

---

## 8. Known Issues & Technical Debt

| # | Component | Issue | Severity |
|---|-----------|-------|----------|
| 1 | Server | assetlinks.json test expects old package name (1 failing test) | Low |
| 2 | Inkwell | `google-services.json` still references Firebase project `obsidian-capture-11a09` — needs new app for `io.inkwell` in Firebase Console | Medium |
| 3 | Inkwell | Windows Defender locks Gradle build intermediates (recurring) | Low (dev-only) |
| 4 | Server | `docker compose build` maxes 1GB droplet during `npm ci` (better-sqlite3 native build) | Low |
| 5 | Inkwell | `NotificationActionReceiver.scope` never cancelled (BroadcastReceiver lifecycle) | Low |
| 6 | Server | registry.ts is 32k lines — candidate for decomposition | Medium |

---

## 9. Audit Focus Recommendations

For a comprehensive audit, prioritize:

1. **API contract alignment** — Do Inkwell DTOs match server response shapes exactly? Are there edge cases where the server returns unexpected JSON?
2. **Auth token lifecycle** — Token stored in APK BuildConfig, EncryptedSharedPrefs, and sent via Ktor bearer auth. Verify no leaks in logs/crash reports.
3. **Sync correctness** — Last-write-wins conflict resolution, tombstone sweep, pending note protection during sync. Race conditions between SyncWorker and manual pull-to-refresh.
4. **Error handling** — CancellationException propagation, 401/413/5xx handling, network timeout behavior.
5. **Server security** — Rate limiting bypass vectors, path traversal in capture/attachment handling, SQL injection in registry queries.
6. **Chrome extension** — Content Security Policy, host permission scope, credential storage in `chrome.storage`.
7. **Docker security** — Container escape vectors, secret management, volume mount permissions.
8. **Performance** — Concurrent detail fetch fan-out (no limit), SQLite WAL contention under load, attachment streaming memory profile.

---

*This document should be updated after each audit cycle with new findings and resolutions.*
