package com.snown.watchinstaller.logic

import java.io.EOFException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** 어느 단계에서 난 오류인지 — 같은 예외라도 단계마다 안내가 다르다. */
enum class Stage { PAIR, CONNECT, INSTALL }

/** 실패 원인을 비개발자가 읽을 수 있는 한국어로 바꾼다. */
object FailureMessages {

    /** `cmd package install` 이 돌려준 출력(예: "Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: ...]"). */
    fun forInstallOutput(output: String): String {
        val code = Regex("""\[?(INSTALL_[A-Z_]+)""").find(output)?.groupValues?.get(1)
        return when {
            code == null -> "설치에 실패했어요. 워치 화면을 확인하고 다시 해 주세요.\n(워치 응답: ${output.trim().take(200)})"
            code == "INSTALL_FAILED_UPDATE_INCOMPATIBLE" ||
                code == "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES" ->
                "워치에 다른 서명의 스논이 있어요. 워치에서 스논을 지우고 다시 설치해 주세요."
            code == "INSTALL_FAILED_VERSION_DOWNGRADE" ->
                "워치에 있는 스논이 이 파일보다 새 버전이에요. 워치에서 스논을 지우고 다시 설치해 주세요."
            code == "INSTALL_FAILED_INSUFFICIENT_STORAGE" ->
                "워치 저장공간이 모자라요. 워치에서 안 쓰는 앱을 지우고 다시 해 주세요."
            code == "INSTALL_FAILED_OLDER_SDK" ->
                "워치의 안드로이드 버전이 낮아서 이 파일을 설치할 수 없어요."
            code == "INSTALL_FAILED_NO_MATCHING_ABIS" ->
                "이 파일은 워치 기종에 맞지 않아요. 워치용 APK 가 맞는지 확인해 주세요."
            code == "INSTALL_FAILED_USER_RESTRICTED" || code == "INSTALL_FAILED_ABORTED" ->
                "워치에서 설치가 거부됐어요. 워치 화면에 뜬 확인 창을 허용해 주세요."
            code == "INSTALL_FAILED_TEST_ONLY" ->
                "테스트 전용으로 빌드된 파일이라 설치할 수 없어요. 개발자에게 다시 빌드를 부탁해 주세요."
            code == "INSTALL_FAILED_INVALID_APK" || code.startsWith("INSTALL_PARSE_FAILED") ->
                "APK 파일이 망가졌거나 워치용이 아니에요. 파일을 다시 받아 주세요."
            else -> "설치에 실패했어요 ($code). 워치에서 스논을 지우고 다시 해 보세요."
        }
    }

    fun forError(stage: Stage, e: Throwable): String {
        val name = e.javaClass.simpleName
        val msg = e.message.orEmpty()
        return when {
            name == "AdbPairingRequiredException" ->
                "아직 페어링이 안 됐어요. 1번 페어링부터 해 주세요."
            name == "AdbAuthenticationFailedException" ->
                "워치가 연결을 허락하지 않았어요. 워치 화면에서 '허용'을 눌렀는지 확인하고, 안 되면 1번 페어링을 다시 해 주세요."
            e is UnknownHostException || e is NoRouteToHostException ->
                "워치를 찾지 못했어요. 폰과 워치가 같은 와이파이에 있는지, IP 를 맞게 넣었는지 확인해 주세요."
            e is ConnectException -> when (stage) {
                Stage.PAIR -> "페어링 창에 연결하지 못했어요. 워치의 '새 기기 페어링' 창이 열려 있는지, IP·포트가 맞는지 확인해 주세요."
                else -> "워치에 연결하지 못했어요. 워치에서 무선 디버깅이 켜져 있는지, IP·포트가 맞는지 확인해 주세요. (포트는 무선 디버깅을 켤 때마다 바뀌어요)"
            }
            e is SocketTimeoutException ->
                "워치가 응답하지 않아요. 같은 와이파이인지 확인하고 다시 해 주세요."
            stage == Stage.PAIR && (e is SSLException || msg.contains("pair", ignoreCase = true) ||
                e is EOFException || e is SecurityException) ->
                "페어링에 실패했어요. 6자리 숫자가 맞는지 확인해 주세요. 워치에서 페어링 창을 닫았다 다시 열면 숫자가 바뀌어요."
            e is SSLException ->
                "워치와 안전한 연결을 맺지 못했어요. 1번 페어링을 다시 해 주세요."
            stage == Stage.INSTALL && (e is EOFException || e is SocketException ||
                msg.contains("closed", ignoreCase = true)) ->
                "설치 도중 워치와 연결이 끊겼어요. 2번 연결을 다시 하고 설치해 주세요."
            e is SocketException || e is EOFException || msg.contains("Not connected", ignoreCase = true) ->
                "워치와 연결이 끊겼어요. 2번 연결을 다시 해 주세요."
            else -> "알 수 없는 오류가 났어요: ${msg.ifEmpty { name }}"
        }
    }
}
