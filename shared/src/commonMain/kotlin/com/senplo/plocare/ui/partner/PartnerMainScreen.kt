package com.senplo.plocare.ui.partner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.senplo.plocare.navigation.PartnerTab
import com.senplo.plocare.ui.components.PloCareBottomBar
import com.senplo.plocare.ui.components.PloCareBottomBarItem
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.gnb_inventory
import plocare.shared.generated.resources.gnb_mypage
import plocare.shared.generated.resources.gnb_visits
import plocare.shared.generated.resources.gnb_work

@Composable
internal fun PartnerMainScreen(
    today: LocalDate,
    currentTab: PartnerTab = PartnerTab.WORK,
    visitViews: List<PartnerVisitView> = emptyList(),
    requests: List<PartnerVisitRequest> = emptyList(),
    stock: List<StockItem> = emptyList(),
    stockLog: List<StockLogEntry> = emptyList(),
    onAdjustStock: (itemId: String, delta: Int) -> Unit = { _, _ -> },
    onSetStock: (itemId: String, quantity: Int) -> Unit = { _, _ -> },
    onAddStock: (maker: String, filterName: String) -> Boolean = { _, _ -> false },
    onTabChange: (PartnerTab) -> Unit = {},
    onOpenCustomer: (String) -> Unit = {},
    onMoveVisit: (customerId: String, offset: Int) -> Unit = { _, _ -> },
    onConfirmRequest: (requestId: String, optionIndex: Int) -> Boolean = { _, _ -> false },
) {
    // Figma GNB/partner (126:1033)
    val tabItems = PartnerTab.entries.map { tab ->
        PloCareBottomBarItem(id = tab, label = tab.title, icon = painterResource(tab.iconResource()))
    }

    Scaffold(
        containerColor = if (currentTab == PartnerTab.MY) PloCareColor.Background else PloCareColor.White,
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
                PartnerTab.WORK -> WorkDashboardTab(
                    visitViews = visitViews,
                    stock = stock,
                    today = today,
                    onReorderVisits = { onTabChange(PartnerTab.VISITS) },
                )
                PartnerTab.VISITS -> VisitManagementTab(
                    visitViews = visitViews,
                    requests = requests,
                    onOpenCustomer = onOpenCustomer,
                    onMoveVisit = onMoveVisit,
                    onConfirmRequest = onConfirmRequest,
                )
                PartnerTab.INVENTORY -> InventoryTab(
                    visitViews = visitViews,
                    stock = stock,
                    stockLog = stockLog,
                    onAdjustStock = onAdjustStock,
                    onSetStock = onSetStock,
                    onAddStock = onAddStock,
                )
                PartnerTab.MY -> PartnerMyPageTab(today = today)
            }
        }
    }
}

private fun PartnerTab.iconResource() = when (this) {
    PartnerTab.WORK -> Res.drawable.gnb_work
    PartnerTab.VISITS -> Res.drawable.gnb_visits
    PartnerTab.INVENTORY -> Res.drawable.gnb_inventory
    PartnerTab.MY -> Res.drawable.gnb_mypage
}
