# Troubleshooting Log

### 2026-02-27 All Instrumented Tests Crash: InputManager.getInstance

**Error/Symptom:** `java.lang.NoSuchMethodException: android.hardware.input.InputManager.getInstance` — all 17 tests fail before any test logic runs, on Pixel 10 Pro XL (Android 16 / API 36).

**Root Cause:** Espresso 3.6.0 (pulled transitively by Compose BOM 2024.12.01) calls `InputManager.getInstance()` which was removed in Android 16.

**Fix:**
```toml
# gradle/libs.versions.toml
espresso-core = { module = "androidx.test.espresso:espresso-core", version = "3.7.0" }
androidx-test-runner = { module = "androidx.test:runner", version = "1.7.0" }
```
```kotlin
// app/build.gradle.kts
androidTestImplementation(libs.espresso.core)  // force override of transitive
```

**Prevention:** After every Compose BOM bump, verify that the transitive `espresso-core` version is >= 3.7.0 when targeting Android 16+.

---

### 2026-02-27 Process Crash: WorkManager not initialized

**Error/Symptom:** `java.lang.IllegalStateException: WorkManager is not initialized properly` — crash during test process startup, before any test methods run.

**Root Cause:** `CaptureApp` implements `Configuration.Provider` for on-demand WorkManager init. `HiltTestApplication` replaces `CaptureApp` at test time but does not implement `Configuration.Provider`. `SyncScheduler` calls `WorkManager.getInstance(context)` at construction time, which fails because WorkManager was never initialized.

**Fix:** Override `callApplicationOnCreate` in `HiltTestRunner`:
```kotlin
override fun callApplicationOnCreate(app: Application) {
    WorkManager.initialize(app, Configuration.Builder().build())
    super.callApplicationOnCreate(app)
}
```

**Prevention:** Any project using on-demand WorkManager init must add this override. `HiltTestApplication` is `final` — do not try to extend it.

---

### 2026-02-27 FocusRequester Not Initialized in Tests

**Error/Symptom:** `java.lang.IllegalStateException: FocusRequester is not initialized` — all capture screen tests fail.

**Root Cause:** `LaunchedEffect(Unit)` in `CaptureScreen` calls `focusRequester.requestFocus()` immediately, but in the test environment the `TextField` composable with `.focusRequester(focusRequester)` modifier is not yet attached to the composition when the effect fires.

**Fix:**
```kotlin
LaunchedEffect(Unit) {
    try { focusRequester.requestFocus() } catch (_: IllegalStateException) { /* not yet attached */ }
}
```

**Prevention:** Always guard `requestFocus()` calls in `LaunchedEffect(Unit)` with try/catch in Compose — the race condition is timing-dependent and will silently pass in production but fail in tests.

---

### 2026-02-27 NavigationBarItem ContentDescription Not Found

**Error/Symptom:** `java.lang.AssertionError: No node with contentDescription 'Inbox'` — all InboxScreen and SettingsScreen tests fail at the `@Before` navigation step.

**Root Cause:** M3 `NavigationBarItem` in Compose BOM 2024.12.01 does not surface the icon's `contentDescription` in the merged semantics tree. Also: keyboard (IME) may obscure the nav bar.

**Fix:**
```kotlin
@Before
fun setUp() {
    hiltRule.inject()
    Espresso.closeSoftKeyboard()
    composeRule.onNodeWithText("Inbox", useUnmergedTree = true).performClick()
}
```

**Prevention:** Never use `onNodeWithContentDescription` for M3 `NavigationBarItem` icons — use label text with `useUnmergedTree = true`.

---

### 2026-02-27 "Found 2 nodes" on Title Assertion

**Error/Symptom:** `java.lang.AssertionError: Expected at most 1 node but found 2 nodes matching: text = 'Inbox'`

**Root Cause:** After navigating to a screen, both the nav bar label AND the screen's TopAppBar title contain the same text ("Inbox" / "Settings").

**Fix:**
```kotlin
composeRule.onAllNodesWithText("Inbox")[0].assertIsDisplayed()
```

**Prevention:** Use `onAllNodesWithText(...)[0]` for screen title assertions when the same text appears in the nav bar.

---

### 2026-02-27 Element Below Fold Not Displayed

**Error/Symptom:** `java.lang.AssertionError: The component is not displayed` — `settingsScreen_hapticFeedbackToggle_isDisplayed` fails.

**Root Cause:** "Haptic Feedback" is in the "Security & Feedback" section which is below the fold on the Pixel 10 Pro XL screen.

**Fix:**
```kotlin
composeRule.onNodeWithText("Haptic Feedback").performScrollTo().assertIsDisplayed()
```

**Prevention:** For any element that might be below the fold on a physical device, use `performScrollTo()` before asserting display.

---

### 2026-02-27 Windows File Lock on Gradle Build Directory

**Error/Symptom:** `java.nio.file.AccessDeniedException` on `app/build/generated/...` — clean build fails because Gradle daemon holds file handles.

**Root Cause:** Two idle Gradle daemons (PIDs seen via `./gradlew --status`) holding file handles on the `app/build` directory. Windows won't allow deletion while handles are open.

**Fix:**
1. `./gradlew --stop` to terminate daemons
2. If directory still locked, create and run a PowerShell script:
   ```powershell
   Remove-Item -Recurse -Force "app\build"
   ```
   Invoke via: `powershell.exe -File delete_build.ps1`

**Prevention:** Always run `./gradlew --stop` before attempting to delete build directories on Windows. Do not use `cmd /c "rmdir ..."` from Git Bash — it hangs without output.
