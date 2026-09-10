package com.senplo.plocare.ui.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.nav_home
import plocare.shared.generated.resources.nav_my_page
import plocare.shared.generated.resources.nav_partner_customers
import plocare.shared.generated.resources.nav_partner_inventory
import plocare.shared.generated.resources.nav_partner_visits

private enum class PartnerTab(val title: String) {
    WORK("업무"), VISITS("방문 관리"), CUSTOMERS("고객 조회"), INVENTORY("재고 관리"), MY("마이페이지"),
}

private enum class VisitView(val title: String) { MAP("지도"), LIST("목록") }

private data class VisitFixture(
    val time: String,
    val customer: String,
    val address: String,
    val filters: String,
    val exhaustion: String,
    val status: String,
    val urgent: Boolean = false,
    val completed: Boolean = false,
)

private data class StockFixture(val name: String, val required: Int, val loaded: Int)

private val visits = listOf(
    VisitFixture("10:00", "김*정 · C-1024", "서울 성동구 성수일로 10", "세디먼트 · 프리카본", "최고 소진율 112%", "긴급", urgent = true),
    VisitFixture("14:00", "박*현 · C-2048", "서울 광진구 아차산로 42", "포스트카본", "소진율 88%", "예정"),
    VisitFixture("16:00", "이*수 · C-3072", "서울 성동구 왕십리로 18", "세디먼트", "소진율 91%", "예정"),
    VisitFixture("완료", "최*아 · C-4096", "서울 광진구 능동로 7", "RO 멤브레인", "교체 완료", "완료", completed = true),
)

private val stocks = listOf(
    StockFixture("1단계 세디먼트", 6, 8),
    StockFixture("2단계 프리카본", 4, 5),
    StockFixture("3단계 RO 멤브레인", 1, 1),
    StockFixture("4단계 포스트카본", 3, 4),
)

