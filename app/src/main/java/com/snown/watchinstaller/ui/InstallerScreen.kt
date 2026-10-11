package com.snown.watchinstaller.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snown.watchinstaller.InstallState
import com.snown.watchinstaller.apk.ApkSource
import com.snown.watchinstaller.InstallerViewModel
import com.snown.watchinstaller.StepStatus
import com.snown.watchinstaller.i18n.Lang
import com.snown.watchinstaller.i18n.Msg
import com.snown.watchinstaller.i18n.Texts
import com.snown.watchinstaller.logic.Format

/** 지금 화면 언어의 문구. */
val LocalTexts = compositionLocalOf<Texts> { Lang.KO.texts }

/** 단계 번호 원의 모양. */
enum class Dot { DONE, ACTIVE, WAITING }

// 여백 리듬: 바깥·카드 안 16dp, 요소 사이 12dp.
private val Gap = 12.dp
private val Pad = 16.dp

@Composable
fun InstallerScreen(vm: InstallerViewModel, onPickFile: () -> Unit, onAllowDownloads: () -> Unit) {
    // 아직 끝나지 않은 첫 단계를 강조한다.
    val done = listOf(
        vm.pairStatus == StepStatus.DONE,
        vm.connectStatus == StepStatus.DONE,
        vm.apk != null,
        vm.install == InstallState.Done,
    )
    val current = done.indexOfFirst { !it } + 1
    fun dot(step: Int) = when {
        done[step - 1] -> Dot.DONE
        step == current -> Dot.ACTIVE
        else -> Dot.WAITING
    }
    ScreenFrame(vm.lang, vm::chooseLang) {
        PairStep(vm, dot(1))
        ConnectStep(vm, dot(2))
        FileStep(vm, dot(3), onPickFile, onAllowDownloads)
        InstallStep(vm, dot(4))
    }
}

/** 머리(제목 + 언어 전환) · 안내 · 단계 카드 · 끝맺음 안내. */
@Composable
private fun ScreenFrame(lang: Lang, onLang: (Lang) -> Unit, steps: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTexts provides lang.texts) {
        val t = LocalTexts.current
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Pad),
                verticalArrangement = Arrangement.spacedBy(Gap),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        t.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(Gap))
                    LangSwitch(lang, onLang)
                }
                Text(t.intro, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        t.sameWifi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Pad, vertical = Gap),
                    )
                }
                steps()
                Text(t.tipTurnOff, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 우측 상단 `한글 | English` 2칸 세그먼트. */
@Composable
private fun LangSwitch(lang: Lang, onLang: (Lang) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = CircleShape, border = BorderStroke(1.dp, colors.outline), color = colors.surface) {
        Row(Modifier.selectableGroup()) {
            Lang.entries.forEach { l ->
                val selected = l == lang
                Box(
                    Modifier
                        .height(36.dp)
                        .background(if (selected) colors.primary else Color.Transparent, CircleShape)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onLang(l) })
                        .padding(horizontal = Gap),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        l.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * 단계 카드 한 장: 왼쪽 번호 원, 제목 한 줄 + 설명 한 줄, 그 아래 입력칸·버튼.
 * [summary] 가 있고 완료된 단계면 요약 한 줄로 접히고, 머리를 누르면 다시 펼친다.
 */
@Composable
fun StepCard(
    number: Int,
    title: String,
    desc: String,
    dot: Dot,
    summary: String? = null,
    content: @Composable () -> Unit,
) {
    val t = LocalTexts.current
    val collapsible = dot == Dot.DONE && summary != null
    var expanded by rememberSaveable(collapsible) { mutableStateOf(!collapsible) }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(Pad), verticalArrangement = Arrangement.spacedBy(Gap)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (collapsible) Modifier.clickable(onClickLabel = if (expanded) t.collapse else t.expand) { expanded = !expanded } else Modifier),
            ) {
                StepDot(number, dot)
                Spacer(Modifier.width(Gap))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (collapsible && !expanded) summary.orEmpty() else desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (collapsible) {
                    Text(
                        if (expanded) "▴" else "▾",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp).clearAndSetSemantics { },
                    )
                }
            }
            if (expanded) content()
        }
    }
}

