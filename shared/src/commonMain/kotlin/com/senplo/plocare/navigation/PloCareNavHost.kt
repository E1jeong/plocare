package com.senplo.plocare.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senplo.plocare.ui.login.LoginScreen
import com.senplo.plocare.ui.login.PartnerSignupScreen
import com.senplo.plocare.ui.consumer.ConsumerMainScreen
import com.senplo.plocare.ui.partner.PartnerMainScreen
import com.senplo.plocare.ui.splash.SplashScreen
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareTheme

@Composable
fun PloCareNavHost(
    navController: NavHostController = rememberNavController(),
    audience: AppAudience = AppAudience.USER,
    onAudienceChange: (AppAudience) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280),
            )
        },
    ) {
        composable<Route.Splash> {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Route.Login) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Login> {
            LoginScreen(
                audience = audience,
                onAudienceChange = onAudienceChange,
                onGoogleLoginSuccess = {
                    onAudienceChange(AppAudience.USER)
                    navController.navigate(Route.ConsumerMain) {
                        popUpTo<Route.Login> { inclusive = true }
                    }
                },
                onPartnerLoginSuccess = {
                    onAudienceChange(AppAudience.PARTNER)
                    navController.navigate(Route.PartnerMain) {
                        popUpTo<Route.Login> { inclusive = true }
                    }
                },
                onPartnerSignupClick = {
                    onAudienceChange(AppAudience.PARTNER)
                    navController.navigate(Route.PartnerSignup)
                },
            )
        }

        composable<Route.PartnerSignup> {
            PartnerSignupScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.ConsumerMain> {
            PloCareTheme(audience = AppAudience.USER) {
                ConsumerMainScreen()
            }
        }

        composable<Route.PartnerMain> {
            PloCareTheme(audience = AppAudience.PARTNER) {
                PartnerMainScreen()
            }
        }
    }
}
