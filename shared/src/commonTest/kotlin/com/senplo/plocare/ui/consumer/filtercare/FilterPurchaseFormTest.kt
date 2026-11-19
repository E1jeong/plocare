package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterPurchaseFormTest {
    @Test
    fun purchaseRouteCarriesFilterIdsWithoutBackend() {
        val route = Route.FilterPurchase(encodeVisitFilterIds(listOf("kitchen-1", "kitchen-2")))
        assertEquals("kitchen-1,kitchen-2", route.filterIds)
        assertEquals(listOf("kitchen-1", "kitchen-2"), decodeVisitFilterIds(route.filterIds))
    }

    @Test
    fun localPricesStayOnFixtureCatalog() {
        assertEquals(24_000, purchaseSubtotalKrw(1, FilterSupplyMode.ONE_TIME))
        assertEquals(48_000, purchaseSubtotalKrw(2, FilterSupplyMode.ONE_TIME))
        assertEquals(21_600, purchaseSubtotalKrw(1, FilterSupplyMode.SUBSCRIPTION))
        assertEquals(43_200, purchaseSubtotalKrw(2, FilterSupplyMode.SUBSCRIPTION))
        assertEquals("24,000원", formatKrw(24_000))
    }

    @Test
    fun supplyModeCopyFollowsFigmaTabsAndPolicyDiscount() {
        assertEquals("1회 구매", supplyModeTabLabel(FilterSupplyMode.ONE_TIME))
        assertEquals("정기 구독 • 10% 할인", supplyModeTabLabel(FilterSupplyMode.SUBSCRIPTION))
        assertEquals("구매하기", purchaseCtaLabel(FilterSupplyMode.ONE_TIME))
        assertEquals("구독 시작하기", purchaseCtaLabel(FilterSupplyMode.SUBSCRIPTION))
    }

    @Test
    fun confirmationCopyDoesNotClaimBackendSuccess() {
        FilterSupplyMode.entries.forEach { mode ->
            val detail = purchaseConfirmationDetail(mode)
            assertTrue(detail.contains("미리보기"))
            assertTrue(detail.contains("연결되지 않았습니다"))
            assertTrue(detail.contains("서버에 저장되지 않습니다"))
            assertFalse(detail.contains("결제 완료"))
            assertFalse(detail.contains("주문 완료"))
            assertFalse(detail.contains("구독이 시작"))
        }
    }
}
