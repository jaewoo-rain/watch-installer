package com.snown.watchinstaller

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snown.watchinstaller.adb.AdbManager
import com.snown.watchinstaller.adb.ApkInstaller
import com.snown.watchinstaller.adb.WatchFinder
import com.snown.watchinstaller.apk.ApkFile
import com.snown.watchinstaller.apk.ApkSource
import com.snown.watchinstaller.logic.FailureMessages
import com.snown.watchinstaller.logic.Stage
import com.snown.watchinstaller.logic.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 4단계 각각의 상태. */
enum class StepStatus { WAITING, WORKING, DONE }

sealed interface InstallState {
    data object Idle : InstallState
    data class Sending(val sent: Long, val total: Long) : InstallState
    data object Installing : InstallState
    data object Done : InstallState
    data class Failed(val message: String) : InstallState
}

class InstallerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("installer", Context.MODE_PRIVATE)
    private val adb by lazy { AdbManager.get(app) }

    // 1. 페어링
    var pairIp by mutableStateOf(prefs.getString(KEY_IP, "").orEmpty())
    var pairPort by mutableStateOf("")
    var pairCode by mutableStateOf("")
    var pairStatus by mutableStateOf(if (prefs.getBoolean(KEY_PAIRED, false)) StepStatus.DONE else StepStatus.WAITING)
        private set
    var pairMessage by mutableStateOf<String?>(null)
        private set
    var pairAutoFilled by mutableStateOf(false)
        private set

    // 2. 연결
    var connectIp by mutableStateOf(prefs.getString(KEY_IP, "").orEmpty())
    var connectPort by mutableStateOf(prefs.getString(KEY_PORT, "").orEmpty())
    var connectStatus by mutableStateOf(StepStatus.WAITING)
        private set
    var connectMessage by mutableStateOf<String?>(null)
        private set
    var connectAutoFilled by mutableStateOf(false)
        private set
    var connectedTo by mutableStateOf<String?>(null)
        private set

    // 3. 파일
    var apk by mutableStateOf<ApkFile?>(null)
        private set
    var canReadDownloads by mutableStateOf(false)
        private set

    // 4. 설치
    var install by mutableStateOf<InstallState>(InstallState.Idle)
        private set

    private val finder = WatchFinder(
        app,
        onConnect = { ip, port ->
            // 사람이 이미 연결했거나 직접 고치는 중이면 덮어쓰지 않는다.
            if (connectStatus != StepStatus.WAITING) return@WatchFinder
            if (connectIp != ip || connectPort != port.toString()) {
                connectIp = ip
                connectPort = port.toString()
                connectAutoFilled = true
            }
        },
        onPairing = { ip, port ->
            if (pairStatus == StepStatus.WORKING) return@WatchFinder
            pairIp = ip
            pairPort = port.toString()
            pairAutoFilled = true
        },
    )

    init {
        refreshApk()
    }

    fun startFinding() = finder.start()
    fun stopFinding() = finder.stop()

    /** "IP:포트" 를 통째로 붙여넣으면 둘로 나눠 넣는다. */
    fun onPairIpChange(v: String) {
        pairAutoFilled = false
        val split = Validation.splitHostPort(v)
        if (split != null) { pairIp = split.first; pairPort = split.second } else pairIp = v
    }

    fun onConnectIpChange(v: String) {
        connectAutoFilled = false
        val split = Validation.splitHostPort(v)
        if (split != null) { connectIp = split.first; connectPort = split.second } else connectIp = v
    }

    val pairInputError: String?
        get() = Validation.ipError(pairIp) ?: Validation.portError(pairPort) ?: Validation.codeError(pairCode)

    val connectInputError: String?
        get() = Validation.ipError(connectIp) ?: Validation.portError(connectPort)

    fun pair() {
        pairInputError?.let { pairMessage = it; return }
        val ip = pairIp.trim()
        val port = pairPort.trim().toInt()
        val code = pairCode.trim()
        pairStatus = StepStatus.WORKING
        pairMessage = "페어링하는 중…"
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { adb.pair(ip, port, code) } }
            result.fold(
                onSuccess = { ok ->
                    if (ok) {
                        prefs.edit().putBoolean(KEY_PAIRED, true).putString(KEY_IP, ip).apply()
                        pairStatus = StepStatus.DONE
                        pairMessage = "페어링 완료! 이제 2번 연결로 가세요."
                        pairCode = ""
                        if (connectIp.isBlank()) connectIp = ip
                    } else {
                        pairStatus = StepStatus.WAITING
                        pairMessage = "페어링에 실패했어요. 6자리 숫자가 맞는지 확인해 주세요."
                    }
                },
                onFailure = {
                    pairStatus = StepStatus.WAITING
                    pairMessage = FailureMessages.forError(Stage.PAIR, it)
                },
            )
        }
    }

    /** 페어링을 다시 하고 싶을 때 1번 칸을 다시 연다. */
    fun repair() {
        pairStatus = StepStatus.WAITING
        pairMessage = null
    }

    fun connect() {
        connectInputError?.let { connectMessage = it; return }
        val ip = connectIp.trim()
        val port = connectPort.trim().toInt()
        connectStatus = StepStatus.WORKING
        connectMessage = "연결하는 중… 워치에 확인 창이 뜨면 '허용'을 눌러 주세요."
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (adb.isConnected) runCatching { adb.disconnect() }
                    adb.connect(ip, port)
                }
            }
            result.fold(
                onSuccess = { ok ->
                    if (ok) {
                        prefs.edit().putString(KEY_IP, ip).putString(KEY_PORT, port.toString()).apply()
                        connectStatus = StepStatus.DONE
                        connectedTo = "$ip:$port"
                        connectMessage = null
                    } else {
                        connectStatus = StepStatus.WAITING
                        connectMessage = "워치가 연결을 허락하지 않았어요. 워치 화면에서 '허용'을 누르고 다시 해 주세요."
                    }
                },
                onFailure = {
                    connectStatus = StepStatus.WAITING
                    connectMessage = FailureMessages.forError(Stage.CONNECT, it)
                },
            )
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) { runCatching { adb.disconnect() } }
        markDisconnected(null)
    }

    private fun markDisconnected(message: String?) {
        connectStatus = StepStatus.WAITING
        connectedTo = null
        connectMessage = message
    }

    /** 앱으로 돌아올 때마다: 기본 파일을 다시 확인(새로 받았을 수 있음). */
    fun refreshApk() {
        canReadDownloads = ApkSource.canReadAllFiles()
        val default = ApkSource.readDefault()
        if (default != null) {
            apk = default
            return
        }
        val current = apk
        if (current?.file != null) apk = null // 기본 파일이 지워졌다
        if (apk == null) {
            prefs.getString(KEY_PICKED, null)?.let { saved ->
                apk = ApkSource.fromUri(getApplication(), Uri.parse(saved))
            }
        } else if (current?.uri != null) {
            apk = ApkSource.fromUri(getApplication(), current.uri)
        }
    }

    fun onPicked(uri: Uri) {
        val app = getApplication<Application>()
        ApkSource.keepPermission(app, uri)
        val picked = ApkSource.fromUri(app, uri) ?: return
        prefs.edit().putString(KEY_PICKED, uri.toString()).apply()
        apk = picked
        if (install !is InstallState.Sending && install !is InstallState.Installing) install = InstallState.Idle
    }

    val canInstall: Boolean
        get() = connectStatus == StepStatus.DONE && apk != null &&
            install !is InstallState.Sending && install !is InstallState.Installing

    fun install() {
        val file = apk ?: return
        if (!canInstall) return
        install = InstallState.Sending(0, file.size)
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    file.open(app).use { input ->
                        ApkInstaller.install(adb, input, file.size) { sent ->
                            val state = if (sent >= file.size) InstallState.Installing else InstallState.Sending(sent, file.size)
                            viewModelScope.launch { install = state }
                        }
                    }
                }
            }
            install = result.fold(
                onSuccess = {
                    when (it) {
                        ApkInstaller.Result.Success -> InstallState.Done
                        is ApkInstaller.Result.Failure -> InstallState.Failed(FailureMessages.forInstallOutput(it.output))
                    }
                },
                onFailure = {
                    if (!adb.isConnected) markDisconnected(null)
                    InstallState.Failed(FailureMessages.forError(Stage.INSTALL, it))
                },
            )
        }
    }

    override fun onCleared() {
        finder.stop()
    }

    private companion object {
        const val KEY_IP = "ip"
        const val KEY_PORT = "port"
        const val KEY_PAIRED = "paired"
        const val KEY_PICKED = "picked_uri"
    }
}
