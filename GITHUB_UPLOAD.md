# Upload this codebase to GitHub

1. Unzip `PleosFriends-GitHub-Final.zip` and open the resulting `PleosFriends` folder.
2. In GitHub Desktop, choose **File → Add Local Repository** and select that folder. If it is not a Git repository yet, use the offered **create a repository here** option with this folder as the repository location.
3. Review and commit the project files, then select **Publish repository**. Choose your repository name and visibility.
4. On GitHub, confirm the repository root contains `README.md`, `android/`, and `.github/workflows/android.yml`. The archive itself is a delivery package; the extracted files are the codebase.
5. Open **Actions → Pleos Android** to see the build. A successful run provides a `pleos-friends-apks` artifact containing the development APKs. Initial dependency downloads require internet access.

If using GitHub's browser upload, upload the extracted project contents, including the hidden `.github` folder. Do not upload the ZIP alone. On macOS, Command–Shift–Period reveals hidden files in Finder.

## Run locally

Open the **`android` folder in Android Studio**, choose the **demoDebug** variant, and run it on a parked **Pleos Connect 2.1.0 / API 34 Automotive** emulator. Use JDK 21 and install Android SDK 35. Android Studio creates your local SDK settings automatically; they are deliberately excluded from this package.

For terminal builds on macOS/Linux:

```sh
cd android
bash ./gradlew :app:assembleDemoDebug :app:assembleConnectDebug
```

On Windows, run `gradlew.bat` from the `android` directory instead.

The separate Busan test case is `android/scenarios/busan-roadtrip.json`. Run `python3 android/scripts/record_busan.py` from the repository root to reproduce the native walkthrough. See the main README for setup and options.

This package contains the final native Android source, fonts, photographs, offline maps and map source data, demo audio, Gradle Wrapper, automated tests, recording scripts, and GitHub Actions workflow. Build outputs, recordings, caches, machine-specific settings and signing keys are excluded. GitHub Pages does not run native Android apps.
