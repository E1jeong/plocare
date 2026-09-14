package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

private enum class VisitView(val title: String) { MAP("지도"), LIST("목록") }

@Composable
internal fun VisitManagementTab(onOpenCustomer: (String) -> Unit) {
    var selectedView by remember { mutableStateOf(VisitView.MAP) }

    PartnerPage("방문 관리", "오늘 8건 · 긴급 2건 · 완료 1건") {
        SegmentedToggle(VisitView.entries.map { it.title }, selectedView.ordinal) {
            selectedView = VisitView.entries[it]
        }
        Spacer(Modifier.height(16.dp))
        if (selectedView == VisitView.MAP) {
            MapSkeleton()
            Spacer(Modifier.height(12.dp))
            LegendRow()
        } else {
            InfoStrip("추천 순서", "길게 눌러 방문 순서를 조정할 수 있어요")
        }
        SectionTitle(if (selectedView == VisitView.MAP) "다음 방문" else "오늘의 방문 목록", "추천 경로 순")
        partnerVisits.forEach { visit ->
            VisitCard(visit, onOpenCustomer = { onOpenCustomer(visit.customerId) })
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SegmentedToggle(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PloCareColor.SurfaceDark).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, option ->
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                    .background(if (index == selectedIndex) PloCareColor.DeepBlue else Color.Transparent)
                    .clickable { onSelected(index) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(option, color = if (index == selectedIndex) Color.White else PloCareColor.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MapSkeleton() {
    Box(
        Modifier.fillMaxWidth().height(238.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(PloCareColor.SurfaceDark, PloCareColor.SurfaceCard)))
            .border(1.dp, PloCareColor.SurfaceBorder, RoundedCornerShape(18.dp)),
    ) {
        Box(Modifier.padding(start = 42.dp, top = 48.dp).width(250.dp).height(2.dp).background(PloCareColor.DeepBlue))
        Box(Modifier.padding(start = 148.dp, top = 90.dp).width(180.dp).height(2.dp).background(PloCareColor.DeepBlue))
        MapMarker("1", true, Modifier.align(Alignment.TopStart).padding(start = 36.dp, top = 34.dp))
        MapMarker("2", false, Modifier.align(Alignment.Center).padding(top = 16.dp))
        MapMarker("3", false, Modifier.align(Alignment.BottomEnd).padding(end = 48.dp, bottom = 38.dp))
        Text("Kakao Map 연동 예정", color = PloCareColor.TextTertiary, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomStart).padding(14.dp))
    }
}

@Composable
private fun MapMarker(label: String, urgent: Boolean, modifier: Modifier) {
    Box(
        modifier.size(30.dp).clip(CircleShape).background(if (urgent) PloCareColor.StatusAlert else PloCareColor.DeepBlue)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LegendRow() {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        LegendItem("긴급", PloCareColor.StatusAlert)
        LegendItem("일반", PloCareColor.DeepBlue)
        LegendItem("완료", PloCareColor.TextTertiary)
        Text("추천 경로", color = PloCareColor.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, color = PloCareColor.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun VisitCard(visit: PartnerVisitFixture, onOpenCustomer: () -> Unit) {
    PartnerCard(onClick = onOpenCustomer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(visit.time, color = if (visit.urgent) PloCareColor.StatusAlert else PloCareColor.VividCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                StatusPill(visit.status, when { visit.urgent -> PloCareColor.StatusAlert; visit.completed -> PloCareColor.TextTertiary; else -> PloCareColor.DeepBlue })
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(visit.displayName, color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                Text(visit.address, color = PloCareColor.TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${visit.filtersLabel} · ${visit.exhaustion}", color = if (visit.urgent) PloCareColor.StatusAlert else PloCareColor.TextTertiary, fontSize = 11.sp)
            }
            Text("›", color = PloCareColor.AquaTeal, fontSize = 22.sp)
        }
        if (!visit.completed) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("카카오내비", Modifier.weight(1f), secondary = true)
                SmallAction("방문 시작", Modifier.weight(1f), onClick = onOpenCustomer)
            }
        }
    }
}
