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

After a successful GitHub Actions run, open the run summary and download the `audin-debug-apk` artifact.

Path uploaded by CI: `app/build/outputs/apk/debug/*.apk`.
