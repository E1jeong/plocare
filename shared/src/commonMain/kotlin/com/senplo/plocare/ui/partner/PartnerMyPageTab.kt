package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
internal fun PartnerMyPageTab() {
    PartnerPage("마이페이지", "파트너 계정과 현장 업무 정보를 관리하세요") {
        PartnerCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(PloCareColor.DeepBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("김", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("김파트너", color = PloCareColor.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("P-1004 · 승인된 현장 파트너", color = PloCareColor.TextSecondary, fontSize = 12.sp)
                    Text("성동 플로케어 서비스센터", color = PloCareColor.AquaTeal, fontSize = 12.sp)
                }
            }
        }
        SectionTitle("업무 정보", null)
        SettingsRow("담당 지역", "서울 성동구 · 광진구")
        SettingsRow("등록 차량", "스타리아 12가 3456")
        SettingsRow("연락처", "010-****-4821")
        SectionTitle("서비스센터 지원", null)
        SettingsRow("배차 담당자", "성동센터 운영팀 · 02-****-1024")
        SettingsRow("공지사항", "새 공지 2건")
        SettingsRow("앱 정보", "버전 1.0.0")
        TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("로그아웃", color = PloCareColor.StatusAlert, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    PartnerCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, color = PloCareColor.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(value, color = PloCareColor.TextSecondary, fontSize = 12.sp)
            }
            Text("›", color = PloCareColor.TextTertiary, fontSize = 22.sp)
        }
    }
    Spacer(Modifier.height(9.dp))
}
