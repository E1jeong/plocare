package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_verified_user

/** Figma `input field/box`: 14/18 label row, 8dp gap, then the input. */
@Composable
internal fun ContactField(
    label: String,
    modifier: Modifier = Modifier,
    labelTrailing: (@Composable () -> Unit)? = null,
    trailingAtEnd: Boolean = false,
    optional: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (trailingAtEnd) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = buildAnnotatedString {
                    append(label)
                    if (optional) withStyle(SpanStyle(color = PloCareColor.Black50Muted)) { append("(선택)") }
                },
                style = PloCareTheme.typography.labelMid,
                color = PloCareColor.TextMain,
            )
            labelTrailing?.invoke()
        }
        content()
    }
}

@Composable
internal fun ContactPhoneField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    safeNumberBadge: Boolean,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    ContactField(
        label = "연락처",
        trailingAtEnd = !safeNumberBadge,
        labelTrailing = {
            if (safeNumberBadge) {
                // Sheet 79:1713 badge (180:1073)
                Row(
                    modifier = Modifier
                        .background(colors.GradientMain.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_verified_user),
                        contentDescription = null,
                        tint = colors.GradientMain,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(text = "안심번호", style = typography.labelSmall, color = colors.GradientMain)
                }
            } else {
                Text(text = "*안심번호로 전달됩니다", style = typography.labelSmall, color = colors.GradientMain)
            }
        },
    ) {
        PloCareTextField(
            value = digits,
            onValueChange = { onDigitsChange(sanitizePhoneDigits(it)) },
            placeholder = "010-0000-0000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneNumberTransformation,
        )
    }
}

@Composable
internal fun ContactAddressFields(
    address: String,
    onAddressChange: (String) -> Unit,
    addressDetail: String,
    onAddressDetailChange: (String) -> Unit,
    onSearchAddress: () -> Unit,
) {
    ContactField(label = "주소") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PloCareTextField(
                value = address,
                onValueChange = onAddressChange,
                placeholder = "주소를 입력해주세요",
                modifier = Modifier.weight(1f),
            )
            PloCareButton(
                text = "주소 검색",
                onClick = onSearchAddress,
                size = PloCareButtonSize.Small,
                modifier = Modifier.height(48.dp),
            )
        }
        PloCareTextField(
            value = addressDetail,
            onValueChange = onAddressDetailChange,
            placeholder = "상세주소를 입력해주세요",
        )
    }
}

/** Figma consent rows (114:1839 / 180:1114): the row toggles, the chevron opens the terms. */
@Composable
internal fun ContactConsentRows(
    draft: ContactAddressDraft,
    onConsentChange: (ContactConsent, Boolean) -> Unit,
    onOpenTerms: (ContactConsent) -> Unit,
) {
    val colors = PloCareColor
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContactConsent.entries.forEach { consent ->
            val checked = draft.consented(consent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .toggleable(
                            value = checked,
                            role = Role.Checkbox,
                            onValueChange = { onConsentChange(consent, it) },
                        ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PloCareCheckMark(checked = checked)
                    Text(
                        text = consent.label,
                        style = PloCareTheme.typography.labelMidMedium,
                        color = colors.TextMain,
                        modifier = Modifier.weight(1f),
                    )
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_forward),
                    contentDescription = "${consent.label} 내용 보기",
                    tint = colors.Black50Muted,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button) { onOpenTerms(consent) },
                )
            }
        }
    }
}

@Stable
internal class SignatureState {
    val strokes = mutableStateListOf<List<Offset>>()
    val isSigned: Boolean get() = strokes.isNotEmpty()

    fun clear() = strokes.clear()
}

/** Figma 동의 서명 (114:1852 / 180:1127): a drawing pad with `다시 쓰기` at the bottom end. */
@Composable
internal fun ContactSignatureField(state: SignatureState, padHeight: Dp) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    ContactField(label = "동의 서명") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(padHeight)
                .clip(shape)
                .background(colors.Black0Surface, shape),
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .semantics {
                        contentDescription = "서명 입력란"
                        stateDescription = if (state.isSigned) "서명함" else "서명 전"
                    }
                    .pointerInput(state) {
                        detectDragGestures(
                            onDragStart = { start -> state.strokes.add(listOf(start)) },
                            onDrag = { change, _ ->
                                change.consume()
                                val last = state.strokes.lastIndex
                                if (last >= 0) state.strokes[last] = state.strokes[last] + change.position
                            },
                        )
                    },
            ) {
                val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                state.strokes.forEach { points ->
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, color = colors.TextMain, style = stroke)
                }
            }
            Text(
                text = "다시 쓰기",
                style = PloCareTheme.typography.labelLarge,
                color = colors.Primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(role = Role.Button, onClick = state::clear)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
internal fun ContactNoteField(
    label: String,
    note: String,
    onNoteChange: (String) -> Unit,
    showCounter: Boolean,
    optional: Boolean = false,
) {
    val colors = PloCareColor
    ContactField(
        label = label,
        trailingAtEnd = true,
        optional = optional,
        labelTrailing = if (showCounter) {
            { Text(text = contactNoteCounter(note), style = PloCareTheme.typography.labelMidMedium, color = colors.Primary) }
        } else {
            null
        },
    ) {
        PloCareTextField(
            value = note,
            onValueChange = { onNoteChange(limitContactNote(it)) },
            placeholder = "요청 사항을 입력해주세요",
        )
    }
}
