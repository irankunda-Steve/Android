#!/usr/bin/env bash
set -euo pipefail

./gradlew --no-daemon :app:assembleDebug :app:copyDebugApkToApkFolder

echo "APK generated in:"
find APK -type f -name "*.apk" -print
