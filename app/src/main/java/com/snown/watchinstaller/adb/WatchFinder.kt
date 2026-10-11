package com.snown.watchinstaller.adb

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.github.muntashirakon.adb.android.AdbMdns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * 같은 와이파이에 있는 워치의 무선 디버깅 주소를 mDNS 로 찾는다.
 * - 연결용: `_adb-tls-connect._tcp` (무선 디버깅이 켜져 있으면 광고)
 * - 페어링용: `_adb-tls-pairing._tcp` (워치에서 '새 기기 페어링' 창이 열려 있는 동안만 광고)
 * 공유기가 mDNS 를 막으면 아무것도 안 찾아진다 — 그땐 직접 입력하면 된다.
 */
class WatchFinder(
    context: Context,
    onConnect: (host: String, port: Int) -> Unit,
    onPairing: (host: String, port: Int) -> Unit,
) {
    private val main = Handler(Looper.getMainLooper())

    private val finders = listOf(
        AdbMdns(context, AdbMdns.SERVICE_TYPE_TLS_CONNECT) { host, port ->
            val ip = (host as? Inet4Address)?.takeUnless(::isThisPhone)?.hostAddress
            if (ip != null && port > 0) main.post { onConnect(ip, port) }
        },
        AdbMdns(context, AdbMdns.SERVICE_TYPE_TLS_PAIRING) { host, port ->
            val ip = (host as? Inet4Address)?.takeUnless(::isThisPhone)?.hostAddress
            if (ip != null && port > 0) main.post { onPairing(ip, port) }
        },
    )

    /** 폰 자신의 무선 디버깅이 켜져 있으면 폰도 광고되므로 걸러 낸다. */
    private fun isThisPhone(addr: InetAddress): Boolean =
        addr.isLoopbackAddress || runCatching { NetworkInterface.getByInetAddress(addr) != null }.getOrDefault(false)

    fun start() = finders.forEach { runCatching { if (!it.isRunning) it.start() } }

    fun stop() = finders.forEach { runCatching { it.stop() } }
}
