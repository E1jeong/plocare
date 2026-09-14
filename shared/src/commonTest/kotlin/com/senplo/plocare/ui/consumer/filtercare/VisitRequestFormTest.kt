package com.senplo.plocare.ui.consumer.filtercare

import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VisitRequestFormTest {
    private val thursday = LocalDate(2026, 9, 10)
    private val friday = LocalDate(2026, 9, 11)

    @Test
    fun earliestVisitSkipsWeekends() {
        assertEquals(LocalDate(2026, 9, 14), addBusinessDays(thursday, 2))
        assertEquals(LocalDate(2026, 9, 15), addBusinessDays(friday, 2))
    }

    @Test
    fun dateChipLabelDropsLeadingZeros() {
        assertEquals("9/14", visitDateChipLabel(LocalDate(2026, 9, 14)))
        assertEquals("12/3", visitDateChipLabel(LocalDate(2026, 12, 3)))
    }

    @Test
    fun dateOptionsStartFromEarliestBusinessDay() {
        val options = visitDateOptions(thursday, count = 5)
        assertEquals(LocalDate(2026, 9, 14), options.first())
        assertEquals(5, options.size)
        assertTrue(options.none { it.dayOfWeek.isoDayNumber >= 6 })
    }

    @Test
    fun validationRejectsIncompleteDraft() {
        val base = validDraft(thursday)
        assertEquals("주소를 입력해 주세요.", validateVisitRequest(base.copy(address = "  "), thursday))
        assertEquals("연락처를 입력해 주세요.", validateVisitRequest(base.copy(contact = "010-123"), thursday))
        assertEquals("교체할 필터를 선택해 주세요.", validateVisitRequest(base.copy(selectedFilterIds = emptySet()), thursday))
        assertEquals("희망 날짜를 선택해 주세요.", validateVisitRequest(base.copy(date = null), thursday))
        assertEquals(
            "방문일은 영업일 기준 이틀 이후부터 선택할 수 있습니다.",
            validateVisitRequest(base.copy(date = LocalDate(2026, 9, 11)), thursday),
        )
        assertEquals(
            "주말에는 방문 예약이 불가합니다.",
            validateVisitRequest(base.copy(date = LocalDate(2026, 9, 19)), thursday),
        )
        assertEquals("희망 시간대를 선택해 주세요.", validateVisitRequest(base.copy(window = null), thursday))
    }

    @Test
    fun validationAcceptsCompleteDraft() {
        assertNull(validateVisitRequest(validDraft(thursday), thursday))
    }

    @Test
    fun filterIdCodecRoundTrips() {
        assertEquals("kitchen-1,kitchen-2", encodeVisitFilterIds(listOf("kitchen-1", "kitchen-2")))
        assertEquals(listOf("kitchen-1", "kitchen-2"), decodeVisitFilterIds("kitchen-1,kitchen-2"))
        assertEquals(emptyList(), decodeVisitFilterIds(""))
    }

    private fun validDraft(today: LocalDate) = VisitRequestDraft(
        contact = "010-1234-5678",
        address = DEFAULT_VISIT_ADDRESS,
        selectedFilterIds = setOf("kitchen-1"),
        date = addBusinessDays(today, 2),
        window = VisitTimeWindow.AFTERNOON,
        notes = "주차 가능",
    )
}
