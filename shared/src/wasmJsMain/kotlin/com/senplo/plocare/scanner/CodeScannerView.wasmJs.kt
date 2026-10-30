package com.senplo.plocare.scanner

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CodeScannerView(
    frame: ScanFrame,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier,
    permissionPrompt: @Composable (requestPermission: () -> Unit) -> Unit,
    fallback: @Composable () -> Unit,
) {
    Box(modifier) { fallback() }
}
