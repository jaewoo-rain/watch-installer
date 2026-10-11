# 개발 노트

사용자용 안내는 [README](../README.md) 에 있다. 이 문서는 코드를 고치거나 빌드 구성을 바꾸는 사람을 위한 것.

- Kotlin + Jetpack Compose(Material3), minSdk 30(Android 11 — 무선 디버깅 페어링이 11부터), compileSdk/targetSdk 36.
- 스토어 배포 안 함 — 직접 빌드해서 설치한다. 디버그 서명으로 충분하다(설치할 워치 앱과 서명이 같을 필요가 없다).

## 빌드·테스트

```powershell
.\gradlew.bat assembleDebug          # 결과: app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat testDebugUnitTest      # JVM 단위 테스트(logic/, i18n/)
# PC 메모리가 빠듯하면
.\gradlew.bat assembleDebug --max-workers=1 "-Dorg.gradle.jvmargs=-Xmx1g"
```

- Android SDK 경로는 `local.properties`(`sdk.dir=...`, 커밋 안 함) 또는 `ANDROID_HOME` 으로 잡는다.
- 화면 미리보기: `ui/InstallerScreen.kt` 아래쪽 `@Preview`(라이트/다크 × 한/영). ViewModel 없이 카드 모양만 그린다.

## 화면 구성

한 장짜리 화면에 단계 카드 4장(페어링 → 연결 → 파일 → 설치).

- 카드 왼쪽 번호 원: 완료 = ✓, 지금 할 단계(아직 안 끝난 첫 단계) = 파랑, 그 뒤 = 회색.
- 1~3단계는 완료되면 요약 한 줄로 접히고, 머리를 누르면 다시 펼친다. 4단계(설치)는 결과 배너를 보여야 해서 접지 않는다.
- 설치 중에는 `LinearProgressIndicator` + 퍼센트, 끝나면 성공(초록)/실패(빨강) 배너.
- 색은 `ui/Theme.kt` 의 고정 팔레트(파랑 강조색 하나 + 회색, 라이트/다크). dynamic color 는 쓰지 않는다.
- 여백은 16dp(바깥·카드 안) / 12dp(요소 사이), 글자는 titleLarge(앱 제목)·titleMedium(카드 제목)·bodyMedium(본문) 세 단계.
- 런처 아이콘은 적응형(`res/mipmap-anydpi-v26/ic_launcher.xml`, round 도 같은 내용)이고 PNG 대체본은 없다(minSdk 30). 전경 `drawable/ic_launcher_foreground.xml` 은 둥근 워치 실루엣 안에 아래 화살표(흰색, 안전 영역 66dp 원 안), 배경 `drawable/ic_launcher_background.xml` 은 강조색 파랑 그라데이션. monochrome(테마 아이콘)은 전경을 그대로 쓴다. README 의 `docs/images/icon.png` 는 같은 좌표를 Java2D 로 원형 마스크·512px 에 그린 미리보기라, 벡터를 고치면 다시 그린다.

## 한글 / English

- 언어 전환은 **Compose 안 언어 객체 + CompositionLocal** 로 했다(`i18n/Texts.kt`, `Ko.kt`, `En.kt`, `ui/InstallerScreen.kt` 의 `LocalTexts`) — 실패·검증 메시지를 Robolectric 없이 JVM 단위 테스트로 두 언어 다 확인할 수 있고, Activity 재시작 없이 즉시 바뀌기 때문.
- 고른 언어는 SharedPreferences(`installer` / `lang`)에 저장한다. 처음 실행은 기기 언어가 한국어면 한글, 아니면 English.
- ViewModel 은 완성된 문자열 대신 `Msg = (Texts) -> String` 을 들고 있어서, 언어를 바꾸면 이미 떠 있는 안내·오류도 바로 바뀐다.
- 런처의 앱 이름만 Android 리소스다(`values/strings.xml` = Wear Installer, `values-ko` = 웨어 인스톨러). 런처는 앱 안 선택이 아니라 기기 언어를 따른다.
- 새 문구를 넣을 때: `Texts` 에 항목을 추가하면 `Ko`·`En` 둘 다 채우기 전엔 컴파일이 안 된다. `TextsTest` 가 영어 문구에 한글이 섞였는지 확인한다.

