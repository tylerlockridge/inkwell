# Inkwell Architecture

Android app (Kotlin + Jetpack Compose) that captures notes/tasks to an Obsidian vault via a
self-hosted REST API (Obsidian Dashboard Desktop).

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| DI | Hilt |
| Local DB | Room (FTS4 for search) |
| Network | Ktor client (OkHttp engine) |
| Background | WorkManager (HiltWorker) |
| Preferences | DataStore (plain) + EncryptedSharedPreferences (auth token) |
| Auth | Google Sign-In (Credential Manager) + JWT bearer token |
| Notifications | FCM push + local WorkManager error notifications |
| Widgets | Glance AppWidget |

---

## Module Structure

```
app/src/main/kotlin/com/obsidiancapture/
├── MainActivity.kt            # Entry point; delegates lock/sync to MainViewModel
├── MainViewModel.kt           # Biometric lock state + startup sync trigger
├── auth/                      # BiometricAuthManager
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt     # Room DB; migrations v1→2→3
│   │   ├── dao/NoteDao.kt     # Queries, FTS4 search, pending sync
│   │   ├── entity/NoteEntity.kt
│   │   └── PreferencesManager.kt   # DataStore + EncryptedSharedPreferences
│   ├── remote/
│   │   ├── CaptureApiService.kt    # Ktor HTTP calls
│   │   └── dto/               # Serializable request/response DTOs
│   └── repository/
│       ├── CaptureRepository.kt    # Capture + offline fallback
│       └── InboxRepository.kt      # Inbox sync + note CRUD
├── di/
│   ├── NetworkModule.kt       # HttpClient (authenticated + unauthenticated)
│   └── DatabaseModule.kt
├── notifications/             # NotificationChannels, FCM handler
├── share/                     # ShareIntentParser
├── sync/
│   ├── SyncScheduler.kt       # WorkManager scheduling (periodic + immediate)
│   ├── SyncWorker.kt          # Server → Room periodic fetch
│   └── UploadWorker.kt        # Room → Server periodic upload
├── ui/
│   ├── capture/               # Capture screen + toolbar + dialogs
│   ├── inbox/                 # Inbox list + swipe actions
│   ├── detail/                # Note detail + edit
│   ├── settings/              # Server URL, auth, sync interval
│   ├── health/                # System health dashboard
│   ├── auth/                  # LockScreen (biometric gate)
│   ├── navigation/            # NavHost, Screen routes, DeepLink
│   ├── components/            # MarkdownText, shared composables
│   └── theme/                 # Colors, typography, haptics, animation
└── widget/                    # Glance home screen widget
```

---

## Sync Strategy

Two WorkManager workers run on the same configurable interval (default: 15 min):

### SyncWorker — Server → Room
1. Fetch inbox list from `GET /api/inbox`
2. For each item, read local note from Room (serial DB reads)
3. **Skip** if `localNote.pendingSync == true` (local changes in flight)
4. **Skip** if `localNote.updated >= item.updated_at` (local is same or newer)
5. Fetch full detail concurrently for all stale items (`async/awaitAll`)
6. Upsert results to Room

### UploadWorker — Room → Server
1. Query all notes with `pendingSync = true`
2. For each note, route by uid prefix:
   - `pending_*` → `POST /api/capture` (new note, gets server-assigned uid)
   - other → `PATCH /api/note/:uid` (status/content update)
3. On success: replace/update note, clear `pendingSync`

### Conflict Resolution — Last-Write-Wins
- Server timestamp `updated_at` compared to local `updated` (ISO 8601 string comparison)
- Local wins on ties: `localNote.updated >= item.updated_at` → skip
- Pending-local always wins: `pendingSync = true` → never overwritten

### Error Classification (UploadWorker)
| HTTP Status | Action |
|-------------|--------|
| 401 | Auth failure → `Result.failure()`, stop all retries |
| 400–499 | Permanent → mark `syncError`, continue other notes |
| 5xx / network | Transient → `Result.retry()` with exponential backoff |

---

## Token Lifecycle

1. User signs in via Google (Credential Manager) → backend returns JWT
2. JWT stored in `EncryptedSharedPreferences` (AES256-GCM, Keystore-backed)
3. Ktor `HttpClient` reads token via `loadTokens`/`refreshTokens` — **no** `sendWithoutRequest`,
   so token is only sent after a 401 challenge (prevents proactive transmission to FCM, CDNs, etc.)
4. On 401 from SyncWorker: token cleared + auth-expired notification posted → user re-authenticates
5. Token is never compiled into the APK (`BuildConfig` field removed)

---

## Offline Fallback

`CaptureRepository.capture()` saves to Room with `pendingSync = true` when the network call fails.
`UploadWorker` picks it up on the next run. The UI shows a pending badge count from
`noteDao.getPendingSyncCount()`.

---

## Biometric Lock

`MainViewModel` holds `isLocked: StateFlow<Boolean>`. `MainActivity` checks
`BiometricAuthManager.checkCapability()` (requires Activity context) on create and after
returning from background (60s timeout). Authenticate via `BiometricPrompt`; on success calls
`viewModel.unlock()`.
