# Inkwell Android App

## Critical Context
Android app (Kotlin + Jetpack Compose) that captures notes/tasks to an Obsidian vault via the
Obsidian Dashboard Desktop server. Previously lived at `Obsidian-Dashboard/android/`.

## Tech Stack
Language: Kotlin | UI: Jetpack Compose | Architecture: MVVM + Repository pattern
Build: Gradle (KTS) | Min SDK: 26 | Target SDK: 34

## File Locations
```
Project Root: C:\Users\tyler\Documents\Claude Projects\Inkwell
Source:       app/src/main/kotlin/com/obsidiancapture/
Tests:        app/src/test/ (unit tests), app/src/androidTest/ (instrumented)
Infra:        infra/docker-compose.yml (Obsidian-Dashboard-Desktop)
Server:       See Obsidian-Dashboard-Desktop project — 138.197.81.173
```

## Quality Gates
```bash
./gradlew test          # unit tests
./gradlew lint          # lint checks
./gradlew assembleRelease  # verify release build
```

## Secrets Policy
**NEVER** commit: `local.properties`, `keys/` directory (signing keystore), auth tokens.

## Do NOT
- `git push --force`
- Commit local.properties or keystore files

## Repo
GitHub: https://github.com/tylerlockridge/inkwell
