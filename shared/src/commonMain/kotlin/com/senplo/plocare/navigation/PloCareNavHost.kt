package com.senplo.plocare.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senplo.plocare.ui.main.MainTabScreen
import com.senplo.plocare.ui.splash.SplashScreen

@Composable
fun PloCareNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable<Route.Splash> {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Route.MainTab) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.MainTab> {
            MainTabScreen()
        }
    }
}
