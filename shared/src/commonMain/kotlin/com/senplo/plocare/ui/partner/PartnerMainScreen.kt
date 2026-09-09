package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

private enum class PartnerTab(
    val title: String,
) {
    WORK("업무"),
    VISITS("방문 관리"),
    CUSTOMERS("고객 조회"),
    INVENTORY("재고 관리"),
    MY("마이페이지"),
}

private data class PartnerSection(
    val title: String,
    val description: String,
    val status: String? = null,
)

@Composable
fun PartnerMainScreen() {
    var currentTab by remember { mutableStateOf(PartnerTab.WORK) }

    Scaffold(
        containerColor = PloCareColor.BgDeep,
        bottomBar = {
            NavigationBar(
                containerColor = PloCareColor.SurfaceDark,
                contentColor = PloCareColor.TextPrimary,
                tonalElevation = 8.dp,
            ) {
                PartnerTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) PloCareColor.AquaTeal
                                        else PloCareColor.TextTertiary,
                                    ),
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PloCareColor.BgDeep, PloCareColor.BgMid),
                    ),
                ),
        ) {
            when (currentTab) {
                PartnerTab.WORK -> WorkDashboardTab()
                PartnerTab.VISITS -> VisitManagementTab()
                PartnerTab.CUSTOMERS -> CustomerManagementTab()
                PartnerTab.INVENTORY -> InventoryManagementTab()
                PartnerTab.MY -> PartnerMyPageTab()
            }
        }
    }
}

@Composable
private fun WorkDashboardTab() {
    PartnerTabContent(
        title = "오늘의 업무",
        subtitle = "담당 지역의 방문 일정과 긴급 작업을 확인하세요.",
        sections = listOf(
            PartnerSection("오늘 방문", "예정 8건 · 완료 1건", "긴급 2건"),
            PartnerSection("추천 방문 경로", "성수동 → 자양동 → 구의동", "경로 보기"),
            PartnerSection("차량 준비 재고", "세디먼트 6 · 프리카본 4 · RO 1", "확인 필요"),
        ),
    )
}

@Composable
private fun VisitManagementTab() {
    PartnerTabContent(
        title = "방문 관리",
        subtitle = "지도와 목록을 기준으로 오늘의 방문 순서를 관리합니다.",
        sections = listOf(
            PartnerSection("오후 2:00 · 고객 C-1024", "서울 성동구 · 필터 2개 교체", "긴급"),
            PartnerSection("오후 4:00 · 고객 C-2048", "서울 광진구 · 정기 점검", "예정"),
            PartnerSection("방문 경로", "추천 순서와 이동 시간을 확인합니다.", "지도 / 목록"),
        ),
    )
}

@Composable
private fun CustomerManagementTab() {
    PartnerTabContent(
        title = "고객 조회",
        subtitle = "고객 번호로 검색하거나 담당 지역 고객을 확인합니다.",
        sections = listOf(
            PartnerSection("고객 번호 검색", "고객 번호를 입력해 현장 작업 화면으로 이동합니다.", "검색"),
            PartnerSection("교체 긴급 고객", "사용률 100% 이상 고객을 우선 표시합니다.", "2명"),
            PartnerSection("담당 고객 목록", "성동구 · 광진구 담당 고객", "전체 보기"),
        ),
    )
}

@Composable
private fun InventoryManagementTab() {
    PartnerTabContent(
        title = "차량 재고 관리",
        subtitle = "오늘 작업에 필요한 필터와 현재 적재 수량을 비교합니다.",
        sections = listOf(
            PartnerSection("오늘 필요 수량", "세디먼트 6 · 프리카본 4 · RO 1", "자동 합산"),
            PartnerSection("현재 차량 재고", "세디먼트 8 · 프리카본 5 · RO 1", "준비 완료"),
            PartnerSection("수동 재고 등록", "추가 적재 또는 사용한 재고를 반영합니다.", "등록"),
        ),
    )
}

@Composable
private fun PartnerMyPageTab() {
    PartnerTabContent(
        title = "파트너 마이페이지",
        subtitle = "파트너 정보와 담당 지역 및 차량 정보를 관리합니다.",
        sections = listOf(
            PartnerSection("김파트너", "P-1004 · 현장 파트너", "승인 계정"),
            PartnerSection("담당 지역", "서울 성동구 · 광진구", "변경 요청"),
            PartnerSection("등록 차량", "스타리아 12가 3456", "수정"),
        ),
    )
}

@Composable
private fun PartnerTabContent(
    title: String,
    subtitle: String,
    sections: List<PartnerSection>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "PLOCARE PARTNER",
            color = PloCareColor.AquaTeal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            color = PloCareColor.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(24.dp))

        sections.forEachIndexed { index, section ->
            PartnerSectionCard(section)
            if (index != sections.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PartnerSectionCard(section: PartnerSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, PloCareColor.SurfaceBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    color = PloCareColor.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = section.description,
                    color = PloCareColor.TextSecondary,
                    fontSize = 12.sp,
                )
            }
            section.status?.let { status ->
                Spacer(Modifier.size(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PloCareColor.SurfaceDark)
                        .border(1.dp, PloCareColor.SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = status,
                        color = PloCareColor.AquaTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
