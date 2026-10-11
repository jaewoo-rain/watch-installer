package com.snown.watchinstaller.logic

/** 워치의 `cmd package install` 이 돌려준 결과. */
sealed interface InstallResult {
    data object Success : InstallResult
    /** 워치가 돌려준 원문(예: "Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: ...]") */
    data class Failure(val output: String) : InstallResult
    /** 정해진 시간 안에 결과 줄이 오지 않았다 — 워치에는 설치됐을 수도 있다. */
    data object NoAnswer : InstallResult
}

/**
 * 설치 결과 읽기. 워치가 결과를 쓰고 스트림을 닫아도 libadb 쪽에서 EOF 가 안 올 때가 있어서,
 * 끝까지 기다리지 않고 'Success'/'Failure' 가 든 줄이 보이면 바로 결과로 본다.
 */
object InstallOutput {
    const val TIMEOUT_MS = 120_000L

    /**
     * 지금까지 받은 출력에서 결과를 찾는다. 아직 결과 줄이 없으면 null.
     * @param ended 스트림이 닫혔으면 true — 결과 줄이 없어도 받은 것을 실패로 돌려준다.
     */
    fun parse(output: String, ended: Boolean): InstallResult? {
        // 줄 끝(\n)이 아직 안 온 마지막 줄도 본다 — 'Success' 뒤 줄바꿈을 기다릴 이유가 없다.
        for (line in output.lineSequence()) {
            val l = line.trim()
            if (l.startsWith("Failure") || l.contains("Failure [")) return InstallResult.Failure(l)
            if (l.startsWith("Success")) return InstallResult.Success
        }
        return if (ended) InstallResult.Failure(output.trim()) else null
    }

    fun timedOut(startedAt: Long, now: Long, timeoutMs: Long = TIMEOUT_MS): Boolean = now - startedAt >= timeoutMs
}
