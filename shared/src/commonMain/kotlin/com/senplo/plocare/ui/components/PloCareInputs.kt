package com.senplo.plocare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

@Composable
fun PloCareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    minHeightDp: Int = if (singleLine) 48 else 100,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(8.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = typography.labelLarge.copy(color = colors.TextMain),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(colors.Primary),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeightDp.dp)
            .background(colors.Black0Surface, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typography.labelLarge,
                            color = colors.Black40Ghost,
                        )
                    }
                    innerTextField()
                }
                if (trailingContent != null) {
                    trailingContent()
                }
            }
        },
    )
}

enum class PloCareDateCellState {
    Disabled,
    Active,
    Selected,
}

@Composable
fun PloCareDateCell(
    dayText: String,
    state: PloCareDateCellState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val textColor = when (state) {
        PloCareDateCellState.Disabled -> colors.Black30
        PloCareDateCellState.Active -> colors.Black60Body
        PloCareDateCellState.Selected -> colors.Primary
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (state == PloCareDateCellState.Selected) {
                    Modifier.background(colors.PrimaryTint, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = state != PloCareDateCellState.Disabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayText,
            style = PloCareTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PloCarePhotoBox(
    hasPhoto: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "사진 추가",
    onRemoveClick: (() -> Unit)? = null,
    photoContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)

    if (!hasPhoto) {
        Column(
            modifier = modifier
                .size(100.dp)
                .clip(shape)
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .clickable(role = Role.Button, onClick = onAddClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        ) {
            CameraAddIcon(tint = colors.Black40Ghost)
            Text(
                text = label,
                style = PloCareTheme.typography.labelMidMedium,
                color = colors.Black40Ghost,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(100.dp)
                .clip(shape)
                .background(colors.Black0Surface, shape),
        ) {
            if (photoContent != null) {
                photoContent()
            }
            if (onRemoveClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.Black90.copy(alpha = 0.65f), CircleShape)
                        .clickable(role = Role.Button, onClick = onRemoveClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        val stroke = 1.6.dp.toPx()
                        drawLine(
                            color = colors.White,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = colors.White,
                            start = Offset(size.width, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraAddIcon(tint: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(width = 20.dp, height = 18.dp)) {
        val stroke = 1.6.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, size.height * 0.2f),
            size = Size(size.width, size.height * 0.75f),
            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = tint,
            radius = size.width * 0.2f,
            center = Offset(size.width * 0.5f, size.height * 0.58f),
            style = Stroke(width = stroke),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PloCareBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PloCareColor
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.White,
        contentColor = colors.TextMain,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(colors.Black20, RoundedCornerShape(100.dp)),
            )
        },
        content = content,
    )
}
