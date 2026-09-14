package com.senplo.plocare.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.senplo.plocare.ui.partner.PartnerMainScreen
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

fun NavGraphBuilder.partnerGraph() {
    composable<Route.PartnerMain> {
        PloCareTheme(audience = AppAudience.PARTNER) {
            PartnerMainScreen()
        }
    }
}
