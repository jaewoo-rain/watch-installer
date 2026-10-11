**한국어** | [English](README.en.md)

# 워치 설치 도우미

폰에서 **Wear OS 워치에 APK 를 설치**하는 안드로이드 앱이다.
PC 없이도 폰이 워치의 무선 디버깅에 붙어서, 폰에 있는 워치용 APK 를 워치에 깔아 준다.
화면은 한글/English 를 오른쪽 위에서 바꿀 수 있다.

## 설치 (폰에 이 앱 깔기)

스토어에는 없다. 이 저장소를 받아 직접 빌드해서 폰에 설치한다. 두 방법 중 편한 쪽으로 한다.

먼저 **폰**에서 USB 디버깅을 켠다: 설정 → 휴대전화 정보 → 소프트웨어 정보 → **빌드 번호**를 7번 탭 → 설정 → 개발자 옵션 → **USB 디버깅** 켜기.

### 방법 1. Android Studio 로 빌드·설치

1. [Android Studio](https://developer.android.com/studio) 를 설치하고 실행한다.
2. **File → Open** 에서 이 저장소 폴더(`watch-installer`)를 고른다. "Trust Project?" 가 뜨면 **Trust Project**.
3. 창 아래쪽에 **Gradle Sync** 진행 표시가 끝날 때까지 기다린다(처음엔 몇 분 걸린다). SDK 를 설치하라는 안내가 뜨면 그대로 설치한다.
4. 폰을 USB 로 PC 에 연결한다. 폰에 "USB 디버깅을 허용하시겠습니까?" 가 뜨면 **허용**.
5. 창 위쪽 도구 막대에서 실행 구성이 **app** 인지 확인하고, 그 옆 기기 목록에서 **내 폰**을 고른다.
6. 초록색 **▶ (Run 'app')** 를 누른다. 빌드가 끝나면 폰에 "워치 설치 도우미" 가 깔리고 바로 열린다.

### 방법 2. 터미널로 빌드·설치

Android SDK 와 `adb` 가 있어야 한다(`ANDROID_HOME` 또는 `local.properties` 의 `sdk.dir`).

```powershell
cd watch-installer
.\gradlew.bat assembleDebug        # macOS/Linux: ./gradlew assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 준비 (워치)

1. **개발자 옵션 켜기** — 워치 설정 → 워치 정보(또는 '정보') → 소프트웨어 정보 → **소프트웨어 버전**을 7번 탭.
2. **디버깅 켜기** — 설정 → 개발자 옵션 → **ADB 디버깅**, **무선 디버깅** 켜기.

### 같은 Wi-Fi 에 연결하기

폰과 워치를 **같은 Wi-Fi** 에 연결한다. 공유기가 없거나 기기끼리 통신을 막는 Wi-Fi(게스트망·회사망·학교망)면 폰의 **모바일 핫스팟**을 켜고 워치를 그 핫스팟에 연결한다.

- 워치 Wi-Fi: 설정 → 연결 → Wi-Fi (블루투스로만 붙어 있으면 안 된다 — Wi-Fi 를 직접 켜서 연결).

## 사용법

화면 캡처와 함께 추가 예정.

---

개발자용 내용(빌드 구성·라이브러리·코드 구조·한계)은 [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).
라이선스: [Apache-2.0](LICENSE).
