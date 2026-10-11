# 워치 설치 도우미 (watch-installer)

폰에서 **워치용 APK 를 갤럭시 워치(Wear OS)에 설치**하는 안드로이드 앱이다. 원래 스논(스키·보드 앱) 워치 앱을 설치하려고 만들었고, 어떤 Wear OS 단일 APK 든 설치할 수 있다.
PC 가 워치와 같은 Wi-Fi 에 없어 `adb` 로 직접 붙을 수 없을 때, 폰이 대신 워치에 무선 디버깅으로 붙어 설치한다.
제3자 앱(Wear Installer 2)에서 필요한 기능만 남기고 한국어로 만든 것.

- 패키지: `com.snown.watchinstaller`. 스토어 배포 안 함 — 직접 빌드해서 설치한다.
- Kotlin + Jetpack Compose(Material3), minSdk 30(Android 11 — 무선 디버깅 페어링이 11부터).

## 화면 (한 장, 위에서 아래로)

1. **페어링 (처음 한 번)** — 워치 '새 기기 페어링' 화면의 IP·포트·6자리 코드 입력
2. **워치 연결** — 워치 무선 디버깅 첫 화면의 IP·포트 입력 (마지막 값 기억)
3. **설치할 파일** — `Download/snown-watch.apk` 자동 선택, 없으면 파일 고르기
4. **워치에 설치** — 보내는 중(%) → 설치 중 → 결과. 실패하면 원인별 한국어 안내

같은 Wi-Fi 에서 워치가 mDNS 로 광고하면 1·2번의 IP·포트를 자동으로 채운다(공유기가 막으면 직접 입력).

## 빌드 → 폰에 설치

```powershell
cd watch-installer
.\gradlew.bat assembleDebug
# 결과: app\build\outputs\apk\debug\app-debug.apk
adb install -r app\build\outputs\apk\debug\app-debug.apk   # 폰을 USB 로 PC 에 연결한 상태
```

- 디버그 서명으로 충분하다. 설치할 워치 앱과 서명이 같을 필요가 없다.
- Android SDK 경로는 `local.properties`(`sdk.dir=...`, 커밋 안 함) 또는 `ANDROID_HOME` 으로 잡는다.
- 단위 테스트: `.\gradlew.bat testDebugUnitTest`
- PC 메모리가 빠듯하면: `.\gradlew.bat assembleDebug --max-workers=1 "-Dorg.gradle.jvmargs=-Xmx1g"`

## 사용 순서

### 처음 한 번: 워치 개발자 옵션 켜기
1. 워치 **설정 → 워치 정보(또는 '정보') → 소프트웨어 정보** 에서 **소프트웨어 버전**을 7번 연속 탭 → "개발자 모드가 켜졌습니다".
2. **설정 → 개발자 옵션** 에서 **ADB 디버깅** 켜기, **무선 디버깅** 켜기.
3. 폰과 워치가 **같은 Wi-Fi** 인지 확인(워치가 블루투스로만 붙어 있으면 안 된다 — 워치 설정에서 Wi-Fi 를 직접 켜서 연결).

### 처음 한 번: 페어링
1. 워치 **개발자 옵션 → 무선 디버깅 → 새 기기 페어링** 을 누른다. 화면에 IP:포트와 6자리 숫자가 뜬다.
2. 이 앱 1번 칸에 IP, 포트, 6자리 숫자를 넣고 **페어링**. (IP 칸에 `192.168.0.12:37123` 처럼 통째로 붙여넣어도 나눠 들어간다)
3. "페어링 완료" 가 뜨면 끝. 다음부터는 건너뛴다(앱을 지우거나 워치를 초기화하면 다시).

### 매번: 연결 → 설치
1. 워치 무선 디버깅 첫 화면의 **IP 주소 및 포트** 를 2번 칸에 넣고 **연결**. 포트는 무선 디버깅을 켤 때마다 바뀐다.
2. PC 에서 만든 워치 APK 를 폰의 `Download/snown-watch.apk` 로 옮겨 두면 3번에 자동으로 잡힌다(카톡·드라이브로 받은 경우 이름을 맞추거나 **파일 고르기**).
   - 자동으로 잡으려면 한 번 **다운로드 폴더 자동 읽기 허용**(모든 파일 접근)을 켜 둔다. 끄고 써도 파일 고르기는 된다.
3. **설치**. 워치에 있는 스논을 덮어쓴다(`adb install -r` 과 같다).
4. 끝나면 워치의 **무선 디버깅을 끈다**(배터리).

## 라이브러리 선택: libadb-android

