package com.senplo.plocare.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.MakerGroup
import com.senplo.plocare.navigation.Route
import com.senplo.plocare.scanner.CodeScannerView
import com.senplo.plocare.ui.components.PloCareBottomSheet
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareChip
import com.senplo.plocare.ui.components.PloCareWizardHeader
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_check_thin
import plocare.shared.generated.resources.ic_chevron_down
import plocare.shared.generated.resources.ic_chevron_forward

/**
 * Figma 4-step purifier settings (79:2246, 79:3086, 79:3305 / 94:4155, 94:4460). UI only: BLE, model
 * write, and Wi-Fi transfer are not wired, and destinations Figma does not draw (pressure options,
 * nearby sensor list, manual Wi-Fi entry) do nothing yet.
 */
@Composable
fun SettingsWizardScreen(
    context: Route.DeviceSettings,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    SettingsWizard(context = context, onBack = onBack, onFinished = onFinished)
}

/** [initialDraft] and the open flags set the starting state (default: step 1). */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SettingsWizard(
    context: Route.DeviceSettings,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    initialDraft: SettingsDraft = SettingsDraft(),
    initialManualModelOpen: Boolean = false,
    initialScanGuideOpen: Boolean = false,
) {
    var draft by remember { mutableStateOf(initialDraft) }
    var manualModelOpen by rememberSaveable { mutableStateOf(initialManualModelOpen) }
    var scanGuideOpen by rememberSaveable { mutableStateOf(initialScanGuideOpen) }
    val deviceNickname = remember(context.deviceId, context.deviceNickname) {
        context.deviceNickname.ifBlank {
            when (context.deviceId) {
                DashboardFixtures.KITCHEN_ID -> "우리 집 주방 정수기"
                DashboardFixtures.OFFICE_ID -> "사무실 직수 정수기"
                else -> context.deviceId
            }
        }
    }
    val deviceTitle = if (context.isPartnerContext) "고객 ${context.customerId} · $deviceNickname" else deviceNickname

    fun goBack() {
        when {
            manualModelOpen -> manualModelOpen = false
            draft.step == SettingsStep.PURIFIER -> onBack()
            else -> draft = draft.back()
        }
    }

    BackHandler(enabled = manualModelOpen || draft.step != SettingsStep.PURIFIER) { goBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.Background)
            .statusBarsPadding(),
    ) {
        PloCareWizardHeader(
            title = if (manualModelOpen) "모델 • 필터 확인" else draft.step.title,
            deviceTitle = deviceTitle,
            currentStep = draft.step.ordinal + 1,
            totalSteps = SettingsStep.entries.size,
            onBackClick = { goBack() },
            // Figma keeps the `테스트` pill on step 1 only; what it runs is a feature-phase decision.
            onTestClick = if (draft.step == SettingsStep.PURIFIER && !manualModelOpen) ({}) else null,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, top = 24.dp, end = 12.dp, bottom = 12.dp),
        ) {
            WizardCard {
                when {
                    manualModelOpen -> ManualModelContent()
                    draft.step == SettingsStep.PURIFIER -> PurifierStepContent(draft)
                    draft.step == SettingsStep.SENSOR -> ScanStepContent(
                        title = "센서 연결",
                        guide = "센서에 붙은 QR 또는 바코드를 비춰 주세요",
                        target = ScanTarget.SENSOR,
                        showToast = true,
                        onCode = { code -> draft.applyScan(ScanTarget.SENSOR, code)?.let { draft = it } },
                    ) {
                        draft.sensorCode?.let { SensorFoundBox(it) }
                        FallbackRow(title = "코드가 안보이나요?", action = "주변 센서 목록에서 고르기", onClick = {})
                    }
                    draft.step == SettingsStep.MODEL -> ScanStepContent(
                        title = "정수기 연결",
                        guide = "정수기 모델 라벨의 QR 또는 바코드를 비춰주세요",
                        target = ScanTarget.MODEL,
                        showToast = false,
                        onCode = { code -> draft.applyScan(ScanTarget.MODEL, code)?.let { draft = it } },
                    ) {
                        draft.model?.let { ModelRecognizedBox(it) }
                        FallbackRow(title = "코드가 없나요?", action = "목록에서 직접 고르기", onClick = { manualModelOpen = true })
                    }
                    else -> ScanStepContent(
                        title = "통신 연결",
                        guide = "공유기 QR을 비추면 센서가 집 Wi-Fi에 연결해요",
                        target = ScanTarget.WIFI,
                        showToast = true,
                        onCode = { code -> draft.applyScan(ScanTarget.WIFI, code)?.let { draft = it } },
                    ) {
                        FallbackRow(title = "QR이 없나요?", action = "Wi-Fi 이름• 비밀번호 직접 입력", onClick = {})
                    }
                }
            }
        }

        // Bottom action (button/Large), same frame as the other Figma screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 24.dp),
        ) {
            val (label, enabled, action) = when {
                manualModelOpen -> Triple("이 모델로 설정", true) {
                    draft = draft.copy(model = sampleModelChoice)
                    manualModelOpen = false
                }
                draft.step == SettingsStep.PURIFIER -> Triple("다음", true) { scanGuideOpen = true }
                draft.step == SettingsStep.NETWORK -> Triple("완료", draft.canAdvance()) { onFinished() }
                else -> Triple("다음", draft.canAdvance()) { draft = draft.advance() }
            }
            PloCareButton(
                text = label,
                onClick = action,
                size = PloCareButtonSize.Large,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (scanGuideOpen) {
        ScanGuideSheet(
            onDismiss = { scanGuideOpen = false },
            onStart = {
                scanGuideOpen = false
                draft = draft.advance()
            },
        )
    }
}

@Composable
private fun WizardCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PloCareColor.White, RoundedCornerShape(12.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        content = content,
    )
}

