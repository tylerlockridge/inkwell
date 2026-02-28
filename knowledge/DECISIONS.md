# Decisions

### 2026-02-27 WorkManager Init Strategy in Hilt Instrumented Tests

**Context:** `CaptureApp` implements `Configuration.Provider` for on-demand WorkManager init (manifest disables `WorkManagerInitializer`). When Hilt tests swap in `HiltTestApplication`, it doesn't implement `Configuration.Provider`, causing `SyncScheduler` to crash when calling `WorkManager.getInstance()` at construction time.

**Decision:** Override `callApplicationOnCreate` in `HiltTestRunner` to call `WorkManager.initialize()` before `super.callApplicationOnCreate(app)`.

**Rationale:** `HiltTestApplication` is `final` — it cannot be extended. `@CustomTestApplication` generates a class at compile time that wasn't reliably picked up. `callApplicationOnCreate` runs before any Activity or Hilt injection, making it the correct pre-initialization hook.

**Alternatives considered:**
- Extend `HiltTestApplication`: Rejected — class is `final`
- `@CustomTestApplication(BaseTestApp::class)`: Rejected — generated `TestCaptureApp_` class not reliably found at runtime (Hilt annotation processor cache issues)

**Impact:** All instrumented tests can now use `WorkManager` without crashing. Pattern is reusable for any Hilt test project with on-demand WorkManager init.

---

### 2026-02-27 Espresso Version for Android 16 Compatibility

**Context:** All 17 instrumented tests crashed with `NoSuchMethodException: android.hardware.input.InputManager.getInstance` on Pixel 10 Pro XL (Android 16 / API 36). The Compose BOM 2024.12.01 pulls in `espresso-core:3.6.0` transitively.

**Decision:** Pin `espresso-core:3.7.0` and `androidx.test:runner:1.7.0` explicitly in `build.gradle.kts` to override the transitive dependency.

**Rationale:** Android 16 removed `InputManager.getInstance()`. Espresso 3.7.0 is the first stable release to remove this call. Version `3.6.2` does not exist on Maven; `3.7.0` is the correct target.

**Alternatives considered:**
- Downgrade Compose BOM: Rejected — would lose other fixes
- Use emulator with older API: Rejected — physical device is the target

**Impact:** All tests pass on Android 16. Must check espresso version whenever a new Compose BOM is adopted.

---

### 2026-02-27 M3 NavigationBarItem Semantics Navigation Strategy

**Context:** Instrumented tests tried to navigate to Inbox/Settings tabs via `onNodeWithContentDescription("Inbox")`, but tests couldn't find the node. M3 `NavigationBarItem` does not surface the icon's `contentDescription` in the merged semantics tree in Compose BOM 2024.12.01+.

**Decision:** Navigate using `onNodeWithText("Inbox", useUnmergedTree = true).performClick()` and add `Espresso.closeSoftKeyboard()` in `@Before` to ensure the nav bar isn't obscured by the IME.

**Rationale:** The label text of a `NavigationBarItem` is always present in the unmerged tree. This is more reliable than contentDescription which depends on M3 implementation internals.

**Alternatives considered:**
- `onNodeWithTag(...)`: Would require adding test tags to production code — rejected as invasive
- `onNodeWithContentDescription(...)`: Fails — not in merged tree

**Impact:** Navigation in all screen tests works reliably. Pattern to use for all future nav bar interactions in tests.
