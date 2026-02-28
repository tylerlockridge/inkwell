# Lessons Learned

### 2026-02-27 Android 16 Broke Espresso — Always Check Transitive Versions

**Context:** Setting up instrumented tests on a Pixel 10 Pro XL running Android 16 (API 36).

**Lesson:** Android 16 removed `InputManager.getInstance()`. Any Espresso version below 3.7.0 will crash on Android 16 with a `NoSuchMethodException`. The Compose BOM pulls in older Espresso versions transitively — you must explicitly override with `espresso-core:3.7.0`.

**Application:** After every Compose BOM upgrade, verify the transitive Espresso version is >= 3.7.0 when supporting Android 16+. Check via `./gradlew :app:dependencies | grep espresso`.

---

### 2026-02-27 HiltTestApplication Is Final — Use Runner Override for Pre-Init

**Context:** Needed to initialize WorkManager before Hilt injection in instrumented tests.

**Lesson:** `HiltTestApplication` is `final` and cannot be extended. `@CustomTestApplication` generates a class that may not be reliably found at runtime when the annotation processor cache is stale. The correct hook for any pre-test-application initialization is to override `callApplicationOnCreate` in the custom `AndroidJUnitRunner` subclass — this runs before any Activity or Hilt injection.

**Application:** Whenever a production `Application` class does initialization that `HiltTestApplication` won't replicate (e.g., on-demand WorkManager, Firebase, custom SDK init), override `callApplicationOnCreate` in the test runner. Never try to extend `HiltTestApplication`.

---

### 2026-02-27 LaunchedEffect(Unit) Races Ahead of Composable Attachment in Tests

**Context:** `CaptureScreen` calls `focusRequester.requestFocus()` in `LaunchedEffect(Unit)` to auto-focus the text field on screen open.

**Lesson:** In instrumented tests, `LaunchedEffect(Unit)` can fire before the composable that owns the `FocusRequester` is attached to the composition. This causes `FocusRequester not initialized` errors. The production app works fine because the timing is slower. Always guard `requestFocus()` with `try/catch(IllegalStateException)`.

**Application:** Any `LaunchedEffect(Unit)` that calls `focusRequester.requestFocus()` should be wrapped in try/catch. This is harmless in production and prevents flaky tests.

---

### 2026-02-27 M3 NavigationBarItem Semantics Are Partially Merged

**Context:** Writing instrumented tests that navigate between tabs using M3 `NavigationBar` + `NavigationBarItem`.

**Lesson:** In Compose BOM 2024.12.01+, M3 `NavigationBarItem` does NOT surface the icon's `contentDescription` in the merged semantics tree. `onNodeWithContentDescription("Inbox")` will fail. The label text is available in the unmerged tree. Additionally, the IME (keyboard) can obscure the nav bar when using `Modifier.imePadding()` on the Scaffold.

**Application:** Always navigate to tabs via `onNodeWithText("Label", useUnmergedTree = true).performClick()` in tests. Always call `Espresso.closeSoftKeyboard()` in `@Before` before any nav bar interactions.

---

### 2026-02-27 PowerShell File Deletion From Git Bash Requires -File Flag

**Context:** Trying to delete a locked `app/build` directory from Git Bash on Windows.

**Lesson:** `cmd /c "rmdir /s /q app\build"` hangs silently from Git Bash. `cmd /c "powershell -Command Remove-Item..."` also hangs. The reliable approach is to write a `.ps1` file and invoke it with `powershell.exe -File script.ps1` — this correctly captures output and exits cleanly.

**Application:** For any Windows-specific file system operations from Git Bash, write a `.ps1` script and invoke with `powershell.exe -File`. Delete the script afterward.
