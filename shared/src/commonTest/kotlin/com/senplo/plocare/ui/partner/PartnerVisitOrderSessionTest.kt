package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class PartnerVisitOrderSessionTest {
    private fun session() = PartnerSession(
        SavedStateHandle(),
        TimeZone.of("Asia/Seoul"),
        Instant.parse("2026-09-10T03:00:00Z"),
    )

    private fun PartnerSession.order() = visits.value.map { it.customerId }

    @Test
    fun startsInVisitOrderWithCompletedFirst() {
        assertEquals(listOf("C-4096", "C-1024", "C-2048", "C-3072"), session().order())
    }

    @Test
    fun movesOpenVisitsButNeverPastCompletedOnes() {
        val session = session()
        session.moveVisit("C-3072", -1)
        assertEquals(listOf("C-4096", "C-1024", "C-3072", "C-2048"), session.order())
        session.moveVisit("C-3072", -1)
        assertEquals(listOf("C-4096", "C-3072", "C-1024", "C-2048"), session.order())

        session.moveVisit("C-3072", -1)
        session.moveVisit("C-4096", 1)
        assertEquals(listOf("C-4096", "C-3072", "C-1024", "C-2048"), session.order())

        session.moveVisit("C-3072", 10)
        assertEquals(listOf("C-4096", "C-1024", "C-2048", "C-3072"), session.order())
    }

    @Test
    fun confirmingAvailableDateRemovesPendingRequest() {
        val session = session()
        val request = session.requests.value.single()

        assertFalse(request.options[0].available)
        assertFalse(session.confirmRequest(request.id, 0))
        assertFalse(session.confirmRequest(request.id, 5))
        assertTrue(session.confirmRequest(request.id, 1))
        assertTrue(session.requests.value.isEmpty())
        assertFalse(session.confirmRequest(request.id, 1))
    }
}
