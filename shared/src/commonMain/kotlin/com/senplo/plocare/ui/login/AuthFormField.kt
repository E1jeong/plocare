package com.senplo.plocare.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

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
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = if (trailingLabel != null && onTrailingClick != null) {
            {
                Text(
                    text = trailingLabel,
                    color = PloCareColor.AquaTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clickable(onClick = onTrailingClick),
                )
            }
        } else {
            null
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PloCareColor.TextPrimary,
            unfocusedTextColor = PloCareColor.TextPrimary,
            focusedBorderColor = PloCareColor.AquaTeal,
            unfocusedBorderColor = PloCareColor.SurfaceBorder,
            focusedLabelColor = PloCareColor.AquaTeal,
            unfocusedLabelColor = PloCareColor.TextSecondary,
            cursorColor = PloCareColor.AquaTeal,
            focusedContainerColor = PloCareColor.SurfaceCard,
            unfocusedContainerColor = PloCareColor.SurfaceCard,
        ),
    )
}
