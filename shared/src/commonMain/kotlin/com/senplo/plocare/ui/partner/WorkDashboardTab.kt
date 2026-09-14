package com.senplo.plocare.ui.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
internal fun WorkDashboardTab(
    onInventoryClick: () -> Unit,
    onOpenCustomer: (String) -> Unit,
) {
    val nextVisit = partnerVisits.first { !it.completed }
    PartnerPage("좋은 아침이에요, 김파트너님", "담당 지역 · 서울 성동구 / 광진구") {
        SectionTitle("오늘의 업무 요약", "9월 10일 목요일")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryMetric("예정", "8건", PloCareColor.VividCyan, Modifier.weight(1f))
            SummaryMetric("긴급", "2건", PloCareColor.StatusAlert, Modifier.weight(1f))
            SummaryMetric("완료", "1건", PloCareColor.StatusGood, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        InfoStrip("예상 이동 + 작업 시간", "4시간 10분")

        SectionTitle("다음 방문", "긴급 고객부터 현장 화면에 들어갑니다")
        PartnerCard(onClick = { onOpenCustomer(nextVisit.customerId) }) {
            Text(nextVisit.displayName, color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${nextVisit.time} · ${nextVisit.address}", color = PloCareColor.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("현장 화면 열기  ›", color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        SectionTitle("추천 방문 경로", "긴급도와 이동 시간을 반영했어요")
        PartnerCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("성수동 1가", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                RouteConnector("18분")
                Text("자양동", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                RouteConnector("12분")
                Text("구의동", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("경로 지도", Modifier.weight(1f))
                SmallAction("순서 조정", Modifier.weight(1f), secondary = true)
            }
        }

        SectionTitle("차량 준비 재고", "오늘 방문 8건 기준 자동 합산")
        PartnerCard(onClick = onInventoryClick) {
            StockSummaryRow("세디먼트", "6개")
            StockSummaryRow("프리카본", "4개")
            StockSummaryRow("RO 멤브레인", "1개")
            Spacer(Modifier.height(10.dp))
            Text("재고 관리에서 적재 확인  ›", color = PloCareColor.AquaTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(PloCareColor.SurfaceCard), border = BorderStroke(1.dp, PloCareColor.SurfaceBorder)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = PloCareColor.TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RowScope.RouteConnector(duration: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Spacer(Modifier.width(5.dp))
        Box(Modifier.weight(1f).height(1.dp).background(PloCareColor.SurfaceBorder))
        Text(duration, color = PloCareColor.TextTertiary, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 3.dp))
        Box(Modifier.weight(1f).height(1.dp).background(PloCareColor.SurfaceBorder))
        Spacer(Modifier.width(5.dp))
    }
}

@Composable
private fun StockSummaryRow(name: String, amount: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, color = PloCareColor.TextSecondary, fontSize = 13.sp)
        Text(amount, color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
