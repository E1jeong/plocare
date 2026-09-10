package com.senplo.plocare.ui.consumer.filtercare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import com.senplo.plocare.ui.consumer.ConsumerCard
import com.senplo.plocare.ui.consumer.ConsumerScreenHeader
import com.senplo.plocare.ui.consumer.ConsumerSectionTitle
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock

@Composable
fun FilterCareScreen(preselectedFilterIds: List<String>) {
    val snapshot = remember {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        buildDashboardSnapshot(
            device = DashboardFixtures.kitchenPurifier(now.toLocalDateTime(timeZone).date, now),
            now = now,
            timeZone = timeZone,
        )
    }
    val attentionFilters = snapshot.filters.filter {
        if (preselectedFilterIds.isEmpty()) it.showReplacementRequest else it.id in preselectedFilterIds
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        ConsumerScreenHeader(
            title = "필터 관리·설정",
            subtitle = "${snapshot.device.nickname} · Cuckoo 4단계",
        )
        Spacer(Modifier.height(18.dp))
        DeviceSetupCard()
        Spacer(Modifier.height(22.dp))
        ConsumerSectionTitle(
            title = "관리가 필요한 필터",
            caption = if (preselectedFilterIds.isEmpty()) {
                "교체 시점에 가까운 필터를 자동으로 모아 보여드려요."
            } else {
                "대시보드에서 선택한 필터가 미리 선택되었습니다."
            },
        )
        Spacer(Modifier.height(10.dp))
        attentionFilters.forEach { filter ->
            FilterAttentionCard(filter)
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(10.dp))
        ConsumerSectionTitle(title = "교체 방법 선택")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ServiceOptionCard(
                emoji = "📦",
                title = "직접 교체",
                detail = "정품 필터 · 무료 배송",
                price = "24,000원부터",
                action = "구매 / 구독",
                primary = true,
                modifier = Modifier.weight(1f),
            )
            ServiceOptionCard(
                emoji = "🧑‍🔧",
                title = "방문 케어",
                detail = "교체 · 살균 · 점검",
                price = "39,000원부터",
                action = "방문 예약",
                primary = false,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        ConsumerCard(modifier = Modifier.fillMaxWidth()) {
            Text("정기 구독 혜택", color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("• 예상 교체일 5일 전 자동 배송", color = PloCareColor.TextSecondary, fontSize = 12.sp)
            Text("• 정품 필터 10% 할인 · 회수 키트 무료", color = PloCareColor.TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeviceSetupCard() {
    ConsumerCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚙", fontSize = 22.sp)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("정수기 환경 및 보정", color = PloCareColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("CP-IN900 · Wi-Fi 연결됨", color = PloCareColor.StatusGood, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("정수기 설정 및 1분 보정  ›", color = PloCareColor.AquaTeal, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FilterAttentionCard(filter: FilterSnapshot) {
    val accent = when (filter.colorLevel) {
        FilterColorLevel.SAFE -> PloCareColor.StatusGood
        FilterColorLevel.REPLACE_SOON -> PloCareColor.StatusWarn
        FilterColorLevel.EXHAUSTED -> PloCareColor.StatusAlert
    }
    val status = when (filter.colorLevel) {
        FilterColorLevel.SAFE -> "정상"
        FilterColorLevel.REPLACE_SOON -> "D-${filter.remainingDays}일"
        FilterColorLevel.EXHAUSTED -> "교체 필요"
    }
    ConsumerCard(modifier = Modifier.fillMaxWidth(), borderColor = accent.copy(alpha = 0.65f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${filter.stage}단", color = PloCareColor.AquaTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "  ${filter.name}",
                color = PloCareColor.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(status, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { (filter.exhaustionPercent / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
            color = accent,
            trackColor = PloCareColor.SurfaceDark,
            strokeCap = StrokeCap.Round,
        )
        Spacer(Modifier.height(7.dp))
        Text("권장 통수량의 ${filter.exhaustionPercent.roundToInt()}% 사용", color = PloCareColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun ServiceOptionCard(
    emoji: String,
    title: String,
    detail: String,
    price: String,
    action: String,
    primary: Boolean,
    modifier: Modifier,
) {
    ConsumerCard(modifier = modifier.height(190.dp)) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(title, color = PloCareColor.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(detail, color = PloCareColor.TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text(price, color = PloCareColor.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (primary) PloCareColor.AquaTeal else PloCareColor.DeepBlue,
            ),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            Text(action, color = PloCareColor.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
