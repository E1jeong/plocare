package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import kotlinx.datetime.TimeZone
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class CustomerWorkspaceCopyTest {
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val zone = TimeZone.of("Asia/Seoul")
    private val devices = partnerDevices(now, zone)

    @Test
    fun rowsCoverEveryPurifierFilterWithTypeNames() {
        val visit = findPartnerVisit("C-1024")!!
        val snapshot = buildDashboardSnapshot(devices.single { it.id == visit.deviceId }, now, zone)
        val rows = workspaceFilterRows(snapshot, visit)

        assertEquals(listOf("1단 세디먼트", "2단 프리카본", "3단 멤브레인", "4단 포스트카본"), rows.map { it.label })
        rows.zip(snapshot.filters).forEach { (row, filter) ->
            assertEquals(filter.exhaustionPercent.roundToInt(), row.percent)
            assertEquals(row.exhausted, row.badge == "즉시 교체")
        }
    }

    @Test
    fun rowsFallBackToVisitSamplesWithoutPurifier() {
        val visit = findPartnerVisit("C-1024")!!
        val rows = workspaceFilterRows(null, visit)

        assertEquals("1단 세디먼트", rows.first().label)
        assertTrue(rows.first().exhausted)
        assertEquals(listOf(false, false, false), rows.drop(1).map { it.exhausted })
    }

    @Test
    fun bannerKindFollowsCompletionThenHouseholdUrgency() {
        val done = findPartnerVisit("C-4096")!!
        assertEquals(VisitBadgeKind.DONE, workspaceVisitBadge(null, done))
        val visit = findPartnerVisit("C-1024")!!
        assertEquals(VisitBadgeKind.URGENT, workspaceVisitBadge(null, visit))
        val snapshot = buildDashboardSnapshot(devices.single { it.id == visit.deviceId }, now, zone)
        val expected = if (householdIsUrgent(snapshot)) VisitBadgeKind.URGENT else VisitBadgeKind.NORMAL
        assertEquals(expected, workspaceVisitBadge(snapshot, visit))
    }
}
