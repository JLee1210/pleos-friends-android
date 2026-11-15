# Final package verification — 2026-11-15

This source package matches the final native Android demo delivered in this session, including detailed offline maps, the departure-summary redesign, the main-map waypoint entry flow, the revised schedule controls, and the full-map voice/completion dialogs.

A fresh extraction of this ZIP was built without local.properties or project build caches. The SDK was supplied through ANDROID_HOME; JDK 21 was used.

Verified command (from android/):

```sh
bash ./gradlew :app:testDemoDebugUnitTest :app:assembleDemoDebug :app:assembleConnectDebug :app:assembleDemoDebugAndroidTest :app:lintDemoDebug :app:lintConnectDebug --console=plain
```

Result: BUILD SUCCESSFUL — all 127 tasks executed; 10 unit tests passed; no lint errors in either variant. Both native APK variants and the demo test APK built successfully.

The latest five-scene Busan journey was also recorded and passed on a parked Pleos Connect 2.1.0 / API 34 emulator. The final video was 3 minutes 33 seconds, silent. Videos and compiled APKs are separate review/build artifacts and are deliberately not included in this source archive.

The ZIP includes the Gradle Wrapper, application source, resources and fonts, geographic source extracts and attribution, scenarios, automated tests, recording scripts, and the GitHub Actions build workflow. It excludes machine-specific settings, caches, build products, private signing keys, recordings and Git history.

SOURCE_MANIFEST.sha256 records the SHA-256 of each delivered source/documentation file, excluding the manifest itself. On macOS, run `shasum -a 256 -c SOURCE_MANIFEST.sha256` inside the extracted PleosFriends folder to verify it.
