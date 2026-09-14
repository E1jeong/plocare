package com.senplo.plocare.ui.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.navigation.ConsumerTabItem
import com.senplo.plocare.ui.consumer.dashboard.HomeDashboardScreen
import com.senplo.plocare.ui.consumer.filtercare.FilterCareScreen
import com.senplo.plocare.ui.consumer.mypage.MyPageScreen
import com.senplo.plocare.ui.consumer.waterreport.WaterReportScreen
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.time.Instant
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.nav_filter_care
import plocare.shared.generated.resources.nav_home
import plocare.shared.generated.resources.nav_my_page
import plocare.shared.generated.resources.nav_water_report

@Composable
fun ConsumerMainScreen(
    currentTab: ConsumerTabItem,
    onTabChange: (ConsumerTabItem) -> Unit,
    filterCareTargetIds: List<String>,
    onFilterCareTargetIdsChange: (List<String>) -> Unit,
    snapshot: DashboardSnapshot,
    devices: List<PurifierDevice>,
    now: Instant,
    onSelectDevice: (String) -> Unit,
    onReplaceFilter: (String) -> Unit,
    onRefreshTelemetry: () -> Unit,
    onBookVisit: (List<String>) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {

    Scaffold(
        containerColor = PloCareColor.BrandNavy,
        bottomBar = {
            NavigationBar(
                containerColor = PloCareColor.SurfaceDark,
                contentColor = PloCareColor.TextPrimary,
                tonalElevation = 8.dp,
            ) {
                ConsumerTabItem.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabChange(tab) },
                        label = { Text(text = tab.title, fontSize = 10.sp) },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconResource()),
                                contentDescription = tab.title,
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
                .background(Brush.verticalGradient(listOf(PloCareColor.BrandNavy, PloCareColor.BgDeep)))
                .padding(innerPadding),
        ) {
            when (currentTab) {
                ConsumerTabItem.DASHBOARD -> HomeDashboardScreen(
                    devices = devices,
                    snapshot = snapshot,
                    now = now,
                    onSelectDevice = onSelectDevice,
                    onReplaceFilter = onReplaceFilter,
                    onRefreshTelemetry = onRefreshTelemetry,
                    onRequestReplacement = { filterIds ->
                        onFilterCareTargetIdsChange(filterIds)
                        onTabChange(ConsumerTabItem.FILTER_CARE)
                    },
                    onOpenDeviceSettings = onOpenSettings,
                )
                ConsumerTabItem.FILTER_CARE -> FilterCareScreen(
                    snapshot = snapshot,
                    preselectedFilterIds = filterCareTargetIds,
                    onBookVisit = onBookVisit,
                    onOpenSettings = onOpenSettings,
                )
                ConsumerTabItem.WATER_REPORT -> WaterReportScreen(device = snapshot.device)
                ConsumerTabItem.MY_PAGE -> MyPageScreen(
                    snapshot = snapshot,
                    now = now,
                    onOpenSettings = onOpenSettings,
                )
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
