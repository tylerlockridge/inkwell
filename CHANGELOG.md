# Changelog

## 2026-02-27 Session — Instrumented Tests Passing on Android 16

- All 17 instrumented Compose UI tests passing on Pixel 10 Pro XL (Android 16 / API 36)
- Fixed `espresso-core:3.7.0` + `runner:1.7.0` for Android 16 `InputManager` compatibility
- Added `callApplicationOnCreate` override in `HiltTestRunner` for WorkManager pre-initialization
- Guarded `FocusRequester.requestFocus()` in `CaptureScreen` with try/catch
- Fixed M3 `NavigationBarItem` semantics — use text + `useUnmergedTree=true` for nav in tests
- Added `performScrollTo()` for below-fold Settings elements
- **Decisions:** 3 captured in `knowledge/DECISIONS.md`
- **Next:** Feature work or release prep — project in clean state (commit `728b240`)

## 2026-02-27 Session — Code Quality Pass + Instrumented Test Scaffolding

- `CaptureScreen.kt` split: `SmartToolbar` + panels extracted to `CaptureToolbar.kt` (973 → 457+548 lines)
- `SettingsScreen.kt` split: `SettingsConnectionCard.kt` + `SettingsComponents.kt` (685 → 289+298+148 lines)
- Deprecated `ClickableText` → `Text` + `LinkAnnotation.Url` in `MarkdownText`/`MarkdownParser`
- Instrumented test scaffolding added: `HiltTestRunner`, `CaptureScreenTest`, `InboxScreenTest`, `SettingsScreenTest`
- 27 unit tests passing, zero compiler warnings
- **Next:** Run instrumented tests on device ✅ (done in next session)
