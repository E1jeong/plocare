package com.senplo.plocare.ui.consumer

import androidx.lifecycle.SavedStateHandle
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.navigation.CONSUMER_DEVICE_ID_KEY
import com.senplo.plocare.ui.consumer.filtercare.VisitRequestDraft
import com.senplo.plocare.ui.consumer.filtercare.VisitTimeWindow
import kotlinx.datetime.LocalDate
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

    @Test
    fun visitRequestKeepsScheduleAndContactOnSelectedDevice() {
        val session = ConsumerDeviceSession(SavedStateHandle(), timeZone, now)
        session.select(DashboardFixtures.OFFICE_ID)

        session.requestVisit(
            VisitRequestDraft(
                contact = "010-1234-5678",
                address = "서울 광진구 아차산로 42",
                selectedFilterIds = setOf("office-1"),
                date = LocalDate(2026, 9, 28),
                window = VisitTimeWindow.AFTERNOON,
                notes = "주차장 이용",
            ),
        )

        val ticket = session.selectedDevice().visitTicket!!
        assertEquals(14, ticket.hour)
        assertEquals(16, ticket.windowEndHour)
        assertEquals("010-1234-5678", ticket.contact)
        assertEquals("서울 광진구 아차산로 42", ticket.address)
        assertEquals("주차장 이용", ticket.notes)
        assertEquals(listOf("office-1"), ticket.targetFilterIds)
        assertEquals(null, session.repository.device(DashboardFixtures.KITCHEN_ID)!!.visitTicket?.address)
    }
}
