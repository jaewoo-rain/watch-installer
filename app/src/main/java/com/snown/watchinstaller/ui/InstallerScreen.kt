package com.snown.watchinstaller.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snown.watchinstaller.InstallState
import com.snown.watchinstaller.InstallerViewModel
import com.snown.watchinstaller.StepStatus
import com.snown.watchinstaller.logic.Format

private val Good = Color(0xFF2E7D32)

@Composable
fun InstallerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}

@Composable
fun InstallerScreen(vm: InstallerViewModel, onPickFile: () -> Unit, onAllowDownloads: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("워치 설치 도우미", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "위에서부터 순서대로 하면 폰에 있는 스논 워치 앱을 워치에 깔 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PairStep(vm)
            ConnectStep(vm)
            FileStep(vm, onPickFile, onAllowDownloads)
            InstallStep(vm)
            Text(
                "💡 설치가 끝나면 워치의 무선 디버깅을 꺼 두세요(배터리).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun StepCard(number: Int, title: String, status: StepStatus, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$number  $title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(status)
            }
            content()
        }
    }
}

@Composable
private fun StatusBadge(status: StepStatus) {
    val (label, color) = when (status) {
        StepStatus.DONE -> "완료" to Good
        StepStatus.WORKING -> "진행 중" to MaterialTheme.colorScheme.primary
        StepStatus.WAITING -> "대기" to MaterialTheme.colorScheme.outline
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Guide(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Message(text: String?, good: Boolean = false) {
    if (text == null) return
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (good) Good else MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    help: String,
    number: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        supportingText = { Text(help) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (number) KeyboardType.Number else KeyboardType.Uri,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun PairStep(vm: InstallerViewModel) {
    StepCard(1, "페어링 (처음 한 번만)", vm.pairStatus) {
        if (vm.pairStatus == StepStatus.DONE) {
            Guide("이 폰과 워치가 이미 짝지어져 있어요. 워치를 초기화했거나 연결이 계속 거부되면 다시 해 주세요.")
            Message(vm.pairMessage, good = true)
            TextButton(onClick = vm::repair) { Text("페어링 다시 하기") }
            return@StepCard
        }
        val busy = vm.pairStatus == StepStatus.WORKING
        Guide("워치에서: 설정 → 개발자 옵션 → 무선 디버깅 → '새 기기 페어링'을 누르세요. 그 화면을 켠 채로 아래를 채우세요.")
        if (vm.pairAutoFilled) Message("같은 와이파이에서 워치를 찾아 IP·포트를 채웠어요.", good = true)
        Field(vm.pairIp, vm::onPairIpChange, "IP 주소", "워치 화면의 'IP 주소 및 포트' 에서 콜론(:) 앞부분", number = false, enabled = !busy)
        Field(vm.pairPort, { vm.pairPort = it.filter(Char::isDigit) }, "포트", "콜론(:) 뒤의 숫자", number = true, enabled = !busy)
        Field(
            vm.pairCode,
            { vm.pairCode = it.filter(Char::isDigit).take(6) },
            "페어링 코드",
            "워치 화면에 보이는 6자리 숫자",
            number = true,
            enabled = !busy,
        )
        Message(vm.pairMessage, good = busy)
        BusyButton("페어링", busy = busy, onClick = vm::pair)
    }
}

@Composable
private fun ConnectStep(vm: InstallerViewModel) {
    StepCard(2, "워치 연결", vm.connectStatus) {
        val connectedTo = vm.connectedTo
        if (vm.connectStatus == StepStatus.DONE && connectedTo != null) {
            Message("워치에 연결됐어요 ($connectedTo)", good = true)
            TextButton(onClick = vm::disconnect) { Text("연결 끊기") }
            return@StepCard
        }
        val busy = vm.connectStatus == StepStatus.WORKING
        Guide("워치의 무선 디버깅 첫 화면에 보이는 'IP 주소 및 포트'를 넣으세요. 포트는 무선 디버깅을 켤 때마다 바뀌어요.")
        if (vm.connectAutoFilled) Message("같은 와이파이에서 워치를 찾아 IP·포트를 채웠어요.", good = true)
        Field(vm.connectIp, vm::onConnectIpChange, "IP 주소", "예: 192.168.0.12", number = false, enabled = !busy)
        Field(vm.connectPort, { vm.connectPort = it.filter(Char::isDigit) }, "포트", "콜론(:) 뒤의 숫자", number = true, enabled = !busy)
        Message(vm.connectMessage, good = busy)
        BusyButton("연결", busy = busy, onClick = vm::connect)
    }
}

@Composable
private fun FileStep(vm: InstallerViewModel, onPickFile: () -> Unit, onAllowDownloads: () -> Unit) {
    val apk = vm.apk
    StepCard(3, "설치할 파일", if (apk != null) StepStatus.DONE else StepStatus.WAITING) {
        if (apk != null) {
            Text(apk.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            val modified = apk.modified?.let { " · ${Format.time(it)} 수정" }.orEmpty()
            Guide("${Format.size(apk.size)}$modified")
            if (apk.file != null) Guide("다운로드 폴더의 snown-watch.apk 를 자동으로 골랐어요.")
        } else {
            Guide("다운로드 폴더에 snown-watch.apk 를 받아 두거나, 아래 버튼으로 파일을 고르세요.")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickFile) { Text(if (apk == null) "파일 고르기" else "다른 파일 고르기") }
        }
        if (!vm.canReadDownloads) {
            Guide("'모든 파일 접근'을 허용하면 다음부터 다운로드 폴더의 snown-watch.apk 를 알아서 집어 와요.")
            TextButton(onClick = onAllowDownloads) { Text("다운로드 폴더 자동 읽기 허용") }
        }
    }
}

@Composable
private fun InstallStep(vm: InstallerViewModel) {
    val state = vm.install
    val status = when (state) {
        InstallState.Done -> StepStatus.DONE
        is InstallState.Sending, InstallState.Installing -> StepStatus.WORKING
        else -> StepStatus.WAITING
    }
    StepCard(4, "워치에 설치", status) {
        when (state) {
            InstallState.Idle -> Guide(
                when {
                    vm.connectStatus != StepStatus.DONE -> "2번에서 워치에 먼저 연결하세요."
                    vm.apk == null -> "3번에서 파일을 먼저 고르세요."
                    else -> "준비됐어요. 설치를 누르세요. 워치에 있는 스논은 새 파일로 덮어써요."
                },
            )
            is InstallState.Sending -> {
                val pct = Format.percent(state.sent, state.total)
                Text("워치로 보내는 중… $pct%")
                LinearProgressIndicator(progress = { pct / 100f }, modifier = Modifier.fillMaxWidth())
            }
            InstallState.Installing -> {
                Text("워치에서 설치하는 중… (조금 걸려요)")
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            InstallState.Done -> Message("설치 완료! 워치에서 스논을 열어 보세요.", good = true)
            is InstallState.Failed -> Message(state.message)
        }
        val busy = status == StepStatus.WORKING
        BusyButton(
            if (state is InstallState.Done || state is InstallState.Failed) "다시 설치" else "설치",
            busy = busy,
            enabled = vm.canInstall,
            onClick = vm::install,
        )
    }
}

@Composable
private fun BusyButton(label: String, busy: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !busy, modifier = Modifier.fillMaxWidth()) {
        if (busy) {
            CircularProgressIndicator(Modifier.padding(end = 8.dp).size(18.dp), strokeWidth = 2.dp)
        }
        Text(label)
    }
}
