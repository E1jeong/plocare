package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import kotlinx.datetime.TimeZone
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class PartnerVisitFixturesTest {
    @Test
    fun findsCustomerByIdIgnoringCase() {
        val visit = assertNotNull(findPartnerVisit("c-1024"))
        assertEquals("C-1024", visit.customerId)
        assertEquals("김*정", visit.maskedName)
        assertEquals(DashboardFixtures.KITCHEN_ID, visit.deviceId)
        assertEquals(listOf("kitchen-1", "kitchen-2"), visit.targetFilterIds)
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

    @Test
    fun listCopyMatchesWorkspaceDeviceExhaustion() {
        val timeZone = TimeZone.of("Asia/Seoul")
        val now = Instant.parse("2026-09-10T03:00:00Z")
        val devices = partnerDevices(now, timeZone)
        val views = partnerVisitViews(partnerVisits, devices, now, timeZone)

        val kitchenView = views.single { it.customerId == "C-1024" }
        val kitchenSnap = assertNotNull(kitchenView.snapshot)
        val kitchenMax = householdMaxExhaustionPercent(kitchenSnap)
        assertEquals(kitchenMax, householdMaxExhaustionPercent(buildDashboardSnapshot(devices.single { it.id == DashboardFixtures.KITCHEN_ID }, now, timeZone)), 0.01)
        assertFalse(kitchenView.urgent)
        assertEquals("예정", kitchenView.status)
        assertEquals("최고 소진율 ${kitchenMax.roundToInt()}%", kitchenView.exhaustionLabel)
        assertTrue(kitchenMax <= 100.0)

        val officeView = views.single { it.customerId == "C-3072" }
        val officeSnap = assertNotNull(officeView.snapshot)
        val officeMax = householdMaxExhaustionPercent(officeSnap)
        assertTrue(officeView.urgent)
        assertEquals("긴급", officeView.status)
        assertEquals("최고 소진율 ${officeMax.roundToInt()}%", officeView.exhaustionLabel)
        assertTrue(officeMax > 100.0)
    }
}