| 후보 | 무선 페어링(SPAKE2) | TLS 연결 | 라이선스 | 판단 |
|---|---|---|---|---|
| `com.github.MuntashirAkon:libadb-android:3.1.1` | O | O | GPL-3.0-or-later **또는** Apache-2.0 (이중) | **채택** |
| `dev.mobile:dadb` | X | X | Apache-2.0 | 탈락 — 무선 디버깅(Android 11+) 페어링·TLS 미지원, 5555 평문 TCP 전용 |

- 워치의 무선 디버깅은 페어링(SPAKE2) + TLS 1.3 연결이 필수라, 둘 다 되는 libadb-android 를 쓴다(App Manager 가 실제로 쓰는 라이브러리).
- **Apache-2.0 쪽을 택해** 쓴다. 이 라이브러리가 끌어오는 의존성: `spake2-android:2.2.1`(**LGPL-3.0**, 순수 Java SPAKE2), `bcprov-jdk15to18`(MIT 계열 Bouncy Castle 라이선스).
  - LGPL 의존성이 하나 있다(라이브러리 README 도 이를 명시). 이 저장소는 소스만 배포하고 의존성은 빌드 때 받아 오므로 LGPL 조건과 충돌하지 않는다. 빌드한 APK 를 남에게 배포한다면 LGPL 고지를 함께 넣는다.
- 설치 API 는 따로 없어서, `adb install` 의 "streamed install" 과 같은 방식으로 직접 구현했다: `exec:cmd package install -r -S <크기>` 스트림을 열고 APK 바이트를 흘려보낸 뒤 `Success`/`Failure [...]` 를 읽는다(`adb/ApkInstaller.kt`).
- 배포처: Maven Central 이 아니라 **JitPack** 에만 있다. `settings.gradle.kts` 에서 JitPack 은 `com.github.MuntashirAkon` 그룹에만 쓰도록 제한했다.
- 함께 쓰는 것:
  - `org.conscrypt:conscrypt-android:2.5.3` (Apache-2.0) — 라이브러리가 있으면 자동으로 쓰는 TLS 1.3 공급자. 라이브러리 README 가 원격 adbd 연결에 권장.
  - `org.bouncycastle:bcpkix-jdk15to18:1.81` (Bouncy Castle 라이선스) — 페어링용 자체 서명 인증서 생성. 라이브러리 예제의 `sun-security-android`(GPL-2.0) 대신 썼다.
- 키 쌍(RSA 2048 + 자체 서명 인증서)은 앱 내부 저장소(`filesDir/adb_key.pk8`, `adb_cert.der`)에 처음 실행 때 만들어 계속 쓴다. 앱을 지우면 워치와 다시 페어링해야 한다.
- 모든 의존성 버전은 고정이다(동적 버전 없음).

## 코드 구성

```
app/src/main/java/com/snown/watchinstaller/
  MainActivity.kt          파일 선택기·설정 화면 이동, mDNS 시작/정지
  InstallerViewModel.kt    4단계 상태와 동작
  adb/AdbManager.kt        libadb 연결 관리자 + 키 생성·보관
  adb/ApkInstaller.kt      스트리밍 설치(cmd package install -S)
  adb/WatchFinder.kt       mDNS 로 워치 IP·포트 찾기(폰 자신은 제외)
  apk/ApkSource.kt         Download/snown-watch.apk 또는 고른 파일
  logic/                   입력 검증·실패 메시지·표시 형식 (JVM 단위 테스트 대상)
  ui/InstallerScreen.kt    한 장짜리 화면
```

## 한계

- 폰과 워치가 같은 Wi-Fi 에 있어야 한다. 게스트망·회사망처럼 기기끼리 통신을 막는 곳에서는 안 된다(mDNS 자동 찾기도 안 됨 → 직접 입력해도 연결 불가).
- 워치 무선 디버깅 포트는 켤 때마다 바뀐다. 자동 찾기가 안 되면 매번 새 포트를 넣어야 한다.
- 워치에 **다른 서명**의 스논이 깔려 있으면 덮어쓸 수 없다 → 워치에서 지우고 다시 설치(데이터도 지워진다).
- 기본 파일(`Download/snown-watch.apk`)을 바로 읽으려면 '모든 파일 접근' 권한이 필요하다. 안 주면 파일 고르기로 쓴다. 고른 파일은 기억하지만, 같은 이름으로 다시 받으면 다운로드 앱이 `snown-watch (1).apk` 처럼 새 파일을 만들 수 있으니 수정시각을 확인한다.
- 분할 APK(App Bundle 의 split)는 지원하지 않는다 — 워치용 단일 APK 만.
- 디버그 APK 가 크다(약 40MB, 여러 CPU 용 Conscrypt 네이티브 라이브러리 포함).

## 라이선스

[Apache License 2.0](LICENSE). 의존성 라이선스는 위 '라이브러리 선택' 참고.
