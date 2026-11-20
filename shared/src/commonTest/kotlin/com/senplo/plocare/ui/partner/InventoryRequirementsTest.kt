package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class InventoryRequirementsTest {
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val zone = TimeZone.of("Asia/Seoul")
    private val views = partnerVisitViews(partnerVisits, partnerDevices(now, zone), now, zone)

    @Test
    fun sumsOnlyOpenVisitTargetFiltersByType() {
        assertEquals(
            mapOf(
                FilterType.SEDIMENT to 2,
                FilterType.PRE_CARBON to 1,
                FilterType.MEMBRANE to 1,
                FilterType.POST_CARBON to 1,
            ),
            inventoryRequirements(views),
        )
    }

    @Test
    fun shortageNamesTheFirstVisitThatNeedsTheMissingType() {
        val stock = defaultStockItems(
            mapOf(FilterType.SEDIMENT to 3, FilterType.PRE_CARBON to 1, FilterType.POST_CARBON to 2, FilterType.MEMBRANE to 0),
        )
        val shortage = stockShortages(views, stock).single()

        assertEquals(FilterType.MEMBRANE, shortage.type)
        assertEquals(1, shortage.missing)
        assertEquals("C-2048", shortage.visit?.customerId)
        assertEquals("멤브레인 1개 부족", shortageTitle(shortage))
        assertEquals("14:00 박*현 방문에 필요해요 • 맞는 업체에서 구매 후 수량을 올려 주세요", shortageDetail(shortage))
    }

    @Test
    fun addedLinesCountTowardTheirType() {
        val stock = defaultStockItems(mapOf(FilterType.SEDIMENT to 2, FilterType.PRE_CARBON to 1, FilterType.POST_CARBON to 1)) +
            StockItem("stock-5", "하나 UF 멤브레인", FilterType.fromName("하나 UF 멤브레인"), 1, "하나필터주식회사")

        assertEquals(1, stockByType(stock)[FilterType.MEMBRANE])
        assertEquals(emptyList(), stockShortages(views, stock))
    }

    @Test
    fun quantityParsingAndLabels() {
        assertEquals(12, parseStockQuantity("1a2"))
        assertEquals(123, parseStockQuantity("12345"))
        assertNull(parseStockQuantity(""))
        assertEquals("직접 수정 +4", manualEditLabel(4))
        assertEquals("직접 수정 -2", manualEditLabel(-2))
        assertEquals("10/3 09:05", stockLogTime(LocalDateTime(2026, 10, 3, 9, 5)))
    }
}
