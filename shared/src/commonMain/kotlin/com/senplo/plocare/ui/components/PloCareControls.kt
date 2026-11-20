package com.senplo.plocare.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_check

@Composable
fun PloCareChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    val backgroundColor = if (selected) colors.PrimaryContainerLight else colors.Black0Surface
    val borderColor = if (selected) colors.GradientSecondary else colors.Black10
    val textColor = if (selected) colors.Primary else colors.Black50Muted

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            style = PloCareTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun <T> PloCareSegmentedTab(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val containerShape = RoundedCornerShape(8.dp)
    val itemShape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.Black0Surface, containerShape)
            .padding(8.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(itemShape)
                    .then(
                        if (selected) {
                            Modifier
                                .background(colors.White, itemShape)
                                .border(1.dp, colors.White.copy(alpha = 0.4f), itemShape)
                        } else {
                            Modifier
                        },
                    )
                    .selectable(selected = selected, role = Role.Tab) { onItemSelected(item) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(item),
                    style = PloCareTheme.typography.bodyLarge.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected) colors.Primary else colors.Black50Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun PloCareToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = PloCareColor
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.Primary else colors.Black10,
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 4.dp,
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(width = 40.dp, height = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(16.dp)
                .background(colors.White, CircleShape),
        )
    }
}

enum class PloCareCheckboxStyle {
    /** Figma `check=off`: subtle `#F6F8FD` box with `#DCE2E5` check mark when unchecked. */
    SubtleCheck,

    /** Figma `check=check3`: white `#FFFFFF` box with `#E5E9F1` border and no check mark when unchecked. */
    OutlineEmpty,
}

@Composable
fun PloCareCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    uncheckedStyle: PloCareCheckboxStyle = PloCareCheckboxStyle.SubtleCheck,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(6.dp))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        PloCareCheckMark(checked = checked, uncheckedStyle = uncheckedStyle)
    }
}

/**
 * Non-interactive Figma `circle/` mark (20dp box in a 24dp frame) for rows that own the
 * selection semantics themselves, such as selectable list rows.
 */
@Composable
fun PloCareCheckMark(
    checked: Boolean,
    modifier: Modifier = Modifier,
    uncheckedStyle: PloCareCheckboxStyle = PloCareCheckboxStyle.SubtleCheck,
    // Figma radio rows (127:1875) draw the same `circle/` marks as circles.
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val colors = PloCareColor
    val boxShape = shape
    val boxColor = when {
        checked -> colors.Primary
        uncheckedStyle == PloCareCheckboxStyle.SubtleCheck -> colors.Black0Surface
        else -> colors.White
    }
    val borderColor = if (!checked && uncheckedStyle == PloCareCheckboxStyle.OutlineEmpty) {
        colors.Black10
    } else {
        Color.Transparent
    }
    val checkMarkColor: Color? = when {
        checked -> colors.White
        uncheckedStyle == PloCareCheckboxStyle.SubtleCheck -> colors.Black20
        else -> null
    }

    Box(modifier = modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(boxColor, boxShape)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(1.dp, borderColor, boxShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checkMarkColor != null) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = null,
                    tint = checkMarkColor,
                )
            }
        }
    }
}
