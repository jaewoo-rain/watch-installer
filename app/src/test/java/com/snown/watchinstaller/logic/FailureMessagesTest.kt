package com.snown.watchinstaller.logic

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class FailureMessagesTest {

    private fun install(output: String) = FailureMessages.forInstallOutput(output)

    @Test fun signature_mismatch() {
        val m = install("Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package com.snown.app signatures do not match previously installed version; ignoring!]")
        assertTrue(m, m.contains("다른 서명") && m.contains("지우고"))
        assertTrue(install("Failure [INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES]").contains("다른 서명"))
    }

    @Test fun version_downgrade() {
        assertTrue(install("Failure [INSTALL_FAILED_VERSION_DOWNGRADE]").contains("새 버전"))
    }

    @Test fun storage() {
        assertTrue(install("Failure [INSTALL_FAILED_INSUFFICIENT_STORAGE]").contains("저장공간"))
    }

    @Test fun user_rejected() {
        assertTrue(install("Failure [INSTALL_FAILED_USER_RESTRICTED: Install canceled by user]").contains("허용"))
    }

    @Test fun broken_apk() {
        assertTrue(install("Failure [INSTALL_PARSE_FAILED_NOT_APK: Failed to parse]").contains("망가졌"))
        assertTrue(install("Failure [INSTALL_FAILED_INVALID_APK]").contains("망가졌"))
    }

    @Test fun older_sdk_and_abi() {
        assertTrue(install("Failure [INSTALL_FAILED_OLDER_SDK]").contains("안드로이드 버전"))
        assertTrue(install("Failure [INSTALL_FAILED_NO_MATCHING_ABIS]").contains("기종"))
    }

    @Test fun unknown_code_keeps_code() {
        assertTrue(install("Failure [INSTALL_FAILED_SOMETHING_NEW]").contains("INSTALL_FAILED_SOMETHING_NEW"))
    }

    @Test fun no_code_shows_raw_output() {
        assertTrue(install("cmd: Can't find service: package").contains("Can't find service"))
    }

    // 라이브러리 예외는 클래스 이름으로 판단한다 — 같은 이름의 가짜로 확인
    private class AdbPairingRequiredException : Exception()
    private class AdbAuthenticationFailedException : RuntimeException()

    @Test fun pairing_required() {
        assertTrue(FailureMessages.forError(Stage.CONNECT, AdbPairingRequiredException()).contains("페어링부터"))
    }

    @Test fun watch_did_not_allow() {
        assertTrue(FailureMessages.forError(Stage.CONNECT, AdbAuthenticationFailedException()).contains("허용"))
    }

    @Test fun connection_refused_by_stage() {
        assertTrue(FailureMessages.forError(Stage.PAIR, ConnectException()).contains("새 기기 페어링"))
        assertTrue(FailureMessages.forError(Stage.CONNECT, ConnectException()).contains("무선 디버깅이 켜져"))
    }

    @Test fun not_found_and_timeout() {
        assertTrue(FailureMessages.forError(Stage.CONNECT, UnknownHostException()).contains("같은 와이파이"))
        assertTrue(FailureMessages.forError(Stage.CONNECT, SocketTimeoutException()).contains("응답하지"))
    }

    @Test fun wrong_pairing_code() {
        assertTrue(FailureMessages.forError(Stage.PAIR, SSLHandshakeException("x")).contains("6자리"))
        assertTrue(FailureMessages.forError(Stage.PAIR, EOFException()).contains("6자리"))
    }

    @Test fun dropped_during_install() {
        assertTrue(FailureMessages.forError(Stage.INSTALL, SocketException("Connection reset")).contains("설치 도중"))
        assertTrue(FailureMessages.forError(Stage.INSTALL, IOException("Stream closed")).contains("설치 도중"))
    }

    @Test fun not_connected() {
        assertTrue(FailureMessages.forError(Stage.CONNECT, IOException("Not connected to ADB.")).contains("2번 연결"))
    }
}
