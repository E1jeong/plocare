package com.senplo.plocare.scanner

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

/** Guide shape over the camera preview: wide for 1D model-label barcodes, square for QR codes. */
enum class ScanFrame { BARCODE, QR }

/**
 * Camera preview that reports decoded text for [frame] codes, possibly repeatedly for the same code.
 * Shows [permissionPrompt] until camera access is granted and [fallback] where no camera scanner exists.
 */
@Composable
expect fun CodeScannerView(
    frame: ScanFrame,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    permissionPrompt: @Composable (requestPermission: () -> Unit) -> Unit,
    fallback: @Composable () -> Unit,
)

/** Drawn over a live preview only, so the scanning label never shows during permission or fallback states. */
@Composable
internal fun ScanFrameOverlay(frame: ScanFrame, modifier: Modifier = Modifier) {
    val size = when (frame) {
        ScanFrame.BARCODE -> Modifier.size(width = 300.dp, height = 130.dp)
        ScanFrame.QR -> Modifier.size(240.dp)
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(size.border(2.dp, PloCareColor.AquaTeal, RoundedCornerShape(12.dp)))
        Spacer(Modifier.height(12.dp))
        Text("스캔 중…", color = PloCareColor.TextPrimary, fontSize = 13.sp)
    }
}
