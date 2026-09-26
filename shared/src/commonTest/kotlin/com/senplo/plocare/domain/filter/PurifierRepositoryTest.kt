package com.senplo.plocare.domain.filter

import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
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
    fun replaceFiltersUpdatesOnlySelectedCartridges() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val kitchenBefore = repository.device(DashboardFixtures.KITCHEN_ID)!!
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!
        val stage3Before = kitchenBefore.filters.single { it.id == "kitchen-3" }.baselineL
        val stage4Before = kitchenBefore.filters.single { it.id == "kitchen-4" }.baselineL

        repository.replaceFilters(DashboardFixtures.KITCHEN_ID, listOf("kitchen-1", "kitchen-2"))

        val kitchen = repository.device(DashboardFixtures.KITCHEN_ID)!!
        val office = repository.device(DashboardFixtures.OFFICE_ID)!!
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.single { it.id == "kitchen-1" }.baselineL)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.single { it.id == "kitchen-2" }.baselineL)
        assertEquals(stage3Before, kitchen.filters.single { it.id == "kitchen-3" }.baselineL)
        assertEquals(stage4Before, kitchen.filters.single { it.id == "kitchen-4" }.baselineL)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.totalCumulativeL)
        assertEquals(officeBefore.filters.map { it.baselineL }, office.filters.map { it.baselineL })
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

    @Test
    fun recordedReplacementAddsHistoryWithoutChangingOtherDevices() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!
        val kitchenBefore = repository.device(DashboardFixtures.KITCHEN_ID)!!

        repository.recordReplacement(
            DashboardFixtures.KITCHEN_ID,
            listOf("kitchen-1"),
            LocalDate(2026, 9, 10),
            ReplacementSource.SELF,
        )

        val kitchen = repository.device(DashboardFixtures.KITCHEN_ID)!!
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.totalCumulativeL)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.first().baselineL)
        assertEquals(kitchenBefore.replacementHistory.size + 1, kitchen.replacementHistory.size)
        assertEquals(listOf("kitchen-1"), kitchen.replacementHistory.first().filterIds)
        assertEquals(ReplacementSource.SELF, kitchen.replacementHistory.first().source)
        assertEquals(officeBefore, repository.device(DashboardFixtures.OFFICE_ID))
    }

    @Test
    fun visitRequestUpdatesOnlySelectedDeviceTicket() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!
        val ticket = VisitTicket(LocalDate(2026, 9, 14), 10, 0, null, listOf("kitchen-1"))

        repository.requestVisit(DashboardFixtures.KITCHEN_ID, ticket)

        assertEquals(ticket, repository.device(DashboardFixtures.KITCHEN_ID)!!.visitTicket)
        assertEquals(officeBefore, repository.device(DashboardFixtures.OFFICE_ID))
    }

    @Test
    fun visitRequestRejectsFiltersOutsideSelectedDevice() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val ticket = VisitTicket(LocalDate(2026, 9, 14), 10, 0, null, listOf("office-1"))

        assertFailsWith<IllegalArgumentException> {
            repository.requestVisit(DashboardFixtures.KITCHEN_ID, ticket)
        }
        assertNotEquals(ticket, repository.device(DashboardFixtures.KITCHEN_ID)!!.visitTicket)
    }

    @Test
    fun renameDeviceRejectsBlankAndOnlyChangesSelectedNickname() {
        val repository = PurifierRepository(DashboardFixtures.devices(now, timeZone))
        val officeBefore = repository.device(DashboardFixtures.OFFICE_ID)!!

        assertFailsWith<IllegalArgumentException> {
            repository.renameDevice(DashboardFixtures.KITCHEN_ID, "   ")
        }
        repository.renameDevice(DashboardFixtures.KITCHEN_ID, "  우리 집 정수기  ")

        assertEquals("우리 집 정수기", repository.device(DashboardFixtures.KITCHEN_ID)!!.nickname)
        assertEquals(officeBefore, repository.device(DashboardFixtures.OFFICE_ID))
    }
}
