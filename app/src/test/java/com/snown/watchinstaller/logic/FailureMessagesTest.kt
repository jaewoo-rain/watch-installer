package com.snown.watchinstaller.logic

import com.snown.watchinstaller.i18n.En
import com.snown.watchinstaller.i18n.Ko
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

    private fun install(output: String) = FailureMessages.forInstallOutput(output)(Ko)
    private fun installEn(output: String) = FailureMessages.forInstallOutput(output)(En)
    private fun error(stage: Stage, e: Throwable) = FailureMessages.forError(stage, e)(Ko)
    private fun errorEn(stage: Stage, e: Throwable) = FailureMessages.forError(stage, e)(En)

    @Test fun signature_mismatch() {
        val m = install("Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package com.example.app signatures do not match previously installed version; ignoring!]")
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
        assertTrue(error(Stage.CONNECT, AdbPairingRequiredException()).contains("페어링부터"))
    }

    @Test fun watch_did_not_allow() {
        assertTrue(error(Stage.CONNECT, AdbAuthenticationFailedException()).contains("허용"))
    }

    @Test fun connection_refused_by_stage() {
        assertTrue(error(Stage.PAIR, ConnectException()).contains("새 기기 페어링"))
        assertTrue(error(Stage.CONNECT, ConnectException()).contains("무선 디버깅이 켜져"))
    }

    @Test fun not_found_and_timeout() {
        assertTrue(error(Stage.CONNECT, UnknownHostException()).contains("같은 Wi-Fi"))
        assertTrue(error(Stage.CONNECT, SocketTimeoutException()).contains("응답하지"))
    }

    @Test fun connection_failures_suggest_hotspot() {
        listOf(
            error(Stage.CONNECT, UnknownHostException()),
            error(Stage.CONNECT, ConnectException()),
            error(Stage.PAIR, ConnectException()),
            error(Stage.CONNECT, SocketTimeoutException()),
        ).forEach { assertTrue(it, it.contains("같은 Wi-Fi") && it.contains("핫스팟")) }
    }

    @Test fun wrong_pairing_code() {
        assertTrue(error(Stage.PAIR, SSLHandshakeException("x")).contains("6자리"))
        assertTrue(error(Stage.PAIR, EOFException()).contains("6자리"))
    }

    @Test fun dropped_during_install() {
        assertTrue(error(Stage.INSTALL, SocketException("Connection reset")).contains("설치 도중"))
        assertTrue(error(Stage.INSTALL, IOException("Stream closed")).contains("설치 도중"))
    }

    @Test fun not_connected() {
        assertTrue(error(Stage.CONNECT, IOException("Not connected to ADB.")).contains("2번 연결"))
    }

    // ---- English ----

    @Test fun en_install_outputs() {
        assertTrue(installEn("Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: x]").contains("different key"))
        assertTrue(installEn("Failure [INSTALL_FAILED_VERSION_DOWNGRADE]").contains("newer"))
        assertTrue(installEn("Failure [INSTALL_FAILED_INSUFFICIENT_STORAGE]").contains("storage"))
        assertTrue(installEn("Failure [INSTALL_FAILED_USER_RESTRICTED]").contains("Allow"))
        assertTrue(installEn("Failure [INSTALL_PARSE_FAILED_NOT_APK]").contains("damaged"))
        assertTrue(installEn("Failure [INSTALL_FAILED_OLDER_SDK]").contains("Android version"))
        assertTrue(installEn("Failure [INSTALL_FAILED_NO_MATCHING_ABIS]").contains("hardware"))
        assertTrue(installEn("Failure [INSTALL_FAILED_TEST_ONLY]").contains("test-only"))
    }

    @Test fun en_unknown_and_raw() {
        assertTrue(installEn("Failure [INSTALL_FAILED_SOMETHING_NEW]").contains("INSTALL_FAILED_SOMETHING_NEW"))
        assertTrue(installEn("cmd: Can't find service: package").contains("Can't find service"))
    }

    @Test fun en_connection_errors() {
        assertTrue(errorEn(Stage.CONNECT, AdbPairingRequiredException()).contains("Not paired"))
        assertTrue(errorEn(Stage.CONNECT, AdbAuthenticationFailedException()).contains("Allow"))
        assertTrue(errorEn(Stage.PAIR, ConnectException()).contains("Pair new device"))
        assertTrue(errorEn(Stage.CONNECT, ConnectException()).contains("Wireless debugging is on"))
        assertTrue(errorEn(Stage.PAIR, SSLHandshakeException("x")).contains("6-digit"))
        assertTrue(errorEn(Stage.INSTALL, IOException("Stream closed")).contains("during installation"))
        assertTrue(errorEn(Stage.CONNECT, IOException("Not connected to ADB.")).contains("step 2"))
        assertTrue(errorEn(Stage.CONNECT, IllegalStateException("boom")).contains("boom"))
    }

    @Test fun en_connection_failures_suggest_hotspot() {
        listOf(
            errorEn(Stage.CONNECT, UnknownHostException()),
            errorEn(Stage.CONNECT, ConnectException()),
            errorEn(Stage.PAIR, ConnectException()),
            errorEn(Stage.CONNECT, SocketTimeoutException()),
        ).forEach { assertTrue(it, it.contains("same Wi-Fi") && it.contains("hotspot")) }
    }
}