## 라이브러리 선택: libadb-android

| 후보 | 무선 페어링(SPAKE2) | TLS 연결 | 라이선스 | 판단 |
|---|---|---|---|---|
| `com.github.MuntashirAkon:libadb-android:3.1.1` | O | O | GPL-3.0-or-later **또는** Apache-2.0 (이중) | **채택** |
| `dev.mobile:dadb` | X | X | Apache-2.0 | 탈락 — 무선 디버깅(Android 11+) 페어링·TLS 미지원, 5555 평문 TCP 전용 |

- 워치의 무선 디버깅은 페어링(SPAKE2) + TLS 1.3 연결이 필수라, 둘 다 되는 libadb-android 를 쓴다(App Manager 가 실제로 쓰는 라이브러리).
- **Apache-2.0 쪽을 택해** 쓴다. 이 라이브러리가 끌어오는 의존성: `spake2-android:2.2.1`(**LGPL-3.0**, 순수 Java SPAKE2), `bcprov-jdk15to18`(MIT 계열 Bouncy Castle 라이선스).
  - LGPL 의존성이 하나 있다(라이브러리 README 도 이를 명시). 이 저장소는 소스만 배포하고 의존성은 빌드 때 받아 오므로 LGPL 조건과 충돌하지 않는다. 빌드한 APK 를 남에게 배포한다면 LGPL 고지를 함께 넣는다.
- 배포처: Maven Central 이 아니라 **JitPack** 에만 있다. `settings.gradle.kts` 에서 JitPack 은 `com.github.MuntashirAkon` 그룹에만 쓰도록 제한했다.
- 함께 쓰는 것:
  - `org.conscrypt:conscrypt-android:2.5.3` (Apache-2.0) — 라이브러리가 있으면 자동으로 쓰는 TLS 1.3 공급자. 라이브러리 README 가 원격 adbd 연결에 권장.
  - `org.bouncycastle:bcpkix-jdk15to18:1.81` (Bouncy Castle 라이선스) — 페어링용 자체 서명 인증서 생성. 라이브러리 예제의 `sun-security-android`(GPL-2.0) 대신 썼다.
- 모든 의존성 버전은 고정이다(동적 버전 없음).

## 설치 구현

- 라이브러리에 설치 API 가 따로 없어서, `adb install` 의 "streamed install" 과 같은 방식으로 직접 구현했다: `exec:cmd package install -r -S <크기>` 스트림을 열고 APK 바이트를 흘려보낸 뒤 `Success`/`Failure [...]` 를 읽는다(`adb/ApkInstaller.kt`). `-r` 이라 같은 앱이 있으면 덮어쓴다.
  - 결과는 EOF 까지 기다리지 않는다. libadb 3.1.1 은 읽을 데이터가 남은 채로 원격이 스트림을 닫으면 닫힘을 '보류'로 두는데, 그 데이터를 다 읽은 뒤에도 닫힘으로 바뀌지 않아 블로킹 read 가 영영 안 끝날 수 있다. 그래서 `available()` 로 짧게 나눠 읽다가 `Success`/`Failure` 가 든 줄이 보이면 바로 끝내고(`logic/InstallOutput.kt`), 120초 안에 결과 줄이 없으면 '워치 앱 목록에서 확인' 안내를 띄운다.
  - libadb 에는 출력 쪽만 닫는(half-close) API 가 없다(`AdbOutputStream.close()` 는 flush 만 한다). `-S <크기>` 로 크기를 알려 주므로 워치는 다 받으면 알아서 설치를 시작한다.
