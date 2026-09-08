package com.senplo.plocare.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object PartnerSignup : Route

    @Serializable
    data object MainTab : Route
}

enum class MainTabItem(
    val title: String,
    val iconName: String
) {
    HOME("대시보드", "home"),
    FILTER("필터 케어", "water_drop"),
    SERVICE("서비스 센터", "location_on"),
    MY("마이페이지", "person")
}
