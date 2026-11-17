package com.senplo.plocare.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

/**
 * Auth form input built on the Figma input field (`PloCareTextField`). Partner login/signup have no
 * Figma frames, so they reuse the shared components and tokens.
 */
@Composable
internal fun AuthFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    showLabel: Boolean = true,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showLabel) {
            Text(
                text = label,
                color = colors.Black80Title,
                style = typography.labelMidMedium,
            )
        }
        PloCareTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (showLabel) "$label 입력" else label,
            modifier = Modifier.semantics { contentDescription = label },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            trailingContent = if (trailingLabel != null && onTrailingClick != null) {
                {
                    Text(
                        text = trailingLabel,
                        color = colors.Primary,
                        style = typography.labelMidMedium,
                        modifier = Modifier.clickable(role = Role.Button, onClick = onTrailingClick),
                    )
                }
            } else {
                null
            },
        )
    }
}
