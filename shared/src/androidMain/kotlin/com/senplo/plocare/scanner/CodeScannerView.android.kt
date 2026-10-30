package com.senplo.plocare.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
actual fun CodeScannerView(
    frame: ScanFrame,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier,
    permissionPrompt: @Composable (requestPermission: () -> Unit) -> Unit,
    fallback: @Composable () -> Unit,
) {
    val context = LocalContext.current
    if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
        Box(modifier) { fallback() }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    // Re-check on resume so a grant or revoke made in system settings applies on return.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = context.hasCameraPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (!granted) {
        Box(modifier) { permissionPrompt { permissionLauncher.launch(Manifest.permission.CAMERA) } }
        return
    }

    val currentOnCodeScanned by rememberUpdatedState(onCodeScanned)
    val controller = remember { LifecycleCameraController(context) }

    DisposableEffect(controller, lifecycleOwner, frame) {
        val scanner = BarcodeScanning.getClient(frame.scannerOptions())
        val executor = ContextCompat.getMainExecutor(context)
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL, executor) { result ->
                result.getValue(scanner)?.firstNotNullOfOrNull { it.rawValue }?.let(currentOnCodeScanned)
            },
        )
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            scanner.close()
        }
    }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext -> PreviewView(viewContext).apply { this.controller = controller } },
        )
        ScanFrameOverlay(frame, Modifier.align(Alignment.Center))
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun ScanFrame.scannerOptions(): BarcodeScannerOptions = when (this) {
    ScanFrame.QR -> BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    ScanFrame.BARCODE -> BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_CODE_128,
        Barcode.FORMAT_CODE_39,
        Barcode.FORMAT_CODE_93,
        Barcode.FORMAT_CODABAR,
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_ITF,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
    ).build()
}
