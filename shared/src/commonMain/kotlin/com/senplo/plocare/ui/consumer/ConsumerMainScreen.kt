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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.navigation.ConsumerTabItem
import com.senplo.plocare.ui.consumer.dashboard.HomeDashboardScreen
import com.senplo.plocare.ui.consumer.filtercare.FilterCareScreen
import com.senplo.plocare.ui.consumer.mypage.MyPageScreen
import com.senplo.plocare.ui.consumer.waterreport.WaterReportScreen
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
                tonalElevation = 8.dp,
            ) {
                ConsumerTabItem.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
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
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(PloCareColor.BgDeep, PloCareColor.BgMid))),
        ) {
            when (currentTab) {
                ConsumerTabItem.DASHBOARD -> HomeDashboardScreen(
                    onRequestReplacement = { filterIds ->
                        filterCareTargetIds = filterIds
                        currentTab = ConsumerTabItem.FILTER_CARE
                    },
                    onOpenDeviceSettings = { currentTab = ConsumerTabItem.FILTER_CARE },
                )
                ConsumerTabItem.FILTER_CARE -> FilterCareScreen(filterCareTargetIds)
                ConsumerTabItem.WATER_REPORT -> WaterReportScreen()
                ConsumerTabItem.MY_PAGE -> MyPageScreen()
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
