package com.senplo.plocare.ui.consumer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.navigation.ConsumerTabItem
import com.senplo.plocare.ui.consumer.dashboard.HomeDashboardScreen
import com.senplo.plocare.ui.theme.PloCareColor
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.nav_filter_care
import plocare.shared.generated.resources.nav_home
import plocare.shared.generated.resources.nav_my_page
import plocare.shared.generated.resources.nav_water_report

@Composable
fun ConsumerMainScreen() {
    var currentTab by remember { mutableStateOf(ConsumerTabItem.DASHBOARD) }
    var filterCareTargetIds by remember { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        containerColor = PloCareColor.BgDeep,
        bottomBar = {
            NavigationBar(
                containerColor = PloCareColor.SurfaceDark,
                contentColor = PloCareColor.TextPrimary,
                tonalElevation = 8.dp
            ) {
                ConsumerTabItem.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconResource()),
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PloCareColor.AquaTeal,
                            selectedTextColor = PloCareColor.AquaTeal,
                            unselectedIconColor = PloCareColor.TextTertiary,
                            unselectedTextColor = PloCareColor.TextSecondary,
                            indicatorColor = PloCareColor.SurfaceCard
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PloCareColor.BgDeep, PloCareColor.BgMid)
                    )
                )
        ) {
            when (currentTab) {
                ConsumerTabItem.DASHBOARD -> HomeDashboardScreen(
                    onRequestReplacement = { filterIds ->
                        filterCareTargetIds = filterIds
                        currentTab = ConsumerTabItem.FILTER_CARE
                    },
                    onOpenDeviceSettings = {
                        currentTab = ConsumerTabItem.FILTER_CARE
                    },
                )
                ConsumerTabItem.FILTER_CARE -> FilterCareAndSettingsTab(
                    preselectedFilterIds = filterCareTargetIds,
                )
                ConsumerTabItem.WATER_REPORT -> WaterReportTab()
                ConsumerTabItem.MY_PAGE -> MyPageTab()
            }
        }
    }
}

private fun ConsumerTabItem.iconResource() = when (this) {
    ConsumerTabItem.DASHBOARD -> Res.drawable.nav_home
    ConsumerTabItem.FILTER_CARE -> Res.drawable.nav_filter_care
    ConsumerTabItem.WATER_REPORT -> Res.drawable.nav_water_report
    ConsumerTabItem.MY_PAGE -> Res.drawable.nav_my_page
}

@Composable
private fun FilterCareAndSettingsTab(preselectedFilterIds: List<String>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text(
            text = "필터 관리·설정",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (preselectedFilterIds.isEmpty()) {
                "실시간 유량 및 센서 누적 데이터 기준 잔여량"
            } else {
                "대시보드에서 선택한 필터로 교체 신청을 이어서 진행하세요."
            },
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        FilterStageCard(stage = "1단계", name = "세디먼트 카본 복합 필터", progress = 0.88f, dDay = "D-64")
        Spacer(modifier = Modifier.height(12.dp))
        FilterStageCard(stage = "2단계", name = "UF 중공사막 나노 필터", progress = 0.72f, dDay = "D-42")
        Spacer(modifier = Modifier.height(12.dp))
        FilterStageCard(stage = "3단계", name = "포스트 실버 항균 필터", progress = 0.94f, dDay = "D-110")

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* Order navigation */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PloCareColor.AquaTeal),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "PloCare 정품 필터 간편 교체 신청",
                color = PloCareColor.BgDeep,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------------------------------------------------
// 3. WATER REPORT TAB
// -------------------------------------------------------------
@Composable
private fun WaterReportTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "물 사용 리포트",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "우리 집 정수 사용량과 절감 효과를 확인하세요.",
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PloCareColor.SurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem("오늘", "4.8 L", PloCareColor.VividCyan)
                MetricItem("30일 누적", "142 L", PloCareColor.TextPrimary)
                MetricItem("전체 누적", "4,820 L", PloCareColor.AquaTeal)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ReportCard(
            title = "최근 14일 사용량",
            description = "일평균 4.7 L · 평균 사용량 기준으로 안정적입니다."
        )
        Spacer(modifier = Modifier.height(12.dp))
        ReportCard(
            title = "환경 절감 효과",
            description = "2 L 생수병 2,410개를 대체했어요."
        )
        Spacer(modifier = Modifier.height(12.dp))
        ReportCard(
            title = "필터 교체 이력",
            description = "2026.08.12 · 1단계 세디먼트 · 직접 교체"
        )
    }
}

// -------------------------------------------------------------
// 4. MY SETTINGS TAB
// -------------------------------------------------------------
@Composable
private fun MyPageTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "마이페이지",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PloCareColor.SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "연결된 PloCare 기기",
                    color = PloCareColor.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PloCare Smart Purifier Pro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "펌웨어 v2.4.1 (최신 버전)",
                    color = PloCareColor.VividCyan,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Subcomponents
@Composable
private fun ReportCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PloCareColor.SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                color = PloCareColor.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = PloCareColor.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, statusColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = PloCareColor.TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = statusColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FilterStageCard(stage: String, name: String, progress: Float, dDay: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PloCareColor.SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PloCareColor.SurfaceDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = stage, color = PloCareColor.AquaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(text = dDay, color = PloCareColor.VividCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PloCareColor.AquaTeal,
                trackColor = PloCareColor.SurfaceDark,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "권장 통수량 대비 잔여", color = PloCareColor.TextTertiary, fontSize = 11.sp)
                Text(text = "${(progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
