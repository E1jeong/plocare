package com.senplo.plocare.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.ui.partner.CustomerWorkspaceScreen
import com.senplo.plocare.ui.partner.PartnerMainScreen
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

internal const val PARTNER_TAB_KEY = "partnerTab"

fun NavGraphBuilder.partnerGraph(navController: NavHostController) {
    composable<Route.PartnerMain> { entry ->
        val tabName by entry.savedStateHandle
            .getStateFlow(PARTNER_TAB_KEY, PartnerTab.WORK.name)
            .collectAsState()
        val currentTab = runCatching { PartnerTab.valueOf(tabName) }
            .getOrDefault(PartnerTab.WORK)
        PloCareTheme(audience = AppAudience.PARTNER) {
            PartnerMainScreen(
                currentTab = currentTab,
                onTabChange = { entry.savedStateHandle[PARTNER_TAB_KEY] = it.name },
                onOpenCustomer = { customerId ->
                    navController.navigate(Route.CustomerWorkspace(customerId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }

    composable<Route.CustomerWorkspace> { entry ->
        val route = entry.toRoute<Route.CustomerWorkspace>()
        CustomerWorkspaceScreen(
            customerId = route.customerId,
            onBack = { navController.popBackStack() },
            onOpenSettings = {
                navController.navigate(
                    Route.DeviceSettings.partner(
                        customerId = route.customerId,
                        deviceId = DashboardFixtures.KITCHEN_ID,
                    ),
                ) {
                    launchSingleTop = true
                }
            },
        )
    }
}
