# Audin

Audin is a Kotlin-based Android voice recorder app with practical everyday features:

- 🎙️ One-tap start/stop recording
- 🔎 Search through saved recordings
- ▶️ In-app playback
- ✏️ Rename recordings
- 📤 Share recordings
- 📥 Export recordings to `Music/Audin`
- 🗑️ Delete recordings

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- `MediaRecorder` + `MediaPlayer`

## Build locally

```bash
./gradlew :app:assembleDebug
```

## CI

GitHub Actions workflow is available at `.github/workflows/android.yml` and will run build + tests on pushes and PRs.


## Download APK from CI

After a successful GitHub Actions run, open the run summary and download either `audin-debug-apk` (debug build) or `audin-release-apk` (release build) artifact.

Paths uploaded by CI: `APK/*-debug.apk` and `APK/*-release.apk`.


## APK folder for phone install

After running a release build, APK files are copied to the repository root `APK/` folder.

> Note: `APK/` is generated/populated by the build. It will be empty before you run the build command.

Build command:

```bash
./build-apk.sh
```

Then install the APK from `APK/` on your phone.


## Downloadable app

To download without opening raw build logs:

1. Go to **Actions** and open the latest successful `Android CI` run to download `audin-release-apk` artifact.
2. On pushes to `main`, CI also updates a prerelease named **Audin Latest APK** with the installable release APK attached.


## Installation note

Install `app-release.apk` (signed). Do **not** install `app-release-unsigned.apk` or `.dm` files.

If installation still fails, uninstall older Audin builds first (signature mismatch can block updates from differently signed APKs).



## Signing note

No binary keystore is committed to git. `build-apk.sh` (and CI) generates `signing/audin-release.keystore` when missing.

You can override signing settings with env vars: `AUDIN_KEYSTORE_PATH`, `AUDIN_STORE_PASSWORD`, `AUDIN_KEY_ALIAS`, `AUDIN_KEY_PASSWORD`.


Debug APK artifact name: `audin-debug-apk`.
