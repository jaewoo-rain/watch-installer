package com.snown.watchinstaller.i18n

import java.util.Locale

/**
 * 화면에 보이는 모든 문구. 한국어([Ko])·영어([En]) 두 벌이 같은 항목을 채운다.
 * Android 리소스 대신 Kotlin 객체로 둔 이유는 docs/DEVELOPMENT.md 참고 — 실패·검증 메시지를 JVM 단위 테스트로 두 언어 다 확인하려고.
 */
interface Texts {
    val locale: Locale
    val datePattern: String

    // 머리
    val appName: String
    val intro: String
    val sameWifi: String
    val tipTurnOff: String
    val stepDone: String
    val stepActive: String
    val stepWaiting: String
    val expand: String
    val collapse: String

    // 입력칸
    val ipLabel: String
    val portLabel: String
    val portHelp: String
    val ipEmpty: String
    val ipFormat: String
    val ipRange: String
    val portEmpty: String
    val portDigits: String
    val portRange: String
    val codeEmpty: String
    val codeFormat: String

    // 1. 페어링
    val pairTitle: String
    val pairDesc: String
    val pairSummary: String
    val pairDoneGuide: String
    val repair: String
    val pairGuide: String
    val autoFilled: String
    val pairIpHelp: String
    val codeLabel: String
    val codeHelp: String
    val pairButton: String
    val pairing: String
    val pairDone: String
    val pairRejected: String

    // 2. 연결
    val connectTitle: String
    val connectDesc: String
    fun connectedTo(address: String): String
    val disconnect: String
    val connectGuide: String
    val connectIpHelp: String
    val connectButton: String
    val connecting: String
    val connectRejected: String

    // 3. 파일
    val fileTitle: String
    val fileDesc: String
    val pickedFileName: String
    fun modifiedAt(time: String): String
    val autoPicked: String
    val noFileGuide: String
    val pickFile: String
    val pickOther: String
    val allowGuide: String
    val allowButton: String
    val filePickAgain: String
    val fileUnreadable: String

    // 4. 설치
    val installTitle: String
    val installDesc: String
    val needConnect: String
    val needFile: String
    val ready: String
    val sending: String
    val installing: String
    val installNoAnswer: String
    val successTitle: String
    val successNext: String
    val failTitle: String
    val installButton: String
    val reinstallButton: String

    // 설치 실패(워치 응답)
    fun installNoCode(output: String): String
    val installSignature: String
    val installDowngrade: String
    val installStorage: String
    val installOlderSdk: String
    val installAbi: String
    val installRejected: String
    val installTestOnly: String
    val installBrokenApk: String
    fun installOther(code: String): String

    // 연결 오류
    val errPairingRequired: String
    val errAuthFailed: String
    val errNotFound: String
    val errPairRefused: String
    val errConnectRefused: String
    val errTimeout: String
    val errPairFailed: String
    val errTls: String
    val errDroppedDuringInstall: String
    val errDropped: String
    fun errUnknown(detail: String): String
}

/** 언어를 고르기 전까지 미뤄 두는 문구 — 언어를 바꾸면 이미 떠 있는 안내도 바로 바뀐다. */
typealias Msg = (Texts) -> String

enum class Lang(val label: String, val texts: Texts) {
    KO("한글", Ko),
    EN("English", En);

    companion object {
        fun fromTag(tag: String?): Lang? = entries.firstOrNull { it.name == tag }

        /** 처음 실행: 기기 언어가 한국어면 한글, 아니면 English. */
        fun fromLocale(locale: Locale): Lang = if (locale.language == "ko") KO else EN
    }
}
