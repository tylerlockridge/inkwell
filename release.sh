#!/bin/bash
# Release script for Obsidian Capture Android app
# Usage: ./release.sh [version]
# Example: ./release.sh 1.1.0

set -euo pipefail

VERSION="${1:?Usage: ./release.sh <version> (e.g., 1.1.0)}"
TAG="android-v${VERSION}"

echo "=== Obsidian Capture Release ${VERSION} ==="
echo ""

# Verify we're on the right branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: ${BRANCH}"

# Check for uncommitted changes
if ! git diff --quiet HEAD; then
  echo "ERROR: Uncommitted changes detected. Commit or stash first."
  exit 1
fi

# Update versionName in build.gradle.kts
GRADLE_FILE="android/app/build.gradle.kts"
if grep -q "versionName = \"${VERSION}\"" "${GRADLE_FILE}"; then
  echo "Version already set to ${VERSION}"
else
  sed -i "s/versionName = \".*\"/versionName = \"${VERSION}\"/" "${GRADLE_FILE}"
  echo "Updated versionName to ${VERSION}"

  # Auto-increment versionCode
  CURRENT_CODE=$(grep 'versionCode' "${GRADLE_FILE}" | head -1 | sed 's/[^0-9]//g')
  NEW_CODE=$((CURRENT_CODE + 1))
  sed -i "s/versionCode = ${CURRENT_CODE}/versionCode = ${NEW_CODE}/" "${GRADLE_FILE}"
  echo "Updated versionCode to ${NEW_CODE}"

  git add "${GRADLE_FILE}"
  git commit -m "chore: bump android version to ${VERSION} (code ${NEW_CODE})"
fi

# Create and push tag
echo ""
echo "Creating tag: ${TAG}"
git tag -a "${TAG}" -m "Android release ${VERSION}"
git push origin "${TAG}"

echo ""
echo "=== Done ==="
echo "GitHub Actions will build and create the release."
echo "Watch: https://github.com/<owner>/<repo>/actions"
