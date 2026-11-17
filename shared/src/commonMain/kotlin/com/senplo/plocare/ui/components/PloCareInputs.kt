package com.senplo.plocare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_photo_add
import plocare.shared.generated.resources.ic_photo_remove

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
            .selectable(
                selected = state == PloCareDateCellState.Selected,
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
    removeLabel: String = "사진 삭제",
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
                .border(1.dp, colors.Black10, shape)
                .clickable(role = Role.Button, onClick = onAddClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_photo_add),
                contentDescription = null,
                tint = colors.Black40Ghost,
            )
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
                // Figma 120:1109: 24dp remove icon at x=72, y=3 inside the 100dp box
                Image(
                    painter = painterResource(Res.drawable.ic_photo_remove),
                    contentDescription = removeLabel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onRemoveClick),
                )
            }
        }
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
    val cornerRadius = 16.dp
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        containerColor = colors.White,
        contentColor = colors.TextMain,
        scrimColor = Color.Black.copy(alpha = 0.2f),
        // Figma handle 40:950: 58x5 bar, radius 2.5, #E5E9F1, 21dp top / 8dp bottom in a 34dp frame.
        // The sheet's top-only 1px #E5E9F1 stroke is drawn from here: the handle sits at the top of the
        // sheet content, centered across its full width, and the sheet surface clips to the 16dp corners.
        // (The ModalBottomSheet modifier sits outside the sheet's offset layout, so it cannot draw this.)
        dragHandle = {
            BoxWithConstraints {
                val sheetWidth = maxWidth
                Box(
                    modifier = Modifier
                        .size(width = 58.dp, height = 34.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val inset = strokeWidth / 2
                            val radius = cornerRadius.toPx() - inset
                            val width = sheetWidth.toPx()
                            val topEdge = Path().apply {
                                moveTo(inset, inset + radius)
                                arcTo(
                                    rect = Rect(inset, inset, inset + radius * 2, inset + radius * 2),
                                    startAngleDegrees = 180f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false,
                                )
                                lineTo(width - inset - radius, inset)
                                arcTo(
                                    rect = Rect(width - inset - radius * 2, inset, width - inset, inset + radius * 2),
                                    startAngleDegrees = 270f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false,
                                )
                            }
                            translate(left = -(width - size.width) / 2) {
                                drawPath(topEdge, color = colors.Black10, style = Stroke(width = strokeWidth))
                            }
                        },
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 21.dp)
                            .width(58.dp)
                            .height(5.dp)
                            .background(colors.Black10, RoundedCornerShape(100.dp)),
                    )
                }
            }
        },
        content = content,
    )
}
