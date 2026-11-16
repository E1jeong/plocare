package com.senplo.plocare.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

enum class PloCareButtonSize {
    Small,
    Medium,
    Large,
}

enum class PloCareButtonVariant {
    Primary,
    Secondary,
}

@Composable
fun PloCareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: PloCareButtonSize = PloCareButtonSize.Medium,
    variant: PloCareButtonVariant = PloCareButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    PloCareButton(
        onClick = onClick,
        modifier = modifier,
        size = size,
        variant = variant,
        enabled = enabled,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Text(text = text)
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

@Composable
fun PloCareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: PloCareButtonSize = PloCareButtonSize.Medium,
    variant: PloCareButtonVariant = PloCareButtonVariant.Primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    val textStyle = when (size) {
        PloCareButtonSize.Small -> typography.labelMidMedium
        PloCareButtonSize.Medium -> typography.labelLarge
        PloCareButtonSize.Large -> typography.titleMid
    }

    val contentPadding = when (size) {
        PloCareButtonSize.Small -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        PloCareButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 14.dp)
        PloCareButtonSize.Large -> PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    }

    val sizeModifier = when (size) {
        PloCareButtonSize.Small -> Modifier.defaultMinSize(minHeight = 40.dp)
        PloCareButtonSize.Medium -> Modifier.defaultMinSize(minHeight = 48.dp)
        PloCareButtonSize.Large -> Modifier.height(56.dp)
    }

    val containerColor = when (variant) {
        PloCareButtonVariant.Primary -> colors.Primary
        PloCareButtonVariant.Secondary -> colors.Black0Surface
    }

    val contentColor = when (variant) {
        PloCareButtonVariant.Primary -> colors.Background
        PloCareButtonVariant.Secondary -> colors.TextMain
    }

    val border = if (enabled && variant == PloCareButtonVariant.Secondary) {
        BorderStroke(1.dp, colors.Black10)
    } else {
        null
    }

    Button(
        onClick = onClick,
        modifier = modifier.then(sizeModifier),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = border,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = colors.Black10,
            disabledContentColor = colors.Black30,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        ProvideTextStyle(textStyle) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
