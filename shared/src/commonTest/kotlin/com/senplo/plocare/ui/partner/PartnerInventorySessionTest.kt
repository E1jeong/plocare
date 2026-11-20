package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class PartnerInventorySessionTest {
    private fun session() = PartnerSession(
        SavedStateHandle(),
        TimeZone.of("Asia/Seoul"),
        Instant.parse("2026-09-10T03:00:00Z"),
    )

    private fun PartnerSession.quantity(type: FilterType) = stockByType(stock.value)[type] ?: 0

    @Test
    fun startsWithTheFigmaSampleQuantities() {
        val session = session()
        assertEquals(listOf(3, 1, 2, 0), defaultStockTypes.map { session.quantity(it) })
        assertEquals(3, session.stockLog.value.size)
    }

    @Test
    fun manualEditsClampAndMergeIntoOneLogEntry() {
        val session = session()
        val sediment = session.stock.value.first { it.type == FilterType.SEDIMENT }.id
        val logBefore = session.stockLog.value.size

        session.adjustStock(sediment, 1)
        session.adjustStock(sediment, 1)
        assertEquals(5, session.quantity(FilterType.SEDIMENT))
        assertEquals(logBefore + 1, session.stockLog.value.size)
        assertEquals("직접 수정 +2", session.stockLog.value.first().text)
        assertEquals("기사 입력", session.stockLog.value.first().source)

        session.adjustStock(sediment, -2)
        assertEquals(logBefore, session.stockLog.value.size)

        session.setStockQuantity(sediment, -5)
        assertEquals(0, session.quantity(FilterType.SEDIMENT))
        session.setStockQuantity(sediment, 5000)
        assertEquals(MAX_STOCK_QUANTITY, session.quantity(FilterType.SEDIMENT))
    }

    @Test
    fun completedWorkConsumesStockByTypeAndLogsTheCustomer() {
        val session = session()
        assertTrue(session.completeWork("C-1024", workDraft()))

        assertEquals(2, session.quantity(FilterType.SEDIMENT))
        assertEquals(0, session.quantity(FilterType.PRE_CARBON))
        val head = session.stockLog.value.take(2)
        assertEquals(setOf("세디먼트 1 사용", "프리카본 1 사용"), head.map { it.text }.toSet())
        assertTrue(head.all { it.source == "C-1024 김*정" })
        assertFalse(session.completeWork("C-1024", workDraft()))
        assertEquals(2, session.quantity(FilterType.SEDIMENT))
    }

    @Test
    fun addedFiltersStartAtZeroAndRejectDuplicates() {
        val session = session()
        assertTrue(session.addStockItem("하나필터주식회사", "하나 UF 멤브레인"))
        val added = session.stock.value.last()
        assertEquals(0, added.quantity)
        assertEquals(FilterType.MEMBRANE, added.type)
        assertFalse(session.addStockItem("하나필터주식회사", "하나 UF 멤브레인"))
        assertFalse(session.addStockItem("", "하나 세디먼트"))
    }

    private fun workDraft() = WorkInformationDraft(
        selectedFilterIds = setOf("kitchen-1", "kitchen-2"),
        type = ReplacementType.BUNDLE,
        barcode = "",
        note = "",
        sensorClamped = true,
        noLeak = true,
        commsVerified = true,
    )
}