@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        modifier = Modifier.semantics { heading() },
    )
}

// Step 1 (79:2246): pressure check and the model/filter summary
@Composable
private fun ColumnScope.PurifierStepContent(draft: SettingsDraft) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        CardTitle("감압밸브 확인")
        FieldGroup(label = "설정 압력") {
            SelectBox(label = "설정 압력", value = draft.pressureKgf?.let { "$it" }, placeholder = "압력을 선택해주세요")
            HelpText("감압밸브에 적힌 압력(kgf/cm²)을 골라 주세요 • 바코드 없음")
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.Black10))
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        CardTitle("모델 • 필터 확인")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Black0Surface, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            draft.modelSummaryRows().forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = label, style = typography.labelMid, color = colors.TextMain)
                    Text(
                        text = value,
                        style = typography.labelMid,
                        color = if (value == UNKNOWN_VALUE) colors.Black40Ghost else colors.TextMain,
                    )
                }
            }
        }
    }
}

// Steps 2-4 (79:3086, 79:3305, 94:4460): live camera in the Figma photo slot, then status and fallback rows
@Composable
private fun ColumnScope.ScanStepContent(
    title: String,
    guide: String,
    target: ScanTarget,
    showToast: Boolean,
    onCode: (String) -> Unit,
    status: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardTitle(title)
            Text(text = guide, style = PloCareTheme.typography.labelMid, color = PloCareColor.TextMain)
        }
        ScannerFrame(target = target, showToast = showToast, onCode = onCode)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = status)
}

@Composable
private fun ScannerFrame(target: ScanTarget, showToast: Boolean, onCode: (String) -> Unit) {
    val colors = PloCareColor
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(258.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.Black0Surface),
    ) {
        CodeScannerView(
            frame = target.frame,
            onCodeScanned = onCode,
            modifier = Modifier.fillMaxSize(),
            permissionPrompt = { requestPermission ->
                ScannerNotice(
                    title = "카메라 권한이 필요합니다",
                    body = "코드를 비추려면 카메라 접근을 허용해 주세요.",
                    bottomInset = showToast,
                ) {
                    PloCareButton(text = "권한 허용", onClick = requestPermission, size = PloCareButtonSize.Small)
                }
            },
            fallback = {
                ScannerNotice(
                    title = "카메라 스캔을 사용할 수 없습니다",
                    body = "이 기기에서는 카메라로 코드를 읽을 수 없어요.",
                    bottomInset = showToast,
                )
            },
        )
        if (showToast) {
            // Figma toast (x22, y204, 278 wide); the backdrop blur is skipped like the GNB blur.
            Text(
                text = "QR, 바코드 모두 인식해요",
                style = PloCareTheme.typography.labelMid,
                color = colors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 22.dp, end = 22.dp, bottom = 22.dp)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
            )
        }
    }
}

@Composable
private fun ScannerNotice(
    title: String,
    body: String,
    bottomInset: Boolean,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = if (bottomInset) 70.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = title,
            style = PloCareTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = PloCareColor.TextMain,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = PloCareTheme.typography.labelMid,
            color = PloCareColor.Black50Muted,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}

@Composable
private fun StatusBox(
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.PrimaryContainerLight, shape)
            .border(1.dp, colors.GradientSecondary, shape)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = verticalAlignment,
    ) {
        content()
    }
}

private val statusTitleStyle @Composable get() =
    PloCareTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)

