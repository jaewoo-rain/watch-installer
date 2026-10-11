package com.snown.watchinstaller.logic

import java.io.FileNotFoundException

object FileAccess {
    /**
     * 파일을 열다 난 이 예외들은 '이 앱이 못 읽는 파일'이다 — 고른 뒤 같은 이름으로 덮어써 URI 가 무효가 됐거나,
     * 읽기 권한이 사라졌거나, 다른 앱(예: PC 의 `adb push` → com.android.shell) 소유라 미디어 저장소가 막는 경우.
     * 설치 오류가 아니라 '파일을 다시 골라 주세요'로 안내한다.
     */
    fun isUnreadable(e: Throwable): Boolean =
        e is SecurityException || e is FileNotFoundException || e is IllegalArgumentException
}
