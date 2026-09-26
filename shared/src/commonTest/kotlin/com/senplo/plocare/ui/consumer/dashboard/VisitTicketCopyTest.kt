package com.senplo.plocare.ui.consumer.dashboard

import com.senplo.plocare.domain.filter.VisitTicket
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class VisitTicketCopyTest {
    @Test
    fun localRequestShowsSelectedTimeWindow() {
        val ticket = VisitTicket(
            LocalDate(2026, 9, 28), 10, 0, null, listOf("kitchen-1"),
            windowEndHour = 12,
        )

        assertTrue(visitWhen(ticket).contains("10:00–12:00"))
    }
}
