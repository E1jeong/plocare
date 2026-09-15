package com.senplo.plocare.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import com.senplo.plocare.ui.partner.CustomerWorkspaceScreen
import com.senplo.plocare.ui.partner.PartnerMainScreen
import com.senplo.plocare.ui.partner.PartnerSession
import com.senplo.plocare.ui.partner.WorkInformationScreen
import com.senplo.plocare.ui.partner.findPartnerVisit
import com.senplo.plocare.ui.partner.partnerVisitViews
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

internal const val PARTNER_TAB_KEY = "partnerTab"

fun NavGraphBuilder.partnerGraph(navController: NavHostController) {
    composable<Route.PartnerMain> { entry ->
        val session = partnerSession(entry)
        val tabName by entry.savedStateHandle
            .getStateFlow(PARTNER_TAB_KEY, PartnerTab.WORK.name)
            .collectAsState()
        val currentTab = runCatching { PartnerTab.valueOf(tabName) }
            .getOrDefault(PartnerTab.WORK)
        val visits by session.visits.collectAsState()
        val devices by session.repository.devices.collectAsState()
        val now by session.now.collectAsState()
        val visitViews = remember(visits, devices, now) {
            partnerVisitViews(visits, devices, now, session.timeZone)
        }
        PloCareTheme(audience = AppAudience.PARTNER) {
            PartnerMainScreen(
                currentTab = currentTab,
                visitViews = visitViews,
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
        val parent = navController.getBackStackEntry<Route.PartnerMain>()
        val session = partnerSession(parent)
        val visits by session.visits.collectAsState()
        val visit = findPartnerVisit(route.customerId, visits)
        val snapshot = session.workspaceSnapshot(route.customerId)
        PloCareTheme(audience = AppAudience.PARTNER) {
            CustomerWorkspaceScreen(
                customerId = route.customerId,
                visit = visit,
                snapshot = snapshot,
                onBack = { navController.popBackStack() },
                onOpenSettings = {
                    val device = session.deviceForVisit(route.customerId)
                    navController.navigate(
                        Route.DeviceSettings.partner(
                            customerId = route.customerId,
                            deviceId = device?.id ?: DashboardFixtures.KITCHEN_ID,
                            deviceNickname = device?.nickname.orEmpty(),
                        ),
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenWork = {
                    navController.navigate(Route.WorkInformation(route.customerId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }

    composable<Route.WorkInformation> { entry ->
        val route = entry.toRoute<Route.WorkInformation>()
        val parent = navController.getBackStackEntry<Route.PartnerMain>()
        val session = partnerSession(parent)
        val visits by session.visits.collectAsState()
        PloCareTheme(audience = AppAudience.PARTNER) {
            WorkInformationScreen(
                visit = findPartnerVisit(route.customerId, visits),
                snapshot = session.workspaceSnapshot(route.customerId),
                onBack = { navController.popBackStack() },
                onConfirmed = { filterIds ->
                    session.completeWork(route.customerId, filterIds)
                    navController.getBackStackEntry<Route.PartnerMain>()
                        .savedStateHandle[PARTNER_TAB_KEY] = PartnerTab.VISITS.name
                    navController.popBackStack<Route.PartnerMain>(inclusive = false)
                },
            )
        }
    }
}

@Composable
private fun partnerSession(owner: NavBackStackEntry): PartnerSession =
    viewModel(viewModelStoreOwner = owner) {
        PartnerSession(owner.savedStateHandle)
    }

@Composable
private fun PartnerSession.workspaceSnapshot(customerId: String): DashboardSnapshot? {
    val devices by repository.devices.collectAsState()
    val visits by visits.collectAsState()
    val now by now.collectAsState()
    val visit = findPartnerVisit(customerId, visits)
    val device = devices.find { it.id == visit?.deviceId }
    return remember(device, now) {
        device?.let { buildDashboardSnapshot(it, now, timeZone) }
    }
}
