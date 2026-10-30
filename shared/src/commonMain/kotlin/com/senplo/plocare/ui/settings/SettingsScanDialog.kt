package com.senplo.plocare.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.senplo.plocare.scanner.CodeScannerView
import com.senplo.plocare.ui.theme.PloCareColor

/**
 * Full-screen camera scan for [target] with a manual fallback. [onCode] returns null when the
 * text was accepted (the caller then closes the dialog) or the error copy to show while scanning continues.
 */
@Composable
internal fun SettingsScanDialog(
    target: ScanTarget,
    onCode: (String) -> String?,
    onDismiss: () -> Unit,
) {
    var accepted by remember(target) { mutableStateOf(false) }
    var error by remember(target) { mutableStateOf<String?>(null) }
    var lastRejected by remember(target) { mutableStateOf<String?>(null) }
    var manual by remember(target) { mutableStateOf("") }

    fun submit(code: String, fromCamera: Boolean) {
        // The analyzer repeats every frame; skip a camera code that was already rejected.
        if (accepted || (fromCamera && code == lastRejected)) return
        if (!fromCamera && code.isBlank()) {
            error = "직접 입력할 값을 적어 주세요."
            return
        }
        val message = onCode(code)
        if (message == null) {
            accepted = true
        } else {
            error = message
            lastRejected = code
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxSize().background(PloCareColor.BrandNavy).padding(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(target.title, color = PloCareColor.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "닫기",
                    color = PloCareColor.AquaTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(target.guide, color = PloCareColor.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            CodeScannerView(
                frame = target.frame,
                onCodeScanned = { submit(it, fromCamera = true) },
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp)).background(PloCareColor.BgDeep),
                permissionPrompt = { requestPermission ->
                    ScanNotice(
                        "카메라 권한이 필요합니다",
                        "코드를 비추려면 카메라 접근을 허용해 주세요. 거부했다면 기기 설정에서 허용하거나 아래에서 직접 입력하세요.",
                    ) {
                        Button(
                            onClick = requestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                        ) {
                            Text("권한 허용", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                fallback = {
                    ScanNotice("카메라 스캔을 사용할 수 없습니다", "이 기기에서는 아래에서 직접 입력해 주세요.")
                },
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = PloCareColor.StatusAlert, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            val manualLabel = target.manualLabel
            if (manualLabel == null) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("직접 입력하기", color = PloCareColor.AquaTeal)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = manual,
                        onValueChange = { manual = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("$manualLabel 직접 입력") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PloCareColor.TextPrimary,
                            unfocusedTextColor = PloCareColor.TextPrimary,
                            focusedBorderColor = PloCareColor.AquaTeal,
                            unfocusedBorderColor = PloCareColor.SurfaceBorder,
                            focusedLabelColor = PloCareColor.AquaTeal,
                            unfocusedLabelColor = PloCareColor.TextSecondary,
                        ),
                    )
                    Button(
                        onClick = { submit(manual, fromCamera = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
                    ) {
                        Text("입력", color = PloCareColor.BgDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanNotice(title: String, body: String, action: (@Composable () -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(body, color = PloCareColor.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                action()
            }
        }
    }
}
