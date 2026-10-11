package com.snown.watchinstaller.adb

import com.snown.watchinstaller.logic.InstallOutput
import com.snown.watchinstaller.logic.InstallResult
import io.github.muntashirakon.adb.AdbStream
import java.io.IOException
import java.io.InputStream

/**
 * `adb install -r` 과 같은 일을 한다: 워치의 `cmd package install -r -S <크기>` 에 APK 바이트를 그대로 흘려보낸다.
 * (adb 의 "streamed install" 방식 — Android 7+ 에서 동작, Wear OS 3+ 워치는 모두 해당)
 */
object ApkInstaller {

    /**
     * @param onSent 보낸 바이트 수 (0..size). 다 보내면 워치가 설치를 시작한다.
     */
    fun install(
        adb: AdbManager,
        apk: InputStream,
        size: Long,
        onSent: (Long) -> Unit,
    ): InstallResult {
        adb.openStream("exec:cmd package install -r -S $size").use { stream ->
            val out = stream.openOutputStream()
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
                val partial = readAvailable(stream)
                if (partial.contains("Failure")) return InstallResult.Failure(partial.trim())
                throw e
            }
            if (sent != size) throw IOException("File size changed while sending ($sent / $size)")
            // libadb 에는 출력 쪽만 닫는(half-close) 방법이 없다 — 크기(-S)를 알려 줬으니 워치는 다 받으면 알아서 설치한다.
            return awaitResult(stream)
        }
    }

    /**
     * 워치가 결과 줄을 쓸 때까지 기다린다. 결과를 쓰고 스트림을 닫았는데도 libadb 가 EOF 를 못 알려 줄 때가 있어
     * (읽기 대기 중 닫힘이 '보류'로 남는 경우) 블로킹 read 대신 짧게 나눠 확인하고, 결과 줄이 보이면 바로 끝낸다.
     */
    private fun awaitResult(stream: AdbStream): InstallResult {
        val input = stream.openInputStream()
        val got = StringBuilder()
        val buf = ByteArray(64 * 1024)
        val startedAt = System.currentTimeMillis()
        while (true) {
            val n = try {
                when {
                    stream.isClosed -> -1
                    input.available() > 0 -> input.read(buf)
                    else -> 0
                }
            } catch (_: IOException) {
                -1
            }
            if (n > 0) got.append(buf.decodeToString(0, n))
            InstallOutput.parse(got.toString(), ended = n < 0)?.let { return it }
            if (InstallOutput.timedOut(startedAt, System.currentTimeMillis())) return InstallResult.NoAnswer
            if (n == 0) Thread.sleep(POLL_MS)
        }
    }

    /** 막히지 않고 지금 와 있는 출력만 읽는다. */
    private fun readAvailable(stream: AdbStream): String = runCatching {
        val input = stream.openInputStream()
        val got = StringBuilder()
        val buf = ByteArray(64 * 1024)
        while (!stream.isClosed && input.available() > 0) {
            val n = input.read(buf)
            if (n <= 0) break
            got.append(buf.decodeToString(0, n))
        }
        got.toString()
    }.getOrDefault("")

    private const val POLL_MS = 100L
}
