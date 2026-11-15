# Pleos Friends — Native Android demo

**따로 타도, 함께 가는 여행.** Kotlin / Jetpack Compose app for Hyundai **Pleos Connect**, implementing Parts 1–3 of the supplied videos and the separate five-scene Busan road-trip story.

## Open and run

Open **`android/`** in Android Studio. Select **`demoDebug`** and run on a **Pleos Connect 2.1.0 / API 34 Automotive** emulator. Cold Boot, 4 GB RAM, and a parked vehicle are recommended. The UI occupies the app window; Pleos supplies Driving View, the system status bar and the dock.

Build requirements: JDK **21** recommended (17 also supported), Gradle Wrapper **8.13**, Android SDK **35**, minimum Android API **28**. No API keys or backend are needed for the demo. Use Android Studio’s Gradle JDK selector if the installed IDE uses Java 25.

```sh
cd android
bash ./gradlew :app:assembleDemoDebug :app:assembleConnectDebug
adb install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk
adb shell am start -n com.pleosfriends.demo/com.pleosfriends.MainActivity
```

Set `sdk.dir=/your/Android/sdk` in an untracked `android/local.properties` if Android Studio has not created it. After installation the demo works offline.

## Record the Busan story immediately

The separate, editable test-case file is **[`android/scenarios/busan-roadtrip.json`](android/scenarios/busan-roadtrip.json)**. It supplies the hotel, lunch, rest area, café, departure addresses, demo phone numbers, three drivers, group name, all voice messages, the deterministic ladder winner, and recording pauses.

With a parked Pleos emulator running:

```sh
python3 android/scripts/record_busan.py
```

The script builds and installs the app and test APK, then drives the actual native UI. **일정 추가** is one address-search field plus **Gleo AI** — no second address box and no stacked extra CTAs. Day 1 is **점심** dragged between 출발 and **부산 호텔**; there is no 카페 미정. Café is added only later from 플픽. It then names the group **대학 동기 부산 원정대**, invites 도현 and 서윤, switches the demo viewer for voice alerts and replies, runs the parked ladder (도현 당첨), adds **해운대 오션카페** from 플픽, and arrives at the hotel with all three cars. Files are written to **`recordings/`**:

* `PleosFriends-Busan-Demo.mp4` — 1920×1080 native emulator recording.
* Selected numbered PNG screenshots of the scenes.
* `busan-test.log` — assertions and scene markers.

Long recordings are split before Android’s time limit and automatically joined on macOS using the included AVFoundation helper. On Windows/Linux, install `ffmpeg` to join automatically; otherwise the individual MP4 segments are kept.

Android’s `screenrecord` produces a **silent MP4**. Synthetic sample voice audio is bundled in the app and plays live when a message is opened; capture emulator audio separately if an audio track is needed. Fixture coordinates and ETAs are for demonstration, not current traffic guidance.

Useful options:

```sh
python3 android/scripts/record_busan.py --test-only
python3 android/scripts/record_busan.py --test-only --test-class com.pleosfriends.PersonalStopTest
python3 android/scripts/record_busan.py --skip-build
python3 android/scripts/record_busan.py --avd YOUR_PLEOS_AVD --output ./my-recording
python3 android/scripts/record_busan.py --serial emulator-5554
```

If no emulator is running, the script can start `PleosFriends_S17` when that AVD exists, or the AVD passed with `--avd`. It does not change the emulator’s driving restrictions. If the working copy is in cloud-synced storage, keep a local checkout for Gradle builds.

To load the three-car scenario for **manual** recording:

```sh
adb shell am start -n com.pleosfriends.demo/com.pleosfriends.MainActivity --es scenario busan
```

This resets the local demo journey. Enter the values in the JSON or use the automated recording command. A slim **DEMO 화면** strip (예빈 / 도현 / 서윤) sits outside the IVI card so viewer switches are obvious; there are no **장면** overlay chips. Accelerated arrival events stay test-only. A Connect-S-17 **light dock** runs under every screen. Driving View remains vehicle chrome around this 1:2 app pane.

## Implemented flows

**Part 1:** photo-tile home → trip setup → invitations → participation confirmation → departure summary → interactive group map. Default 1박 2일; 2박 3일 creates **three horizontal day timelines stacked vertically**, matching the approved reference. Compact departure/destination and duration controls sit above the timelines. The group screen uses a centered name field, invited-friend cards and a separate “+” tile. Text uses real 600/700/800 font weights throughout the demo. Text and Gleo sample input, itinerary drag-and-drop, reorder/remove, and three invited friends are supported.

**Part 2:** the map stays visible beside the right panel. **전체 여행 일정** shows departure, ordered stops and arrival with clocks and estimated leg durations. **경유지 추가 / 일정 추가** opens the shared live editor. Gleo sample input fills **가평제알휴게소**, which can be dragged before stop 1. The recorded flow closes **전체 여행 일정**, returns to the main map, and opens the right-hand **경유지 추가** control. Placing the new stop opens **수정된 일정**, with the new row highlighted in purple. **내 차만 / 그룹 경유지로** and **응, 음성 메시지로 / 아니** remain visible together. Scope selection does not apply a change; sending the voice message or choosing **아니** applies it. **내 차만** changes only that vehicle’s route and ETA; **그룹 경유지로** changes all member itineraries. Cancelling the voice dialog preserves the editor draft and selected scope. The voice composer expands over the full map, followed by a compact sent-confirmation card. Voice notifications, playback, replies and completion feedback are included. Repeated confirmation IDs cannot duplicate stops.