- 키 쌍(RSA 2048 + 자체 서명 인증서)은 앱 내부 저장소(`filesDir/adb_key.pk8`, `adb_cert.der`)에 처음 실행 때 만들어 계속 쓴다. 앱을 지우면 워치와 다시 페어링해야 한다.
- 같은 Wi-Fi 에서 워치가 mDNS 로 광고하면(`_adb-tls-pairing._tcp`, `_adb-tls-connect._tcp`) 1·2단계의 IP·포트를 자동으로 채운다(`adb/WatchFinder.kt`). 공유기가 멀티캐스트를 막으면 직접 입력.
- 설치할 파일: 다운로드 폴더의 기본 파일(`ApkSource.DEFAULT_NAME`)을 '모든 파일 접근' 권한이 있으면 자동으로 집고, 없으면 파일 선택기로 고른다. 고른 파일은 persistable URI 권한으로 기억한다.
  - 3단계를 다시 보여 줄 때(앱으로 돌아올 때) 기억한 파일을 미리 열어 본다. 못 열면(`SecurityException`·`FileNotFoundException`·`IllegalArgumentException`) 기억을 지우고 '파일을 다시 골라 주세요'로 되돌린다 — 설치 단계의 오류로 보이지 않게.
  - '모든 파일 접근'이 있으면 URI 보다 같은 이름의 `Download/` 파일을 경로로 직접 읽는 것이 우선이다(미디어 저장소의 소유자 검사를 거치지 않는다).

## 코드 구성

```
app/src/main/java/.../watchinstaller/
  MainActivity.kt          파일 선택기·설정 화면 이동, mDNS 시작/정지
  InstallerViewModel.kt    4단계 상태와 동작, 화면 언어 저장
  adb/AdbManager.kt        libadb 연결 관리자 + 키 생성·보관
  adb/ApkInstaller.kt      스트리밍 설치(cmd package install -S), 결과 줄 대기
  adb/WatchFinder.kt       mDNS 로 워치 IP·포트 찾기(폰 자신은 제외)
  apk/ApkSource.kt         다운로드 폴더의 기본 파일 또는 고른 파일
  i18n/                    화면 문구 한/영(Texts, Ko, En, Lang)
  logic/                   입력 검증·실패 메시지·설치 결과 읽기·표시 형식 (JVM 단위 테스트 대상)
  ui/Theme.kt              고정 팔레트(라이트/다크)
  ui/InstallerScreen.kt    한 장짜리 화면 + 미리보기
```

## 한계

- 폰과 워치가 같은 Wi-Fi 에 있어야 한다. 게스트망·회사망·학교망처럼 기기끼리 통신을 막는 곳에서는 안 된다(mDNS 자동 찾기도 안 됨 → 직접 입력해도 연결 불가). 이럴 땐 폰의 모바일 핫스팟에 워치를 붙인다.
- 워치 무선 디버깅 포트는 켤 때마다 바뀐다. 자동 찾기가 안 되면 매번 새 포트를 넣어야 한다.
- 워치에 **다른 서명**의 같은 앱이 깔려 있으면 덮어쓸 수 없다 → 워치에서 지우고 다시 설치(데이터도 지워진다).
- 기본 파일을 바로 읽으려면 '모든 파일 접근' 권한이 필요하다. 안 주면 파일 고르기로 쓴다. 고른 파일은 기억하지만, 같은 이름으로 다시 받으면 다운로드 앱이 `이름 (1).apk` 처럼 새 파일을 만들 수 있으니 수정시각을 확인한다.
- PC 에서 `adb push` 로 넣은 파일은 미디어 저장소 소유자가 `com.android.shell` 이 되어, 파일 고르기로 골라도 열리지 않는다(`... has no access to content://media/...`). '모든 파일 접근'을 켜거나 PC 탐색기(MTP)로 복사하면 된다.
- 분할 APK(App Bundle 의 split)는 지원하지 않는다 — 워치용 단일 APK 만.
- 디버그 APK 가 크다(약 40MB, 여러 CPU 용 Conscrypt 네이티브 라이브러리 포함).

## 의존성 라이선스

이 저장소는 [Apache License 2.0](../LICENSE). 의존성 라이선스는 위 '라이브러리 선택' 표 참고.
