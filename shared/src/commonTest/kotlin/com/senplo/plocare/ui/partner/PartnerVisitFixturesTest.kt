package com.senplo.plocare.ui.partner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PartnerVisitFixturesTest {
    @Test
    fun findsCustomerByIdIgnoringCase() {
        val visit = assertNotNull(findPartnerVisit("c-1024"))
        assertEquals("C-1024", visit.customerId)
        assertEquals("김*정", visit.maskedName)
        assertTrue(visit.urgent)
        assertEquals(listOf(1, 2, 3, 4), visit.filterStatuses.map { it.stage })
    }

    @Test
    fun searchMatchesCustomerNumberAndSkipsCompleted() {
        assertEquals(listOf("C-1024"), searchPartnerVisits("1024").map { it.customerId })
        assertTrue(searchPartnerVisits("").none { it.completed })
        assertTrue(searchPartnerVisits("C-4096").isEmpty())
        assertNull(findPartnerVisit("C-0000"))
        assertNull(findPartnerVisit("  "))
    }
}