// 79:3086 found state with the `연결중` badge (BLE is not wired, so it never turns connected)
@Composable
private fun SensorFoundBox(sensorCode: String) {
    val colors = PloCareColor
    StatusBox {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(Res.drawable.ic_check_thin), contentDescription = null, tint = colors.GradientMain)
            Text(text = "센서 ${sensorCode}를 찾았어요", style = statusTitleStyle, color = colors.Primary)
        }
        Text(
            text = "연결중",
            style = badgeTextStyle(),
            color = colors.Primary,
            modifier = Modifier
                .background(colors.PrimaryTint, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp),
        )
    }
}

// 79:3305 recognized model box
@Composable
private fun ModelRecognizedBox(model: PurifierModelChoice) {
    val colors = PloCareColor
    StatusBox(verticalAlignment = Alignment.Top) {
        Icon(painter = painterResource(Res.drawable.ic_check_thin), contentDescription = null, tint = colors.GradientMain)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = model.recognizedHeadline(), style = statusTitleStyle, color = colors.Primary)
            Text(text = model.filterCountLine(), style = badgeTextStyle(), color = colors.Primary)
        }
    }
}

// Figma badge/caption text: Regular 13/22, -0.03em
@Composable
private fun badgeTextStyle() = PloCareTheme.typography.gnbLabel.copy(
    fontWeight = FontWeight.Normal,
    lineHeight = 22.sp,
    letterSpacing = (-0.39).sp,
)

@Composable
private fun FallbackRow(title: String, action: String, onClick: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.Black0Surface, shape)
            .border(1.dp, colors.Black10, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = colors.TextMain)
            Text(text = action, style = typography.labelLarge, color = colors.TextMain)
        }
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = colors.Black50Muted,
        )
    }
}

// 94:4155 manual model selection inside step 3. Selects show the Figma sample (no catalog yet).
@Composable
private fun ColumnScope.ManualModelContent() {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val model = sampleModelChoice
    var makerGroup by rememberSaveable { mutableStateOf(model.makerGroup) }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CardTitle("모델 • 필터 확인")
        FieldGroup(label = "제조사 구분") {
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MakerGroup.entries.forEach { group ->
                    PloCareChip(text = group.label, selected = group == makerGroup, onClick = { makerGroup = group })
                }
            }
        }
        FieldGroup(label = "제조사") {
            SelectBox(label = "제조사", value = model.makerName, placeholder = "")
        }
        FieldGroup(label = "모델명") {
            SelectBox(label = "모델명", value = model.modelName, placeholder = "")
            HelpText("정수기 옆면•뒷면 모델 라벨의 QR•바코드를 찍으면 자동 선택돼요")
        }
        FieldGroup(label = "필터 구성") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                model.filters.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .border(1.dp, colors.Black10, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors.filterTypeColor(filter.type), RoundedCornerShape(4.dp)),
                        )
                        Text(text = filter.label, style = typography.labelMidMedium, color = colors.TextMain)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = PloCareTheme.typography.labelMid, color = PloCareColor.TextMain)
        content()
    }
}

@Composable
private fun HelpText(text: String) {
    Text(text = text, style = PloCareTheme.typography.labelMid, color = PloCareColor.GradientMain)
}

/** Figma select box (247:6159). Option lists are not drawn in Figma, so opening does nothing yet. */
@Composable
private fun SelectBox(label: String, value: String?, placeholder: String) {
    val colors = PloCareColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.Black0Surface)
            .clickable(role = Role.DropdownList, onClickLabel = "$label 선택", onClick = {})
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value ?: placeholder,
            style = PloCareTheme.typography.labelLarge,
            color = if (value == null) colors.Black30 else colors.TextMain,
            modifier = Modifier.weight(1f),
        )
        Icon(painter = painterResource(Res.drawable.ic_chevron_down), contentDescription = null, tint = colors.TextMain)
    }
}

// Scan guide sheet (79:3029), opened by step 1 `다음`
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanGuideSheet(onDismiss: () -> Unit, onStart: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val steps = listOf(
        "센서 연결" to "센서 QR• 바코드 → 휴대폰과 블루투스 연결",
        "정수기 인식" to "모델 QR• 바코드 → 블루투스로 센서에 저장",
        "Wi-Fi 연결" to "공유기 QR → 센서가 Wi-Fi로 통신",
    )
    PloCareBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "카메라로 스캔",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = "이 순서대로 연결해요", style = typography.labelMidMedium, color = colors.GrayChateau)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEachIndexed { index, (title, detail) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.Black0Surface, RoundedCornerShape(12.dp))
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(colors.White, CircleShape)
                                .border(1.dp, colors.PrimaryTint, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.54).sp,
                                ),
                                color = colors.GradientMain,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = typography.bodyLarge, color = colors.TextMain)
                            Text(text = detail, style = typography.labelMidMedium, color = colors.Black50Muted)
                        }
                    }
                }
            }
            PloCareButton(
                text = "스캔 시작",
                onClick = onStart,
                size = PloCareButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
