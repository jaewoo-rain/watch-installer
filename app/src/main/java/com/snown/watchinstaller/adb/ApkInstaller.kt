package com.snown.watchinstaller.adb

import java.io.IOException
import java.io.InputStream

/**
 * `adb install -r` 과 같은 일을 한다: 워치의 `cmd package install -r -S <크기>` 에 APK 바이트를 그대로 흘려보낸다.
 * (adb 의 "streamed install" 방식 — Android 7+ 에서 동작, Wear OS 3+ 워치는 모두 해당)
 */
object ApkInstaller {

    sealed interface Result {
        data object Success : Result
        /** 워치가 돌려준 원문(예: "Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: ...]") */
        data class Failure(val output: String) : Result
    }

    /**
     * @param onSent 보낸 바이트 수 (0..size). 다 보내면 워치가 설치를 시작한다.
     */
    fun install(
        adb: AdbManager,
        apk: InputStream,
        size: Long,
        onSent: (Long) -> Unit,
    ): Result {
        adb.openStream("exec:cmd package install -r -S $size").use { stream ->
            val out = stream.openOutputStream()
            val input = stream.openInputStream()
            var sent = 0L
            try {
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = apk.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    sent += n
                    onSent(sent)
                }
                out.flush()
            } catch (e: IOException) {
                // 저장공간 부족 등으로 워치가 먼저 거절하면 스트림이 닫힌다 — 남긴 메시지가 있으면 그걸 보여 준다.
                val partial = runCatching { input.readBytes().decodeToString() }.getOrDefault("")
                if (partial.contains("Failure")) return Result.Failure(partial)
                throw e
            }
            if (sent != size) throw IOException("파일 크기가 바뀌었어요 ($sent / $size)")
            // 워치가 설치를 끝내면 결과를 한 줄 쓰고 스트림을 닫는다.
            val output = input.readBytes().decodeToString().trim()
            return if (output.contains("Success")) Result.Success else Result.Failure(output)
        }
    }
}
