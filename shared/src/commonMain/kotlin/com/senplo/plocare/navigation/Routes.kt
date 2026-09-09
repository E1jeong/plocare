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
    data object ConsumerMain : Route

    @Serializable
    data object PartnerMain : Route
}

enum class ConsumerTabItem(
    val title: String
) {
    DASHBOARD("대시보드"),
    FILTER_CARE("필터 관리·설정"),
    WATER_REPORT("물 사용 리포트"),
    MY_PAGE("마이페이지")
}
