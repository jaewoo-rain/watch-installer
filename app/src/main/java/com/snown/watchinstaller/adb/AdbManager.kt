package com.snown.watchinstaller.adb

import android.content.Context
import android.os.Build
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * 이 폰의 ADB 신원(키 쌍 + 자체 서명 인증서)을 들고 있는 연결 관리자.
 * 키는 앱 내부 저장소(filesDir)에 처음 한 번 만들어 두고 계속 쓴다 — 키가 바뀌면 워치가 모르는 기기가 되어 다시 페어링해야 한다.
 */
class AdbManager private constructor(
    private val privateKey: PrivateKey,
    private val certificate: Certificate,
) : AbsAdbConnectionManager() {

    init {
        setApi(Build.VERSION.SDK_INT)
        setTimeout(15, TimeUnit.SECONDS)
    }

    override fun getPrivateKey(): PrivateKey = privateKey
    override fun getCertificate(): Certificate = certificate
    override fun getDeviceName(): String = "Wear Installer"

    companion object {
        @Volatile private var instance: AdbManager? = null

        fun get(context: Context): AdbManager =
            instance ?: synchronized(this) {
                instance ?: create(context.applicationContext).also { instance = it }
            }

        private fun create(context: Context): AdbManager {
            val keyFile = File(context.filesDir, "adb_key.pk8")
            val certFile = File(context.filesDir, "adb_cert.der")
            if (keyFile.exists() && certFile.exists()) {
                runCatching {
                    val key = KeyFactory.getInstance("RSA")
                        .generatePrivate(PKCS8EncodedKeySpec(keyFile.readBytes()))
                    val cert = certFile.inputStream().use {
                        CertificateFactory.getInstance("X.509").generateCertificate(it)
                    }
                    return AdbManager(key, cert)
                }
            }
            val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val subject = X500Name("CN=snown-watch-installer")
            val now = System.currentTimeMillis()
            val holder = JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(now),
                Date(now - TimeUnit.DAYS.toMillis(1)),
                Date(now + TimeUnit.DAYS.toMillis(365L * 30)),
                subject,
                pair.public,
            ).build(JcaContentSignerBuilder("SHA256withRSA").build(pair.private))
            val cert = JcaX509CertificateConverter().getCertificate(holder)
            keyFile.writeBytes(pair.private.encoded)
            certFile.writeBytes(cert.encoded)
            return AdbManager(pair.private, cert)
        }
    }
}