@Composable
fun PartnerMainScreen() {
    var currentTab by remember { mutableStateOf(PartnerTab.WORK) }

    Scaffold(
        containerColor = PloCareColor.BgDeep,
        bottomBar = {
            NavigationBar(containerColor = PloCareColor.SurfaceDark, tonalElevation = 8.dp) {
                PartnerTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconResource()),
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(tab.title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PloCareColor.AquaTeal,
                            selectedTextColor = PloCareColor.AquaTeal,
                            unselectedIconColor = PloCareColor.TextTertiary,
                            unselectedTextColor = PloCareColor.TextSecondary,
                            indicatorColor = PloCareColor.SurfaceCard,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .background(Brush.verticalGradient(listOf(PloCareColor.BgDeep, PloCareColor.BgMid))),
        ) {
            when (currentTab) {
                PartnerTab.WORK -> WorkDashboardTab(onInventoryClick = { currentTab = PartnerTab.INVENTORY })
                PartnerTab.VISITS -> VisitManagementTab()
                PartnerTab.CUSTOMERS -> CustomerManagementTab()
                PartnerTab.INVENTORY -> InventoryManagementTab()
                PartnerTab.MY -> PartnerMyPageTab()
            }
        }
    }
}

private fun PartnerTab.iconResource() = when (this) {
    PartnerTab.WORK -> Res.drawable.nav_home
    PartnerTab.VISITS -> Res.drawable.nav_partner_visits
    PartnerTab.CUSTOMERS -> Res.drawable.nav_partner_customers
    PartnerTab.INVENTORY -> Res.drawable.nav_partner_inventory
    PartnerTab.MY -> Res.drawable.nav_my_page
}

@Composable
private fun WorkDashboardTab(onInventoryClick: () -> Unit) {
    PartnerPage("좋은 아침이에요, 김파트너님", "담당 지역 · 서울 성동구 / 광진구") {
        SectionTitle("오늘의 업무 요약", "9월 10일 목요일")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryMetric("예정", "8건", PloCareColor.VividCyan, Modifier.weight(1f))
            SummaryMetric("긴급", "2건", PloCareColor.StatusAlert, Modifier.weight(1f))
            SummaryMetric("완료", "1건", PloCareColor.StatusGood, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        InfoStrip("예상 이동 + 작업 시간", "4시간 10분")

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
private fun VisitManagementTab() {
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
        visits.forEach { visit ->
            VisitCard(visit)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CustomerManagementTab() {
    var query by remember { mutableStateOf("") }
    val visibleCustomers = visits.filter {
        !it.completed && (query.isBlank() || it.customer.contains(query, ignoreCase = true))
    }

    PartnerPage("고객 조회", "고객 번호로 검색하거나 담당 고객을 확인하세요") {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("고객 번호") },
            placeholder = { Text("예: C-1024") },
            trailingIcon = { Text("검색", color = PloCareColor.AquaTeal, fontWeight = FontWeight.Bold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PloCareColor.TextPrimary,
                unfocusedTextColor = PloCareColor.TextPrimary,
                focusedBorderColor = PloCareColor.AquaTeal,
                unfocusedBorderColor = PloCareColor.SurfaceBorder,
                focusedLabelColor = PloCareColor.AquaTeal,
                unfocusedLabelColor = PloCareColor.TextSecondary,
                focusedContainerColor = PloCareColor.SurfaceDark,
                unfocusedContainerColor = PloCareColor.SurfaceDark,
            ),
        )
        SectionTitle("교체 긴급 고객", "필터 소진율 100% 이상")
        CustomerCard(visits.first())
        SectionTitle("담당 고객", "긴급도 높은 순 · ${visibleCustomers.size}명 표시")
        if (visibleCustomers.isEmpty()) {
            EmptySearchResult(query)
        } else {
            visibleCustomers.forEach { customer ->
                CustomerCard(customer)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun InventoryManagementTab() {
    var loadedConfirmed by remember { mutableStateOf(false) }

    PartnerPage("차량 재고 관리", "스타리아 12가 3456 · 김파트너") {
        SectionTitle("오늘 필요 수량", "방문 8건의 교체 예정 필터 자동 합산")
        PartnerCard { stocks.forEach { InventoryRow(it, showLoaded = false) } }
        SectionTitle("현재 트렁크 재고", "출발 전 부족 수량을 확인하세요")
        PartnerCard {
            stocks.forEach { InventoryRow(it, showLoaded = true) }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { loadedConfirmed = !loadedConfirmed },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loadedConfirmed) PloCareColor.StatusGood else PloCareColor.AquaTeal,
                    contentColor = PloCareColor.BgDeep,
                ),
            ) {
                Text(if (loadedConfirmed) "차량 적재 확인 완료" else "차량 적재 확인", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        PartnerCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("수동 재고 등록", color = PloCareColor.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("추가 적재 또는 현장 사용 수량을 반영합니다.", color = PloCareColor.TextSecondary, fontSize = 12.sp)
                }
                Text("등록  ›", color = PloCareColor.AquaTeal, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PartnerMyPageTab() {
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
private fun PartnerPage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("PLOCARE PARTNER", color = PloCareColor.AquaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, color = PloCareColor.TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = PloCareColor.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        content()
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SectionTitle(title: String, description: String?) {
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(title, color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        description?.let { Text(it, color = PloCareColor.TextTertiary, fontSize = 10.sp) }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun PartnerCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val modifier = if (onClick == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().clickable(onClick = onClick)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        border = BorderStroke(1.dp, PloCareColor.SurfaceBorder),
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp), content = content)
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
private fun InfoStrip(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PloCareColor.SurfaceDark).padding(13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = PloCareColor.TextSecondary, fontSize = 12.sp)
        Text(value, color = PloCareColor.VividCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
private fun SmallAction(text: String, modifier: Modifier = Modifier, secondary: Boolean = false) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (secondary) PloCareColor.SurfaceDark else PloCareColor.DeepBlue)
            .border(1.dp, if (secondary) PloCareColor.SurfaceBorder else PloCareColor.DeepBlue, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StockSummaryRow(name: String, amount: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, color = PloCareColor.TextSecondary, fontSize = 13.sp)
        Text(amount, color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
private fun VisitCard(visit: VisitFixture) {
    PartnerCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(visit.time, color = if (visit.urgent) PloCareColor.StatusAlert else PloCareColor.VividCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                StatusPill(visit.status, when { visit.urgent -> PloCareColor.StatusAlert; visit.completed -> PloCareColor.TextTertiary; else -> PloCareColor.DeepBlue })
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(visit.customer, color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                Text(visit.address, color = PloCareColor.TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${visit.filters} · ${visit.exhaustion}", color = if (visit.urgent) PloCareColor.StatusAlert else PloCareColor.TextTertiary, fontSize = 11.sp)
            }
            Text("›", color = PloCareColor.AquaTeal, fontSize = 22.sp)
        }
        if (!visit.completed) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("카카오내비", Modifier.weight(1f), secondary = true)
                SmallAction("방문 시작", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(Modifier.padding(top = 5.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.16f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CustomerCard(customer: VisitFixture) {
    PartnerCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.customer, color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                    if (customer.urgent) {
                        Spacer(Modifier.width(7.dp))
                        StatusPill("교체 긴급", PloCareColor.StatusAlert)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(customer.address, color = PloCareColor.TextSecondary, fontSize = 12.sp)
                Text("${customer.filters} · ${customer.exhaustion}", color = if (customer.urgent) PloCareColor.StatusAlert else PloCareColor.TextTertiary, fontSize = 11.sp)
            }
            Text("현장 화면  ›", color = PloCareColor.AquaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySearchResult(query: String) {
    PartnerCard {
        Text("검색 결과가 없습니다", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("‘$query’ 고객 번호를 다시 확인해 주세요.", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun InventoryRow(stock: StockFixture, showLoaded: Boolean) {
    val amount = if (showLoaded) stock.loaded else stock.required
    val state = when {
        !showLoaded -> "필요"
        stock.loaded > stock.required -> "여유"
        stock.loaded == stock.required -> "정확"
        else -> "부족"
    }
    val stateColor = when (state) {
        "여유" -> PloCareColor.StatusGood
        "정확" -> PloCareColor.StatusWarn
        "부족" -> PloCareColor.StatusAlert
        else -> PloCareColor.VividCyan
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stock.name, color = PloCareColor.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("${amount}개", color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        StatusPill(state, stateColor)
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
