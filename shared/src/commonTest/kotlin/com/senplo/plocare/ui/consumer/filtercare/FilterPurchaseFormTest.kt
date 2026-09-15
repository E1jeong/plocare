package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
    fun validationRejectsIncompleteDraft() {
        val base = validDraft()
        assertEquals("배송 주소를 입력해 주세요.", validateFilterPurchase(base.copy(address = "  ")))
        assertEquals("연락처를 입력해 주세요.", validateFilterPurchase(base.copy(contact = "010-123")))
        assertEquals(
            "구매할 필터를 선택해 주세요.",
            validateFilterPurchase(base.copy(selectedFilterIds = emptySet())),
        )
        assertEquals("구매 또는 구독을 선택해 주세요.", validateFilterPurchase(base.copy(mode = null)))
    }

    @Test
    fun validationAcceptsCompleteDraft() {
        assertNull(validateFilterPurchase(validDraft()))
        assertNull(validateFilterPurchase(validDraft().copy(mode = FilterSupplyMode.SUBSCRIPTION)))
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

    private fun validDraft() = FilterPurchaseDraft(
        contact = "010-1234-5678",
        address = DEFAULT_VISIT_ADDRESS,
        selectedFilterIds = setOf("kitchen-1"),
        mode = FilterSupplyMode.ONE_TIME,
    )
}
