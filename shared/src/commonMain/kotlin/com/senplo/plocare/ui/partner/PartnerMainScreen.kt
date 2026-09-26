package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.navigation.PartnerTab
import com.senplo.plocare.ui.theme.PloCareColor
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.nav_home
import plocare.shared.generated.resources.nav_my_page
import plocare.shared.generated.resources.nav_partner_customers
import plocare.shared.generated.resources.nav_partner_inventory
import plocare.shared.generated.resources.nav_partner_visits

@Composable
internal fun PartnerMainScreen(
    currentTab: PartnerTab = PartnerTab.WORK,
    visitViews: List<PartnerVisitView> = emptyList(),
    loadedStock: Map<Int, Int> = emptyMap(),
    loadedConfirmed: Boolean = false,
    onAdjustStock: (Int, Int) -> Unit = { _, _ -> },
    onToggleLoadedConfirmation: () -> Unit = {},
    onTabChange: (PartnerTab) -> Unit = {},
    onOpenCustomer: (String) -> Unit = {},
) {

    Scaffold(
        containerColor = PloCareColor.BrandNavy,
        bottomBar = {
            NavigationBar(containerColor = PloCareColor.SurfaceDark, tonalElevation = 8.dp) {
                PartnerTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabChange(tab) },
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
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PloCareColor.BrandNavy, PloCareColor.BgDeep)))
                .padding(innerPadding),
        ) {
            when (currentTab) {
                PartnerTab.WORK -> WorkDashboardTab(
                    visitViews = visitViews,
                    onInventoryClick = { onTabChange(PartnerTab.INVENTORY) },
                    onOpenCustomer = onOpenCustomer,
                )
                PartnerTab.VISITS -> VisitManagementTab(visitViews = visitViews, onOpenCustomer = onOpenCustomer)
                PartnerTab.CUSTOMERS -> CustomerSearchTab(visitViews = visitViews, onOpenCustomer = onOpenCustomer)
                PartnerTab.INVENTORY -> InventoryTab(
                    visitViews,
                    loadedStock,
                    loadedConfirmed,
                    onAdjustStock,
                    onToggleLoadedConfirmation,
                )
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
