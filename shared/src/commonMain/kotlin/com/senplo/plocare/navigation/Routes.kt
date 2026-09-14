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
    data class VisitRequest(
        val filterIds: String = "",
    ) : Route

    @Serializable
    data object PartnerMain : Route

    @Serializable
    data class CustomerWorkspace(
        val customerId: String,
    ) : Route
}

enum class ConsumerTabItem(
    val title: String
) {
    DASHBOARD("대시보드"),
    FILTER_CARE("필터 관리·설정"),
    WATER_REPORT("물 사용 리포트"),
    MY_PAGE("마이페이지")
}

enum class PartnerTab(
    val title: String
) {
    WORK("업무"),
    VISITS("방문 관리"),
    CUSTOMERS("고객 조회"),
    INVENTORY("재고 관리"),
    MY("마이페이지")
}
