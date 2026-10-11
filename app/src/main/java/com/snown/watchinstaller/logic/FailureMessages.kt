package com.snown.watchinstaller.logic

import com.snown.watchinstaller.i18n.Msg
import java.io.EOFException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** 어느 단계에서 난 오류인지 — 같은 예외라도 단계마다 안내가 다르다. */
enum class Stage { PAIR, CONNECT, INSTALL }

/** 실패 원인을 비개발자가 읽을 수 있는 안내([Msg] — 한/영 어느 쪽으로도 꺼낼 수 있다)로 바꾼다. */
object FailureMessages {

    /** `cmd package install` 이 돌려준 출력(예: "Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: ...]"). */
    fun forInstallOutput(output: String): Msg {
        val code = Regex("""\[?(INSTALL_[A-Z_]+)""").find(output)?.groupValues?.get(1)
        return { t ->
            when {
                code == null -> t.installNoCode(output.trim().take(200))
                code == "INSTALL_FAILED_UPDATE_INCOMPATIBLE" ||
                    code == "INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES" -> t.installSignature
                code == "INSTALL_FAILED_VERSION_DOWNGRADE" -> t.installDowngrade
                code == "INSTALL_FAILED_INSUFFICIENT_STORAGE" -> t.installStorage
                code == "INSTALL_FAILED_OLDER_SDK" -> t.installOlderSdk
                code == "INSTALL_FAILED_NO_MATCHING_ABIS" -> t.installAbi
                code == "INSTALL_FAILED_USER_RESTRICTED" || code == "INSTALL_FAILED_ABORTED" -> t.installRejected
                code == "INSTALL_FAILED_TEST_ONLY" -> t.installTestOnly
                code == "INSTALL_FAILED_INVALID_APK" || code.startsWith("INSTALL_PARSE_FAILED") -> t.installBrokenApk
                else -> t.installOther(code)
            }
        }
    }

    fun forError(stage: Stage, e: Throwable): Msg {
        val name = e.javaClass.simpleName
        val msg = e.message.orEmpty()
        return { t ->
            when {
                name == "AdbPairingRequiredException" -> t.errPairingRequired
                name == "AdbAuthenticationFailedException" -> t.errAuthFailed
                e is UnknownHostException || e is NoRouteToHostException -> t.errNotFound
                e is ConnectException -> if (stage == Stage.PAIR) t.errPairRefused else t.errConnectRefused
                e is SocketTimeoutException -> t.errTimeout
                stage == Stage.PAIR && (e is SSLException || msg.contains("pair", ignoreCase = true) ||
                    e is EOFException || e is SecurityException) -> t.errPairFailed
                e is SSLException -> t.errTls
                stage == Stage.INSTALL && (e is EOFException || e is SocketException ||
                    msg.contains("closed", ignoreCase = true)) -> t.errDroppedDuringInstall
                e is SocketException || e is EOFException || msg.contains("Not connected", ignoreCase = true) ->
                    t.errDropped
                else -> t.errUnknown(msg.ifEmpty { name })
            }
        }
    }
}
