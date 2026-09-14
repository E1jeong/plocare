package com.senplo.plocare.ui.consumer

import androidx.lifecycle.SavedStateHandle
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.navigation.CONSUMER_DEVICE_ID_KEY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ConsumerDeviceSessionTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val now = Instant.parse("2026-09-10T03:00:00Z")

    @Test
    fun selectedDeviceIdPersistsAndSwitchDoesNotMutateOtherDevice() {
        val handle = SavedStateHandle()
        val session = ConsumerDeviceSession(handle, timeZone, now)
        assertEquals(DashboardFixtures.KITCHEN_ID, session.selectedId.value)

        session.select(DashboardFixtures.OFFICE_ID)
        assertEquals(DashboardFixtures.OFFICE_ID, handle[CONSUMER_DEVICE_ID_KEY])
        assertEquals(DashboardFixtures.OFFICE_ID, session.selectedDevice().id)

        session.replaceSelectedFilter("office-1")
        val office = session.devices.value.single { it.id == DashboardFixtures.OFFICE_ID }
        val kitchen = session.devices.value.single { it.id == DashboardFixtures.KITCHEN_ID }
        assertEquals(office.totalCumulativeL, office.filters.single { it.id == "office-1" }.baselineL)
        assertEquals(
            DashboardFixtures.kitchenPurifier(now.toLocalDateTime(timeZone).date, now)
                .filters.single { it.id == "kitchen-1" }.baselineL,
            kitchen.filters.single { it.id == "kitchen-1" }.baselineL,
            0.01,
        )
    }

    @Test
    fun unknownDeviceIdIsIgnored() {
        val session = ConsumerDeviceSession(SavedStateHandle(), timeZone, now)
        session.select("missing")
        assertEquals(DashboardFixtures.KITCHEN_ID, session.selectedId.value)
    }

    @Test
    fun selectedDeviceFallsBackWhenSavedIdIsUnknown() {
        val handle = SavedStateHandle(mapOf(CONSUMER_DEVICE_ID_KEY to "missing"))
        val session = ConsumerDeviceSession(handle, timeZone, now)
        assertEquals(DashboardFixtures.KITCHEN_ID, session.selectedDevice().id)
    }
}
