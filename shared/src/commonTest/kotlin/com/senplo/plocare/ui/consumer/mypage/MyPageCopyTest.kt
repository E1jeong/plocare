package com.senplo.plocare.ui.consumer.mypage

import com.senplo.plocare.domain.filter.DashboardFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class MyPageCopyTest {
    private val now = Instant.parse("2026-11-20T03:00:00Z")
    private val devices = DashboardFixtures.devices(now, TimeZone.of("Asia/Seoul"))

    @Test
    fun purifierSummaryJoinsModelAndFilterCount() {
        val kitchen = devices.first { it.id == DashboardFixtures.KITCHEN_ID }
        assertEquals(
            "제조사 A • A-IN900 ${kitchen.filters.size}개 필터 • 센서 연결됨",
            purifierSummary(kitchen),
        )
    }

    @Test
    fun purifierSummaryStaysConnectedForStaleTelemetry() {
        val stale = devices.first().copy(lastTelemetryAt = now - 48.hours)
        assertEquals(true, purifierSummary(stale).endsWith("• 센서 연결됨"))
    }

    @Test
    fun purifierSummarySkipsMissingMakerAndModel() {
        val device = devices.first().copy(makerName = "", modelCode = " ")
        assertEquals("${device.filters.size}개 필터 • 센서 연결됨", purifierSummary(device))
        val modelOnly = devices.first().copy(makerName = "", modelCode = "A-IN900")
        assertEquals("A-IN900 ${modelOnly.filters.size}개 필터 • 센서 연결됨", purifierSummary(modelOnly))
    }
}
