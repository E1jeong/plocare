package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class VisitManagementCopyTest {
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val zone = TimeZone.of("Asia/Seoul")
    private val views = partnerVisitViews(partnerVisits, partnerDevices(now, zone), now, zone)

    @Test
    fun currentVisitCardCopy() {
        val current = currentVisit(views)!!
        assertEquals("C-1024", current.customerId)
        assertEquals("2/4", currentVisitPosition(views))
        assertEquals("김*정 • C-1024", visitTitle(current.visit.maskedName, current.customerId))
        assertEquals("약 12분 • 고객 희망 오전 → 통화 후 도착 시간 입력", arrivalGuide(current.visit))
        assertEquals("안심번호 0504-***-5678\n방문 완료 24시간 뒤 만료", safeNumberLines(current.visit))
        assertEquals(listOf(1 to FilterType.SEDIMENT, 2 to FilterType.PRE_CARBON), visitTargetFilters(current))
        assertEquals("1단 세디먼트", stageFilterLabel(1, FilterType.SEDIMENT))
        assertTrue(maxExhaustionLabel(current).startsWith("최고 소진율 "))
    }

    @Test
    fun noOpenVisitHasNoPosition() {
        assertNull(currentVisitPosition(views.filter { it.completed }))
    }

    @Test
    fun dateOptionLabelsUseRealWeekdays() {
        val option = VisitDateOption(LocalDate(2026, 11, 9), "오후", 1)
        assertEquals("11/9(월) 오후", shortDateOptionLabel(option))
        assertEquals("11월 9일(월) 오후", dateOptionLabel(option))
        assertEquals("그날 1건", dateOptionDetail(option))
        assertEquals("그날 4건 • 오후 꽉 참", dateOptionDetail(option.copy(dayVisitCount = 4, note = "오후 꽉 참")))
    }

    @Test
    fun pendingRequestCopy() {
        val request = partnerVisitRequests(LocalDate(2026, 10, 30)).single()
        assertEquals(1, defaultDateOption(request))
        assertEquals("정*아 • 서울 성동구 뚝섬로 27 • 고객 희망 순서", dateSheetSubtitle(request))
        assertEquals("24시간 안에 확정해 주세요 • 남은 21시간", confirmDeadlineLabel(listOf(request)))
        assertNull(confirmDeadlineLabel(emptyList()))
        assertNull(defaultDateOption(request.copy(options = request.options.map { it.copy(available = false) })))
    }

    @Test
    fun schematicPointsStayInsideTheBoxWithNorthUp() {
        val points = schematicMapPoints(views)
        assertEquals(views.size, points.size)
        points.forEach { assertTrue(it.x in 0f..1f && it.y in 0f..1f) }
        val north = views.indices.maxBy { views[it].visit.latitude }
        assertEquals(0f, points[north].y)
        assertEquals(listOf(MapPoint(0.5f, 0.5f)), schematicMapPoints(views.take(1)))
    }
}
