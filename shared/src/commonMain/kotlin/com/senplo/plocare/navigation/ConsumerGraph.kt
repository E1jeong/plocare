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

internal const val CONSUMER_OPEN_TAB_KEY = "consumerOpenTab"

fun NavGraphBuilder.consumerGraph(navController: NavHostController) {
    composable<Route.ConsumerMain> { entry ->
        val pendingTabName by entry.savedStateHandle
            .getStateFlow(CONSUMER_OPEN_TAB_KEY, "")
            .collectAsState()
        val pendingTab = pendingTabName.takeIf { it.isNotEmpty() }?.let {
            runCatching { ConsumerTabItem.valueOf(it) }.getOrNull()
        }
        PloCareTheme(audience = AppAudience.USER) {
            ConsumerMainScreen(
                pendingTab = pendingTab,
                onPendingTabConsumed = { entry.savedStateHandle[CONSUMER_OPEN_TAB_KEY] = "" },
                onBookVisit = { filterIds ->
                    navController.navigate(Route.VisitRequest(encodeVisitFilterIds(filterIds)))
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
                    ?.set(CONSUMER_OPEN_TAB_KEY, ConsumerTabItem.DASHBOARD.name)
                navController.popBackStack()
            },
        )
    }
}
