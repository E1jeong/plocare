package com.senplo.plocare.ui.partner

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class InventoryRequirementsTest {
    @Test
    fun sumsOnlyOpenVisitTargetFilters() {
        val now = Instant.parse("2026-09-10T03:00:00Z")
        val zone = TimeZone.of("Asia/Seoul")
        val views = partnerVisitViews(partnerVisits, partnerDevices(now, zone), now, zone)

        assertEquals(mapOf(1 to 2, 2 to 1, 4 to 1), inventoryRequirements(views))
    }
}
