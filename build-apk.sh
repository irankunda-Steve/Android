#!/usr/bin/env bash
set -euo pipefail

KEYSTORE_PATH="${AUDIN_KEYSTORE_PATH:-signing/audin-release.keystore}"
STORE_PASSWORD="${AUDIN_STORE_PASSWORD:-audin123}"
KEY_ALIAS="${AUDIN_KEY_ALIAS:-audin}"
KEY_PASSWORD="${AUDIN_KEY_PASSWORD:-audin123}"

mkdir -p "$(dirname "$KEYSTORE_PATH")"
if [ ! -f "$KEYSTORE_PATH" ]; then
  keytool -genkeypair -v -storetype PKCS12 \
    -keystore "$KEYSTORE_PATH" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 36500 \
    -storepass "$STORE_PASSWORD" -keypass "$KEY_PASSWORD" \
    -dname "CN=Audin, OU=Mobile, O=Audin, L=Kigali, ST=Kigali, C=RW"
fi

./gradlew --no-daemon :app:assembleRelease :app:copyReleaseApkToApkFolder

echo "APK generated in:"
find APK -type f -name "*-release.apk" -print
