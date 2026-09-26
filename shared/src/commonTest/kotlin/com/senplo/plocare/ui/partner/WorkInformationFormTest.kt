package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.navigation.Route
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class WorkInformationFormTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val today = LocalDate(2026, 9, 10)

    @Test
    fun workRouteCarriesCustomerId() {
        val route = Route.WorkInformation("C-1024")
        assertEquals("C-1024", route.customerId)
    }

    @Test
    fun validationRejectsIncompleteDraft() {
        val base = validDraft()
        assertEquals("교체한 필터를 선택해 주세요.", validateWorkInformation(base.copy(selectedFilterIds = emptySet())))
        assertEquals("교체 유형을 선택해 주세요.", validateWorkInformation(base.copy(type = null)))
        assertEquals(
            "하드웨어 점검을 모두 확인해 주세요.",
            validateWorkInformation(base.copy(sensorClamped = false)),
        )
        assertEquals(
            "하드웨어 점검을 모두 확인해 주세요.",
            validateWorkInformation(base.copy(noLeak = false, commsVerified = false)),
        )
    }

    @Test
    fun validationAcceptsCompleteDraft() {
        assertNull(validateWorkInformation(validDraft()))
    }

    @Test
    fun baselinePreviewListsOnlySelectedFilters() {
        val device = DashboardFixtures.kitchenPurifier(today, now)
        val preview = baselineSafetyPreview(device, setOf("kitchen-1", "kitchen-2"))
        assertEquals(
            listOf("1단 세디먼트 카본" to device.totalCumulativeL, "2단 프리카본" to device.totalCumulativeL),
            preview.updated,
        )
        assertEquals(listOf("3단 RO 멤브레인", "4단 포스트 실버 항균"), preview.unchangedLabels)
        assertEquals(device.totalCumulativeL, preview.totalCumulativeL)
        assertEquals("4,820 L", formatCumulativeL(device.totalCumulativeL))
    }

    @Test
    fun confirmationCopyDoesNotClaimBackendSave() {
        val detail = workConfirmationDetail()
        assertTrue(detail.contains("선택한 필터의 기준점만"))
        assertTrue(detail.contains("서버에는 저장되지 않습니다"))
        assertFalse(detail.contains("서버에 저장되었습니다"))
    }

    @Test
    fun completeWorkUpdatesOnlySelectedBaselinesAndIsIdempotent() {
        val session = PartnerSession(SavedStateHandle(), timeZone, now)
        val kitchenBefore = session.repository.device(DashboardFixtures.KITCHEN_ID)!!
        val stage3Before = kitchenBefore.filters.single { it.id == "kitchen-3" }.baselineL
        val officeBefore = session.repository.device(DashboardFixtures.OFFICE_ID)!!

        val report = validDraft().copy(barcode = "SKU-123", note = "수압 정상")
        assertTrue(session.completeWork("C-1024", report))

        val kitchen = session.repository.device(DashboardFixtures.KITCHEN_ID)!!
        val office = session.repository.device(DashboardFixtures.OFFICE_ID)!!
        val visit = assertNotNull(session.visit("C-1024"))
        assertTrue(visit.completed)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.single { it.id == "kitchen-1" }.baselineL)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.filters.single { it.id == "kitchen-2" }.baselineL)
        assertEquals(stage3Before, kitchen.filters.single { it.id == "kitchen-3" }.baselineL)
        assertEquals(kitchenBefore.totalCumulativeL, kitchen.totalCumulativeL)
        assertEquals(officeBefore.filters.map { it.baselineL }, office.filters.map { it.baselineL })
        assertEquals(report, session.workReports.value["C-1024"])
        assertFalse(session.completeWork("C-1024", report))
        assertEquals(stage3Before, session.repository.device(DashboardFixtures.KITCHEN_ID)!!.filters.single { it.id == "kitchen-3" }.baselineL)
    }

    @Test
    fun c1024VisitUsesKitchenDevice() {
        val session = PartnerSession(SavedStateHandle(), timeZone, now)
        assertEquals(DashboardFixtures.KITCHEN_ID, session.deviceForVisit("C-1024")?.id)
        assertEquals("우리 집 주방 정수기", session.deviceForVisit("C-1024")?.nickname)
        assertEquals("아차산 정수기", session.deviceForVisit("C-2048")?.nickname)
    }

    private fun validDraft() = WorkInformationDraft(
        selectedFilterIds = setOf("kitchen-1", "kitchen-2"),
        type = ReplacementType.BUNDLE,
        barcode = "",
        note = "",
        sensorClamped = true,
        noLeak = true,
        commsVerified = true,
    )
}