The map uses detailed offline grayscale road and area renderings for the Seoul metropolitan area and Busan, colored scenario routes, dark ETA/distance labels, a **ring destination**, and a light right-hand action rail with single-line labels (not cramped multi-line dark discs). Geographic source extracts and their regeneration script are included with attribution; no runtime map API key is needed. These are demonstration maps, not live Hyundai map or traffic data. Accent is **`#5A46FA`**, text is Gray/900 **`#131417`**.

**Picks during a trip:** horizontal curation banners, current-location/destination tabs, 전체/카페/식당/여행·명소 filters, 네이버/카카오 source selection, rating/distance/recommendation sorting, photographs, bookmarks, proximity badges, address/hours/rating, and a **small navy Hyundai 128 / 👍 30** pill. The Busan café card is **해운대 오션카페**. **일정 추가** opens the same live itinerary editor with the selected place, without changing the entered destination or group.

**Part 3:** home → destination-region Picks browser covering the app’s two-thirds screen area. Region follows the current destination. Vertical scrolling, horizontal banners, source/sort/category filters, bookmarks, detail views and **🐻 10 visits / 👍 8 recommendations** are present. Ratings are stable demo values so they do not change on every redraw.

**Busan case:** six college friends in three vehicles — **예빈** (고양시 일산), **도현** (서울시 마포구), **서윤** (경기도 수원); group **대학 동기 부산 원정대**; dest **부산 호텔**; **2박 3일**. Five scenes: (1) setup lunch+hotel only, (2) 죽전휴게소 group waypoint + 예빈 voice + 도현/서윤 viewer alerts and 도현 voice reply, (3) parked 사다리 **오늘 점심 쏘기** with 도현 당첨, (4) 플픽 현재 위치 → 카페 → 해운대 오션카페 → group route + 서윤 reply, (5) 여행 일정 shows 점심 · 휴게소 · 카페 · 호텔 and all three cars arrive. Games never change an itinerary.

## Real versus simulated

| Variant | Behavior |
| --- | --- |
| `demoDebug` | Self-contained showcase; fixture voice recognition, local invitations, friend positions, map progression, ETAs, place metrics and group synchronization. No actual SMS is sent and no real vehicle route changes. |
| `connectDebug` | Official Pleos **NaviHelper 3.0.5**, **FusedLocation 1.1.3**, **SpeechToText 3.3.1**, and **TextToSpeech 3.3.1** adapters. STT/TTS use ON_DEVICE. Services and permissions depend on the installed Pleos environment. |

Both observe AAOS `CarUxRestrictionsManager` through an application-scoped policy connection with no Activity reference; the listener survives Activity recreation and is released when the app process ends. This avoids a synchronous Pleos 2.1 listener-unregister deadlock during Activity destruction. Setup editing and games require parking; the recording uses simulated travel progress while the emulator remains parked. No passenger exception to real AAOS restrictions is assumed.

There is **no pickup or Vehicle SDK battery feature**. No LLM SDK, Gleo App Schema, Google Play Services, real SMS, cloud speech upload, live Naver/Kakao POI integration, or actual Hyundai visit analytics is included. Source names are UI demo filters. Sample photos do not verify a venue’s appearance. Festival banners are sample curation, not a live event feed.

The connected adapter sends a route only for the known showcase destination **성수 연무장** and at most two supported waypoints. Placeholder rest stops and unresolved typed locations are withheld from real navigation. The Busan scenario is a demo fixture, not a live vehicle navigation integration. Connected GPS/native ETA status is separately identified; actual SDK service behavior still requires target-device validation.

Travel state, scope-specific stops, notifications, bookmarks and game state are encrypted locally using Android Keystore and survive recreation/relaunch. Real invitation phone inputs and raw audio are not persisted; the JSON contains demo phone fixtures only.

## GitHub

Extract the ZIP, then upload the **contents of the `PleosFriends` folder** to your repository root: `README.md`, `.gitignore`, `.github/`, and `android/`. Do not upload only the ZIP as your codebase, and do not place an extra `PleosFriends` folder above these files. GitHub Desktop or Git will include the hidden `.github` directory automatically. See [`GITHUB_UPLOAD.md`](GITHUB_UPLOAD.md) for the steps.

Push the extracted project contents to GitHub. **Actions → Pleos Android** builds both debug APKs, runs unit tests and Android lint, and publishes the **`pleos-friends-apks`** artifact. Download and unzip it to install. The workflow also supports manual dispatch. No secrets are required. These are development-signed APKs; release signing is separate.

This is a **native Android app**. GitHub Pages does not run APKs in a browser. Older website demo files in the original workspace remain separate and unchanged.

## Validation

```sh
cd android
bash ./gradlew :app:testDemoDebugUnitTest :app:lintDemoDebug :app:lintConnectDebug
# Parked Pleos emulator required:
bash ./gradlew :app:connectedDemoDebugAndroidTest
```

The instrumented suites cover the original Part 1 flow and restoration, all five Busan scenes plus home Picks, the private-stop/English-voice/Picks-cancel flow, and parked ladder completion. Unit tests check three-vehicle startup, ordering, personal/group isolation, idempotency, replies, game separation and filters.

Key source files: `PleosApp.kt` (setup/home), `TravelPanels.kt` (live editor, messages, ladder), `PicksBrowser.kt`, `GroupMap.kt`, `JourneyEngine.kt`, `ConnectServices.kt`, `DrivingPolicy.kt`, `SecureTripStorage.kt`. See [`android/THIRD_PARTY_NOTICES.md`](android/THIRD_PARTY_NOTICES.md) for assets and official references.
