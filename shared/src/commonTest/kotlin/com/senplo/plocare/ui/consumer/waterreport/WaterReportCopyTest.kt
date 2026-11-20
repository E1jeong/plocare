package com.senplo.plocare.ui.consumer.waterreport

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.SamplePricingPolicy
import com.senplo.plocare.domain.filter.SavingsReportMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class WaterReportCopyTest {
    private val pricing = SamplePricingPolicy()

    @Test
    fun litersFollowFigmaFormat() {
        assertEquals("4.8L", litersLabel(4.79))
        assertEquals("5.0L", litersLabel(4.98))
        assertEquals("0.0L", litersLabel(0.0))
        assertEquals("1,497L", lifetimeLitersLabel(1_496.6))
    }

    @Test
    fun installedForUsesDaysThenMonthsThenYears() {
        val today = LocalDate(2026, 11, 20)
        assertEquals("설치 후 18일", installedForLabel(LocalDate(2026, 11, 2), today))
        assertEquals("설치 후 4개월", installedForLabel(LocalDate(2026, 7, 20), today))
        assertEquals("설치 후 1년", installedForLabel(LocalDate(2025, 11, 20), today))
        assertEquals("설치 후 0일", installedForLabel(LocalDate(2026, 11, 21), today))
    }

    @Test
    fun signedWonKeepsSignAndGrouping() {
        assertEquals("+48,000원", signedWonLabel(48_000))
        assertEquals("-51,500원", signedWonLabel(-51_500))
        assertEquals("0원", signedWonLabel(0))
    }

    @Test
    fun profitSampleReproducesFigmaRentalNote() {
        val profit = DashboardFixtures.sampleSavings(DashboardFixtures.KITCHEN_ID)
        assertEquals(SavingsReportMode.PROFIT, profit.mode)
        assertEquals(profit.scheduledReplacementCostKrw - profit.ploCareTotalCostKrw, profit.netSavingsKrw)
        assertEquals(
            "새 정수기 재렌탈(연 334,800원) 대비로는 174,800원 덜 냈어요 • 단순 비교 아님",
            savingsNote(profit, pricing),
        )
        assertNull(savingsNote(profit.copy(ploCareTotalCostKrw = 400_000), pricing))
    }

    @Test
    fun lossSampleReproducesFigmaBreakEvenNote() {
        val loss = DashboardFixtures.sampleSavings(DashboardFixtures.OFFICE_ID)
        assertEquals(SavingsReportMode.BREAK_EVEN_PENDING, loss.mode)
        assertEquals(loss.scheduledReplacementCostKrw - loss.ploCareTotalCostKrw, loss.netSavingsKrw)
        assertEquals(
            "아직은 모듈값이 더 커요 • 약 3개월 뒤(설치 6개월 차)부터 이득으로 바뀌어요",
            savingsNote(loss, pricing),
        )
    }

    @Test
    fun hygieneValueHasNoBreakEvenClaim() {
        val hygiene = DashboardFixtures.sampleSavings(DashboardFixtures.OFFICE_ID).copy(
            mode = SavingsReportMode.HYGIENE_VALUE,
            estimatedBreakEvenMonth = null,
            remainingMonthsToBreakEven = null,
        )
        assertEquals("사용량이 많아 아직 이득으로 바뀌는 시점을 예상할 수 없어요", savingsNote(hygiene, pricing))
    }
}
