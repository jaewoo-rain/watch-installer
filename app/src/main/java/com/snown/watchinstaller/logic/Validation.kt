package com.snown.watchinstaller.logic

/** 입력칸 검증. 문제가 없으면 null, 있으면 화면에 띄울 한국어 안내. */
object Validation {

    private val ipv4 = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")

    fun ipError(raw: String): String? {
        val ip = raw.trim()
        if (ip.isEmpty()) return "IP 주소를 넣어 주세요"
        val m = ipv4.matchEntire(ip) ?: return "숫자 네 덩어리를 점으로 이어 써 주세요 (예: 192.168.0.12)"
        if (m.groupValues.drop(1).any { it.toInt() > 255 }) return "각 숫자는 0~255 사이여야 해요"
        return null
    }

    fun portError(raw: String): String? {
        val port = raw.trim()
        if (port.isEmpty()) return "포트 번호를 넣어 주세요"
        val n = port.toIntOrNull() ?: return "포트는 숫자만 넣어 주세요"
        if (n !in 1..65535) return "포트 번호가 너무 커요 (1~65535)"
        return null
    }

    fun codeError(raw: String): String? {
        val code = raw.trim()
        if (code.isEmpty()) return "워치 화면의 6자리 숫자를 넣어 주세요"
        if (!code.all { it.isDigit() } || code.length != 6) return "숫자 6자리여야 해요"
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
