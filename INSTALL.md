# Obsidian Capture — Android Install Guide

## Quick Install

1. Download the latest `obsidian-capture-*-release.apk` from [GitHub Releases](../../releases)
2. On your Android device, enable **Settings > Install unknown apps** for your browser/file manager
3. Open the downloaded APK to install
4. Launch **Obsidian Capture** and configure your server in Settings

## First-Time Setup

1. Open the app and tap the **Settings** tab (gear icon)
2. Enter your **Server URL** (e.g., `https://tyler-capture.duckdns.org`)
3. Enter your **Auth Token** (the `CAPTURE_AUTH_TOKEN` value from your server)
4. Tap **Test Connection** — you should see a green checkmark
5. Optionally enable **Push Notifications** and **Biometric Unlock**

## Requirements

- Android 8.0 (Oreo) or higher
- Network access to your Obsidian Dashboard server

## Features

- **Quick Capture**: Title, body, tags, kind, calendar, priority, schedule
- **Inbox**: Browse, search, filter by status, swipe to mark done/drop
- **Note Detail**: View and edit notes, change status
- **Offline Mode**: Captures saved locally and synced when online
- **Background Sync**: Automatic every 15 minutes (configurable)
- **Push Notifications**: Real-time alerts for new captures (requires FCM setup)
- **Batch Mode**: Rapid sequential capture without leaving the screen

## Push Notifications (Optional)

Push notifications require Firebase Cloud Messaging setup:

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.obsidiancapture`
3. Download `google-services.json` and place it in `android/app/`
4. Add FCM service account credentials to your server config:
   ```yaml
   fcm:
     enabled: true
     projectId: "your-project-id"
     clientEmail: "firebase-adminsdk-xxx@your-project.iam.gserviceaccount.com"
     privateKey: "<your-service-account-key>"
   ```

## Building from Source

### Prerequisites
- JDK 17
- Android SDK (API 35)

### Debug Build
```bash
cd android
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
cd android
KEYSTORE_PATH=path/to/release.keystore \
KEYSTORE_PASSWORD=changeit \
KEY_ALIAS=release \
KEY_PASSWORD=changeit \
./gradlew assembleRelease
# APK at: app/build/outputs/apk/release/app-release.apk
```

### Creating a Release Keystore
```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias release \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=Obsidian Capture"
```

## Updating

Download the new APK from GitHub Releases and install over the existing app. Your settings and local data are preserved.

## Troubleshooting

**"App not installed" error**: Ensure you have enough storage and that the APK isn't corrupted. Try downloading again.

**Connection test fails**: Verify your server URL includes the protocol (`https://`) and that your auth token is correct.

**Sync not working**: Check that your server is reachable and the auth token hasn't expired. Try "Sync Now" in Settings.

**Notifications not arriving**: Ensure notification permissions are granted in Android Settings > Apps > Obsidian Capture > Notifications.
