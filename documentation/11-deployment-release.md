# Feature: Deployment & Release

*Created: 2026-03-02 | Updated: 2026-03-02 | Project: Inkwell*

---

## Feature Overview

**What it does:**
Describes the build system, SDK targets, release configuration, signing, CI/CD setup, and server dependency for Inkwell Android releases.

**What it does NOT do:**
- Does not manage server infrastructure (that is Obsidian-Dashboard-Desktop)
- Does not describe automated Play Store publishing (verify CI/CD configuration for current state)

---

## Build Configuration

| Setting | Value |
|---------|-------|
| Build system | Gradle with AGP 8.5+ |
| Language | Kotlin 2.0+ |
| `compileSdk` | 35 (Android 15) |
| `targetSdk` | 35 (Android 15) |
| `minSdk` | 26 (Android 8.0 Oreo) |

---

## Release Build

| Setting | Configuration |
|---------|--------------|
| R8 minification | Enabled |
| ProGuard rules | Applied for data classes (prevents stripping) |
| Signing | Release signing config in `build.gradle.kts` (keystore path TBD per environment) |
| APK output | `obsidian-capture-*.apk` in project root (versioned) |

Known APK artifacts (tracked in git root):
- `obsidian-capture-2.1.0-release.apk`
- `obsidian-capture-2.1.1-release.apk`
- `obsidian-capture-2.1.2-release.apk`

---

## Versioning

| Field | Location |
|-------|---------|
| `versionCode` | `build.gradle.kts` |
| `versionName` | `build.gradle.kts` |

---

## CI/CD

- GitHub Actions referenced in project configuration
- Verify current workflow files in `.github/workflows/` for active job definitions
- Typical gates: compile, lint, unit tests, build release APK

---

## Room Schema Migration Tracking

Room schema is exported to the `schema/` directory and committed to git. This ensures that:
- Migration history is auditable
- Incompatible schema changes are detectable at code review
- Automated migration tests can reference the exported schema

---

## Server Dependency

Inkwell requires the Obsidian-Dashboard-Desktop backend:
- **Host:** `138.197.81.173` (DigitalOcean droplet)
- **Protocol:** HTTPS
- All sync, capture push, and auth token exchange go through this server

Without the server, the app degrades to offline-only mode: captures save locally with `pendingSync=true` and upload when connectivity and auth are restored.

---

## Target Distribution

- **Platform:** Google Play Store
- **Release track:** Standard release track
- **Minimum supported Android:** 8.0 (API 26)

---

## Status

| Item | Status | Notes |
|------|--------|-------|
| Gradle AGP 8.5+ / Kotlin 2.0+ | ✅ PASS | |
| compileSdk / targetSdk 35 | ✅ PASS | Android 15 |
| minSdk 26 | ✅ PASS | Android 8.0 |
| R8 minification enabled | ✅ PASS | |
| ProGuard rules for data classes | ✅ PASS | |
| Room schema exported to git | ✅ PASS | `schema/` directory |
| GitHub Actions CI/CD | ⚠️ WARN | Referenced; verify workflow files |
| Release signing keystore | ⚠️ WARN | Keystore path TBD per environment |
| Google Play target | 🔲 TODO | Verify current Play listing status |
