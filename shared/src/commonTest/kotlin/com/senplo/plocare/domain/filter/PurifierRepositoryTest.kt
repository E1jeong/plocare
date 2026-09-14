package com.senplo.plocare.domain.filter

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class PurifierRepositoryTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val now = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun replaceFilterUpdatesOnlySelectedDeviceAndCartridge() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val kitchenBefore = repository.device(DashboardFixtures.KITCHEN_ID)!!
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!
        val kitchenStage2Before = kitchenBefore.filters.single { it.id == "kitchen-2" }.baselineL

        repository.replaceFilter(DashboardFixtures.KITCHEN_ID, "kitchen-1")

        val kitchen = repository.device(DashboardFixtures.KITCHEN_ID)!!
        val office = repository.device(DashboardFixtures.OFFICE_ID)!!
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.single { it.id == "kitchen-1" }.baselineL)
        assertEquals(kitchenStage2Before, kitchen.filters.single { it.id == "kitchen-2" }.baselineL)
        assertEquals(officeBefore.filters.first().baselineL, office.filters.first().baselineL)
        assertEquals(kitchenBefore.installedOn, kitchen.installedOn)
    }

    @Test
    fun refreshTelemetryTouchesOnlySelectedDevice() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!.lastTelemetryAt
        val later = Instant.parse("2026-09-10T04:00:00Z")

        repository.refreshTelemetry(DashboardFixtures.KITCHEN_ID, later)

        assertEquals(later, repository.device(DashboardFixtures.KITCHEN_ID)!!.lastTelemetryAt)
        assertEquals(officeBefore, repository.device(DashboardFixtures.OFFICE_ID)!!.lastTelemetryAt)
        assertNotEquals(now, later)
    }
}
