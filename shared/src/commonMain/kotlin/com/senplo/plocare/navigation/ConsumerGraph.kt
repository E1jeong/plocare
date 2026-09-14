package com.senplo.plocare.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.senplo.plocare.ui.consumer.ConsumerMainScreen
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

fun NavGraphBuilder.consumerGraph() {
    composable<Route.ConsumerMain> {
        PloCareTheme(audience = AppAudience.USER) {
            ConsumerMainScreen()
        }
    }
}