@Composable
private fun StepDot(number: Int, dot: Dot) {
    val t = LocalTexts.current
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when (dot) {
        Dot.DONE -> colors.primaryContainer to colors.primary
        Dot.ACTIVE -> colors.primary to colors.onPrimary
        Dot.WAITING -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    val state = when (dot) {
        Dot.DONE -> t.stepDone
        Dot.ACTIVE -> t.stepActive
        Dot.WAITING -> t.stepWaiting
    }
    Box(
        Modifier
            .size(28.dp)
            .background(bg, CircleShape)
            .clearAndSetSemantics { contentDescription = "$number · $state" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (dot == Dot.DONE) "✓" else number.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun Guide(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** 진행·안내([good]) 는 강조색, 오류는 빨강. */
@Composable
private fun Message(msg: Msg?, good: Boolean = false) {
    if (msg == null) return
    Text(
        msg(LocalTexts.current),
        style = MaterialTheme.typography.bodyMedium,
        color = if (good) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    )
}

/** 설치 결과 배너: 제목 + 원인·다음 할 일. */
@Composable
fun ResultBanner(success: Boolean, title: String, body: String) {
    val ok = LocalSuccessColors.current
    val colors = MaterialTheme.colorScheme
    val (container, accent, onContainer) =
        if (success) Triple(ok.container, ok.content, ok.onContainer)
        else Triple(colors.errorContainer, colors.error, colors.onErrorContainer)
    Surface(color = container, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = Pad, vertical = Gap), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = accent)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = onContainer)
        }
    }
}

/** 보내는 중: 막대 + 퍼센트. [percent] 가 null 이면 끝을 모르는 막대(워치에서 설치 중). */
@Composable
fun InstallProgress(label: String, percent: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (percent != null) {
                Text("$percent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        if (percent != null) {
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    help: String,
    number: Boolean,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        supportingText = { Text(help) },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            // IP 칸은 "192.168.0.12:37123" 붙여넣기와 점·콜론 입력이 되도록 숫자 전용 키패드를 쓰지 않는다.
            keyboardType = if (number) KeyboardType.Number else KeyboardType.Uri,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PairStep(vm: InstallerViewModel, dot: Dot) {
    val t = LocalTexts.current
    StepCard(1, t.pairTitle, t.pairDesc, dot, summary = t.pairSummary) {
        if (vm.pairStatus == StepStatus.DONE) {
            Guide(t.pairDoneGuide)
            Message(vm.pairMessage, good = true)
            TextButton(onClick = vm::repair) { Text(t.repair) }
            return@StepCard
        }
        val busy = vm.pairStatus == StepStatus.WORKING
        Guide(t.pairGuide)
        if (vm.pairAutoFilled) Message({ it.autoFilled }, good = true)
        Field(vm.pairIp, vm::onPairIpChange, t.ipLabel, t.pairIpHelp, number = false, enabled = !busy)
        Field(vm.pairPort, { vm.pairPort = it.filter(Char::isDigit) }, t.portLabel, t.portHelp, number = true, enabled = !busy)
        Field(vm.pairCode, { vm.pairCode = it.filter(Char::isDigit).take(6) }, t.codeLabel, t.codeHelp, number = true, enabled = !busy)
        Message(vm.pairMessage, good = busy)
        BusyButton(t.pairButton, busy = busy, onClick = vm::pair)
    }
}

@Composable
private fun ConnectStep(vm: InstallerViewModel, dot: Dot) {
    val t = LocalTexts.current
    val connectedTo = vm.connectedTo
    StepCard(2, t.connectTitle, t.connectDesc, dot, summary = connectedTo?.let(t::connectedTo)) {
        if (vm.connectStatus == StepStatus.DONE && connectedTo != null) {
            Message({ it.connectedTo(connectedTo) }, good = true)
            TextButton(onClick = vm::disconnect) { Text(t.disconnect) }
            return@StepCard
        }
        val busy = vm.connectStatus == StepStatus.WORKING
        Guide(t.connectGuide)
        if (vm.connectAutoFilled) Message({ it.autoFilled }, good = true)
        Field(vm.connectIp, vm::onConnectIpChange, t.ipLabel, t.connectIpHelp, number = false, enabled = !busy)
        Field(vm.connectPort, { vm.connectPort = it.filter(Char::isDigit) }, t.portLabel, t.portHelp, number = true, enabled = !busy)
        Message(vm.connectMessage, good = busy)
        BusyButton(t.connectButton, busy = busy, onClick = vm::connect)
    }
}

@Composable
private fun FileStep(vm: InstallerViewModel, dot: Dot, onPickFile: () -> Unit, onAllowDownloads: () -> Unit) {
    val t = LocalTexts.current
    val apk = vm.apk
    val name = apk?.name?.ifEmpty { t.pickedFileName }
    StepCard(3, t.fileTitle, t.fileDesc, dot, summary = name) {
        if (apk != null) {
            Column {
                Text(name.orEmpty(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                val modified = apk.modified?.let { " · " + t.modifiedAt(Format.time(it, t)) }.orEmpty()
                Guide("${Format.size(apk.size)}$modified")
            }
            if (apk.file != null && apk.name == ApkSource.DEFAULT_NAME) Guide(t.autoPicked)
        } else {
            Message(vm.fileMessage)
            Guide(t.noFileGuide)
        }
        OutlinedButton(onClick = onPickFile, modifier = Modifier.fillMaxWidth()) {
            Text(if (apk == null) t.pickFile else t.pickOther)
        }
        if (!vm.canReadDownloads) {
            Guide(t.allowGuide)
            TextButton(onClick = onAllowDownloads) { Text(t.allowButton) }
        }
    }
}

@Composable
private fun InstallStep(vm: InstallerViewModel, dot: Dot) {
    val t = LocalTexts.current
    val state = vm.install
    StepCard(4, t.installTitle, t.installDesc, dot) {
        when (state) {
            InstallState.Idle -> Guide(
                when {
                    vm.connectStatus != StepStatus.DONE -> t.needConnect
                    vm.apk == null -> t.needFile
                    else -> t.ready
                },
            )
            is InstallState.Sending -> InstallProgress(t.sending, Format.percent(state.sent, state.total))
            InstallState.Installing -> InstallProgress(t.installing, null)
            InstallState.Done -> ResultBanner(success = true, title = t.successTitle, body = t.successNext)
            is InstallState.Failed -> ResultBanner(success = false, title = t.failTitle, body = state.message(t))
        }
        val busy = state is InstallState.Sending || state is InstallState.Installing
        BusyButton(
            if (state is InstallState.Done || state is InstallState.Failed) t.reinstallButton else t.installButton,
            busy = busy,
            enabled = vm.canInstall,
            onClick = vm::install,
        )
    }
}

@Composable
private fun BusyButton(label: String, busy: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.padding(end = 8.dp).size(18.dp), strokeWidth = 2.dp)
        }
        Text(label)
    }
}

// ---- 미리보기: ViewModel 없이 카드 모양만 확인한다 ----

@Composable
private fun PreviewScreen(lang: Lang, dark: Boolean) {
    InstallerTheme(dark = dark) {
        ScreenFrame(lang, onLang = {}) {
            val t = LocalTexts.current
            StepCard(1, t.pairTitle, t.pairDesc, Dot.DONE, summary = t.pairSummary) {}
            StepCard(2, t.connectTitle, t.connectDesc, Dot.ACTIVE) {
                Guide(t.connectGuide)
                Field("192.168.0.12", {}, t.ipLabel, t.connectIpHelp, number = false, enabled = true)
                Field("", {}, t.portLabel, t.portHelp, number = true, enabled = true)
                Message({ it.errConnectRefused })
                BusyButton(t.connectButton, busy = false) {}
            }
            StepCard(3, t.fileTitle, t.fileDesc, Dot.DONE, summary = "watch-app.apk") {}
            StepCard(4, t.installTitle, t.installDesc, Dot.WAITING) {
                Guide(t.needConnect)
                BusyButton(t.installButton, busy = false, enabled = false) {}
            }
        }
    }
}

@Composable
private fun PreviewInstall(lang: Lang, dark: Boolean) {
    InstallerTheme(dark = dark) {
        CompositionLocalProvider(LocalTexts provides lang.texts) {
            val t = LocalTexts.current
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(Pad), verticalArrangement = Arrangement.spacedBy(Gap)) {
                    StepCard(4, t.installTitle, t.installDesc, Dot.ACTIVE) {
                        InstallProgress(t.sending, 42)
                        BusyButton(t.installButton, busy = true) {}
                    }
                    ResultBanner(success = true, title = t.successTitle, body = t.successNext)
                    ResultBanner(success = false, title = t.failTitle, body = t.installSignature)
                }
            }
        }
    }
}

@Preview(name = "Light · Korean", widthDp = 360, heightDp = 1000)
@Composable
private fun PreviewLightKo() = PreviewScreen(Lang.KO, dark = false)

@Preview(name = "Dark · English", widthDp = 360, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDarkEn() = PreviewScreen(Lang.EN, dark = true)

@Preview(name = "Install · Light · English", widthDp = 360)
@Composable
private fun PreviewInstallLightEn() = PreviewInstall(Lang.EN, dark = false)

@Preview(name = "Install · Dark · Korean", widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewInstallDarkKo() = PreviewInstall(Lang.KO, dark = true)
