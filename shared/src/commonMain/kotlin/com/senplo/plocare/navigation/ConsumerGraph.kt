package com.senplo.plocare.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.senplo.plocare.ui.consumer.ConsumerMainScreen
import com.senplo.plocare.ui.consumer.filtercare.VisitRequestScreen
import com.senplo.plocare.ui.consumer.filtercare.decodeVisitFilterIds
import com.senplo.plocare.ui.consumer.filtercare.encodeVisitFilterIds
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

internal const val CONSUMER_TAB_KEY = "consumerTab"
internal const val CONSUMER_FILTER_IDS_KEY = "consumerFilterIds"

fun NavGraphBuilder.consumerGraph(navController: NavHostController) {
    composable<Route.ConsumerMain> { entry ->
        val tabName by entry.savedStateHandle
            .getStateFlow(CONSUMER_TAB_KEY, ConsumerTabItem.DASHBOARD.name)
            .collectAsState()
        val filterIdsRaw by entry.savedStateHandle
            .getStateFlow(CONSUMER_FILTER_IDS_KEY, "")
            .collectAsState()
        val currentTab = runCatching { ConsumerTabItem.valueOf(tabName) }
            .getOrDefault(ConsumerTabItem.DASHBOARD)
        PloCareTheme(audience = AppAudience.USER) {
            ConsumerMainScreen(
                currentTab = currentTab,
                onTabChange = { entry.savedStateHandle[CONSUMER_TAB_KEY] = it.name },
                filterCareTargetIds = decodeVisitFilterIds(filterIdsRaw),
                onFilterCareTargetIdsChange = {
                    entry.savedStateHandle[CONSUMER_FILTER_IDS_KEY] = encodeVisitFilterIds(it)
                },
                onBookVisit = { filterIds ->
                    navController.navigate(Route.VisitRequest(encodeVisitFilterIds(filterIds))) {
                        launchSingleTop = true
                    }
                },
            )
        }
    }

    composable<Route.VisitRequest> { entry ->
        val route = entry.toRoute<Route.VisitRequest>()
        VisitRequestScreen(
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
