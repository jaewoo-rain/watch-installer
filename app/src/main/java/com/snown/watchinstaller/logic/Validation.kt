package com.snown.watchinstaller.logic

import com.snown.watchinstaller.i18n.Msg

/** 입력칸 검증. 문제가 없으면 null, 있으면 화면에 띄울 안내. */
object Validation {

    private val ipv4 = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")

    fun ipError(raw: String): Msg? {
        val ip = raw.trim()
        if (ip.isEmpty()) return { it.ipEmpty }
        val m = ipv4.matchEntire(ip) ?: return { it.ipFormat }
        if (m.groupValues.drop(1).any { it.toInt() > 255 }) return { it.ipRange }
        return null
    }

    fun portError(raw: String): Msg? {
        val port = raw.trim()
        if (port.isEmpty()) return { it.portEmpty }
        val n = port.toIntOrNull() ?: return { it.portDigits }
        if (n !in 1..65535) return { it.portRange }
        return null
    }

    fun codeError(raw: String): Msg? {
        val code = raw.trim()
        if (code.isEmpty()) return { it.codeEmpty }
        if (!code.all { it.isDigit() } || code.length != 6) return { it.codeFormat }
        return null
    }

    /**
     * 워치 화면에서 "192.168.0.12:37123" 처럼 통째로 붙여넣은 경우 IP 와 포트로 나눈다.
     * 콜론이 없으면 null.
     */
    fun splitHostPort(raw: String): Pair<String, String>? {
        val s = raw.trim()
        val i = s.lastIndexOf(':')
        if (i <= 0 || i == s.length - 1) return null
        return s.substring(0, i).trim() to s.substring(i + 1).trim()
    }
}
