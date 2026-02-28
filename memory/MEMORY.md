# Inkwell Project Memory

## Build System
- **JDK**: Must use Android Studio's bundled JRE (`/c/Program Files/Android/Android Studio/jbr`)
  - System Java is JDK 8 (Temurin), which is too old for AGP 8.7.3 (needs JDK 11+)
  - Always run: `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH" ./gradlew ...`
- **File locks**: Android Studio holds Gradle build files. Kill Java processes before clean builds:
  - `cmd /c "taskkill /F /IM java.exe"`
  - `./gradlew --stop` to stop daemons first

## Architecture
- Package: `com.obsidiancapture` | Min SDK 26, Target SDK 35
- MVVM + Hilt DI | Jetpack Compose UI | Room + Ktor
- Navigation: CaptureNavHost → default start = CaptureScreen

## Key File Locations
```
ui/capture/CaptureScreen.kt    — main composable + dialogs + chips (~457 lines)
ui/capture/CaptureToolbar.kt   — SmartToolbar + all panels (~548 lines)
ui/capture/CaptureUiState.kt   — state + ToolbarPanel enum
ui/capture/CaptureViewModel.kt — ViewModel
ui/inbox/InboxScreen.kt        — inbox with swipe-to-dismiss
```

## Hilt Instrumented Testing (CRITICAL GOTCHA)
- Hilt testing package is `dagger.hilt.android.testing.*` NOT `com.google.dagger.hilt.android.testing.*`
- Required build.gradle.kts deps:
  ```kotlin
  androidTestImplementation(libs.hilt.testing)          // hilt-android-testing
  androidTestImplementation(libs.androidx.test.runner)  // needed for AndroidJUnitRunner
  androidTestImplementation(libs.espresso.core)         // 3.7.0 fixes Android 16 InputManager issue
  kspAndroidTest(libs.hilt.compiler)
  ```
- libs.versions.toml: `espresso-core = { module = "androidx.test.espresso:espresso-core", version = "3.7.0" }` and `runner = "1.7.0"`
- testInstrumentationRunner must be `"com.obsidiancapture.HiltTestRunner"`
- Test classes need `@HiltAndroidTest` + `@get:Rule val hiltRule = HiltAndroidRule(this)` (order=0, before composeRule at order=1)

## Hilt + WorkManager + Instrumented Tests
- CaptureApp implements `Configuration.Provider` (on-demand WorkManager init, `WorkManagerInitializer` disabled in manifest)
- HiltTestApplication does NOT implement Configuration.Provider → SyncScheduler crashes at init
- Fix: override `callApplicationOnCreate` in HiltTestRunner to call `WorkManager.initialize()` BEFORE super
- `HiltTestApplication` is final — do NOT try to extend it; use `callApplicationOnCreate` override instead

## Android 16 Compose Test Quirks (Pixel 10 Pro XL)
- `FocusRequester.requestFocus()` in `LaunchedEffect(Unit)` can fire before TextField is attached in tests
  - Fix: wrap in `try { } catch (_: IllegalStateException) { }`
- M3 `NavigationBarItem` does NOT surface icon contentDescription in merged semantics tree
  - `onNodeWithContentDescription("Inbox")` fails — use `onNodeWithText("Inbox", useUnmergedTree = true)` instead
- Keyboard may obscure nav bar — call `Espresso.closeSoftKeyboard()` in @Before before clicking nav items
- Nav label text also appears in screen title → use `onAllNodesWithText(...)[0]` not `onNodeWithText(...)`
- Settings screen is scrollable — use `performScrollTo()` before asserting elements below the fold

## File Splits Done (2026-02-27)
- `CaptureScreen.kt` (973→457) + `CaptureToolbar.kt` (548) — toolbar/panels extracted
- `SettingsScreen.kt` (685→289) + `SettingsConnectionCard.kt` (298) + `SettingsComponents.kt` (148)
- `MarkdownText.kt` — fixed deprecated `ClickableText` → `Text` + `LinkAnnotation.Url`

## Quality Gates
```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" PATH="$JAVA_HOME/bin:$PATH" ./gradlew test
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" PATH="$JAVA_HOME/bin:$PATH" ./gradlew lint
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" PATH="$JAVA_HOME/bin:$PATH" ./gradlew assembleRelease
```
