package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WorkDashboardCopyTest {
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val zone = TimeZone.of("Asia/Seoul")
    private val views = partnerVisitViews(partnerVisits, partnerDevices(now, zone), now, zone)

    @Test
    fun summaryCountsMatchFigmaSample() {
        assertEquals(WorkSummary(scheduled = 2, urgent = 1, done = 1), workSummary(views))
    }

    @Test
    fun durationUsesHoursAndMinutes() {
        assertEquals("40분", workDurationLabel(40))
        assertEquals("1시간 40분", workDurationLabel(100))
        assertEquals("2시간", workDurationLabel(120))
        assertEquals("0분", workDurationLabel(0))
    }

    @Test
    fun dateLabelMatchesFigmaFormat() {
        assertEquals("10월 30일(금)", assignmentDateLabel(LocalDate(2026, 10, 30)))
    }

    @Test
    fun assignmentRowsKeepVisitOrderWithFirstOpenVisitCurrent() {
        val rows = assignmentRows(views)

        assertEquals(listOf("C-4096", "C-1024", "C-2048", "C-3072"), rows.map { it.view.customerId })
        assertEquals(listOf(1, 2, 3, 4), rows.map { it.order })
        rows.forEach { row ->
            val expected = when {
                row.view.completed -> VisitBadgeKind.DONE
                row.view.urgent -> VisitBadgeKind.URGENT
                else -> VisitBadgeKind.NORMAL
            }
            assertEquals(expected, row.badge, row.view.customerId)
        }
        assertEquals(VisitBadgeKind.DONE, rows.first().badge)
        assertEquals(listOf(false, true, false, false), rows.map { it.current })
        assertEquals(listOf("광진구", "성동구", "광진구", "성동구"), rows.map { it.district })
    }

    @Test
    fun districtFallsBackToEmpty() {
        assertEquals("", visitDistrict("서울"))
        assertEquals("", visitDistrict("  "))
    }

    @Test
    fun stockItemsFollowFigmaOrderAndReportShortage() {
        val items = vehicleStockItems(
            required = mapOf(FilterType.SEDIMENT to 2, FilterType.PRE_CARBON to 1, FilterType.MEMBRANE to 1, FilterType.POST_CARBON to 1),
            stocked = mapOf(FilterType.SEDIMENT to 2, FilterType.PRE_CARBON to 1, FilterType.POST_CARBON to 1),
        )

        assertEquals(
            listOf(FilterType.SEDIMENT, FilterType.PRE_CARBON, FilterType.POST_CARBON, FilterType.MEMBRANE),
            items.map { it.type },
        )
        assertEquals("멤브레인 1개 부족 • 출발 전 재고 관리에서 확인하세요", stockShortageMessage(items))
        assertNull(stockShortageMessage(vehicleStockItems(mapOf(FilterType.SEDIMENT to 2), mapOf(FilterType.SEDIMENT to 3))))
    }
}
