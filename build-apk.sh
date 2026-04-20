#!/usr/bin/env bash
set -euo pipefail

./gradlew --no-daemon :app:assembleRelease :app:copyReleaseApkToApkFolder

echo "APK generated in:"
find APK -type f -name "*-release.apk" -print
