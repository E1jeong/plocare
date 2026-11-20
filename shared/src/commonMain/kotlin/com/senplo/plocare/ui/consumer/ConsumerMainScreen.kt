package com.senplo.plocare.ui.consumer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.navigation.ConsumerTabItem
import com.senplo.plocare.ui.components.PloCareBottomBar
import com.senplo.plocare.ui.components.PloCareBottomBarItem
import com.senplo.plocare.ui.consumer.dashboard.HomeDashboardScreen
import com.senplo.plocare.ui.consumer.filtercare.FilterCareScreen
import com.senplo.plocare.ui.consumer.mypage.MyPageScreen
import com.senplo.plocare.ui.consumer.waterreport.WaterReportScreen
import com.senplo.plocare.ui.theme.PloCareColor
import kotlin.time.Instant
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.gnb_dashboard
import plocare.shared.generated.resources.gnb_filter
import plocare.shared.generated.resources.gnb_mypage
import plocare.shared.generated.resources.gnb_report

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
    onRefreshTelemetry: () -> Unit,
    onRemoveDevice: (String) -> Unit,
    onBookVisit: (List<String>) -> Unit = {},
    onOpenPurchase: (List<String>) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenDeviceSettings: (deviceId: String) -> Unit = {},
    onOpenContactAddress: () -> Unit = {},
) {
    // Figma GNB/customer (74:878)
    val tabItems = ConsumerTabItem.entries.map { tab ->
        PloCareBottomBarItem(id = tab, label = tab.title, icon = painterResource(tab.iconResource()))
    }

    Scaffold(
        containerColor = PloCareColor.Background,
        bottomBar = {
            PloCareBottomBar(
                items = tabItems,
                selectedId = currentTab,
                onSelect = onTabChange,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentTab) {
                ConsumerTabItem.DASHBOARD -> HomeDashboardScreen(
                    devices = devices,
                    snapshot = snapshot,
                    now = now,
                    onSelectDevice = onSelectDevice,
                    onRefreshTelemetry = onRefreshTelemetry,
                    onRequestReplacement = { filterIds ->
                        onFilterCareTargetIdsChange(filterIds)
                        onTabChange(ConsumerTabItem.FILTER_CARE)
                    },
                    onOpenDeviceSettings = onOpenSettings,
                    onRemoveDevice = onRemoveDevice,
                )
                ConsumerTabItem.FILTER_CARE -> FilterCareScreen(
                    snapshot = snapshot,
                    preselectedFilterIds = filterCareTargetIds,
                    onBookVisit = onBookVisit,
                    onOpenPurchase = onOpenPurchase,
                    onOpenSettings = onOpenSettings,
                )
                ConsumerTabItem.WATER_REPORT -> WaterReportScreen(snapshot = snapshot)
                ConsumerTabItem.MY_PAGE -> MyPageScreen(
                    devices = devices,
                    onOpenDeviceSettings = onOpenDeviceSettings,
                    onOpenContactAddress = onOpenContactAddress,
                    onBookVisit = { onBookVisit(emptyList()) },
                )
            }
        }
    }
}

private fun ConsumerTabItem.iconResource() = when (this) {
    ConsumerTabItem.DASHBOARD -> Res.drawable.gnb_dashboard
    ConsumerTabItem.FILTER_CARE -> Res.drawable.gnb_filter
    ConsumerTabItem.WATER_REPORT -> Res.drawable.gnb_report
    ConsumerTabItem.MY_PAGE -> Res.drawable.gnb_mypage
}
