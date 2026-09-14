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
import com.senplo.plocare.domain.filter.DashboardSnapshot
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import com.senplo.plocare.ui.consumer.ConsumerDeviceSession
import com.senplo.plocare.ui.consumer.ConsumerMainScreen
import com.senplo.plocare.ui.consumer.filtercare.VisitRequestScreen
import com.senplo.plocare.ui.consumer.filtercare.decodeVisitFilterIds
import com.senplo.plocare.ui.consumer.filtercare.encodeVisitFilterIds
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

internal const val CONSUMER_TAB_KEY = "consumerTab"
internal const val CONSUMER_FILTER_IDS_KEY = "consumerFilterIds"
internal const val CONSUMER_DEVICE_ID_KEY = "consumerDeviceId"

fun NavGraphBuilder.consumerGraph(navController: NavHostController) {
    composable<Route.ConsumerMain> { entry ->
        val session = consumerDeviceSession(entry)
        val tabName by entry.savedStateHandle
            .getStateFlow(CONSUMER_TAB_KEY, ConsumerTabItem.DASHBOARD.name)
            .collectAsState()
        val filterIdsRaw by entry.savedStateHandle
            .getStateFlow(CONSUMER_FILTER_IDS_KEY, "")
            .collectAsState()
        val currentTab = runCatching { ConsumerTabItem.valueOf(tabName) }
            .getOrDefault(ConsumerTabItem.DASHBOARD)
        val devices by session.devices.collectAsState()
        val now by session.now.collectAsState()
        val snapshot = session.dashboardSnapshot()
        PloCareTheme(audience = AppAudience.USER) {
            ConsumerMainScreen(
                currentTab = currentTab,
                onTabChange = { entry.savedStateHandle[CONSUMER_TAB_KEY] = it.name },
                filterCareTargetIds = decodeVisitFilterIds(filterIdsRaw),
                onFilterCareTargetIdsChange = {
                    entry.savedStateHandle[CONSUMER_FILTER_IDS_KEY] = encodeVisitFilterIds(it)
                },
                snapshot = snapshot,
                devices = devices,
                now = now,
                onSelectDevice = session::select,
                onReplaceFilter = session::replaceSelectedFilter,
                onRefreshTelemetry = { session.refreshSelectedTelemetry() },
                onBookVisit = { filterIds ->
                    navController.navigate(Route.VisitRequest(encodeVisitFilterIds(filterIds))) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = {
                    navController.navigate(Route.DeviceSettings.consumer(session.selectedId.value)) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }

    composable<Route.VisitRequest> { entry ->
        val route = entry.toRoute<Route.VisitRequest>()
        val parent = navController.getBackStackEntry<Route.ConsumerMain>()
        val session = consumerDeviceSession(parent)
        VisitRequestScreen(
            snapshot = session.dashboardSnapshot(),
            preselectedFilterIds = decodeVisitFilterIds(route.filterIds),
            onBack = { navController.popBackStack() },
            onSubmitted = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(CONSUMER_TAB_KEY, ConsumerTabItem.DASHBOARD.name)
                navController.popBackStack()
            },
        )
    }
}

@Composable
private fun consumerDeviceSession(owner: NavBackStackEntry): ConsumerDeviceSession =
    viewModel(viewModelStoreOwner = owner) {
        ConsumerDeviceSession(owner.savedStateHandle)
    }

@Composable
private fun ConsumerDeviceSession.dashboardSnapshot(): DashboardSnapshot {
    val devices by devices.collectAsState()
    val selectedId by selectedId.collectAsState()
    val now by now.collectAsState()
    val selected = devices.find { it.id == selectedId } ?: devices.first()
    return remember(selected, now) {
        buildDashboardSnapshot(selected, now, timeZone)
    }
}
