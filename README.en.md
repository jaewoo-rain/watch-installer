[한국어](README.md) | **English**

# Watch Installer

An Android app that **installs APKs on a Wear OS watch from your phone**.
No PC needed: the phone connects to the watch over Wireless debugging and installs a watch APK that's on the phone.
The screen can be switched between 한글 and English at the top right.

## Install (put this app on your phone)

It's not on any store. Clone this repository, build it, and install it on your phone yourself. Pick whichever method you prefer.

First, enable USB debugging on the **phone**: Settings → About phone → Software information → tap **Build number** 7 times → Settings → Developer options → turn on **USB debugging**.

### Method 1. Build and install with Android Studio

1. Install and launch [Android Studio](https://developer.android.com/studio).
2. Choose **File → Open** and select this repository folder (`watch-installer`). If "Trust Project?" appears, click **Trust Project**.
3. Wait until the **Gradle Sync** progress at the bottom of the window finishes (the first time takes a few minutes). If it asks you to install an SDK, go ahead and install it.
4. Connect the phone to the PC with a USB cable. When the phone asks "Allow USB debugging?", tap **Allow**.
5. In the toolbar at the top, make sure the run configuration is **app**, and pick **your phone** in the device list next to it.
6. Click the green **▶ (Run 'app')**. When the build finishes, "Watch Installer" is installed on the phone and opens.

### Method 2. Build and install from a terminal

You need the Android SDK and `adb` (`ANDROID_HOME`, or `sdk.dir` in `local.properties`).

```powershell
cd watch-installer
.\gradlew.bat assembleDebug        # macOS/Linux: ./gradlew assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Prepare (watch)

1. **Turn on Developer options** — on the watch, Settings → About watch → Software information → tap **Software version** 7 times.
2. **Turn on debugging** — Settings → Developer options → turn on **ADB debugging** and **Wireless debugging**.

### Connect to the same Wi-Fi

Connect the phone and the watch to the **same Wi-Fi**. If there's no router, or the Wi-Fi blocks devices from talking to each other (guest, office, or school networks), turn on the phone's **mobile hotspot** and connect the watch to that hotspot.

- Watch Wi-Fi: Settings → Connections → Wi-Fi (being connected only over Bluetooth isn't enough — turn on Wi-Fi and connect).

## Usage

Coming soon with screenshots.

---

Developer notes (build setup, libraries, code structure, limitations) are in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) (Korean).
License: [Apache-2.0](LICENSE).
