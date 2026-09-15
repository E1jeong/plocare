package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.buildDashboardSnapshot
import com.senplo.plocare.navigation.Route
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SensorDiagnosticsFormTest {
    private val timeZone = TimeZone.of("Asia/Seoul")
    private val now = Instant.parse("2026-09-10T03:00:00Z")
    private val today = LocalDate(2026, 9, 10)

    @Test
    fun diagnosticsRouteCarriesCustomerId() {
        val route = Route.SensorDiagnostics("C-1024")
        assertEquals("C-1024", route.customerId)
    }

    @Test
    fun kitchenFixtureShowsRecentTelemetryWithoutLiveClaim() {
        val snapshot = buildDashboardSnapshot(DashboardFixtures.kitchenPurifier(today, now), now, timeZone)
        val view = sensorDiagnosticsView(snapshot, now)
        assertEquals("2분 전 수신 기록", view.telemetryLabel)
        assertTrue(view.telemetryFresh)
        assertEquals("마지막 수신 기록 있음", view.wifiLabel)
        assertEquals("NB-IoT 모뎀 점검은 연결되지 않았습니다", view.nbiotLabel)
        assertEquals("실시간 신호 강도는 측정하지 않습니다", view.signalLabel)
        assertFalse(view.wifiLabel.contains("연결됨"))
        assertFalse(view.telemetryLabel.contains("정상"))
    }

    @Test
    fun officeFixtureShowsStaleTelemetryWithoutLiveClaim() {
        val snapshot = buildDashboardSnapshot(DashboardFixtures.officePurifier(today, now), now, timeZone)
        val view = sensorDiagnosticsView(snapshot, now)
        assertEquals("24시간 이상 미수신", view.telemetryLabel)
        assertFalse(view.telemetryFresh)
        assertEquals("24시간 이상 미수신", view.wifiLabel)
        assertEquals("NB-IoT 모뎀 점검은 연결되지 않았습니다", view.nbiotLabel)
        assertEquals("실시간 신호 강도는 측정하지 않습니다", view.signalLabel)
    }

    @Test
    fun validationRejectsIncompleteDraft() {
        val base = validDraft()
        assertEquals("현장 육안 확인을 모두 표시해 주세요.", validateSensorDiagnostics(base.copy(clampSeated = false)))
        assertEquals("현장 육안 확인을 모두 표시해 주세요.", validateSensorDiagnostics(base.copy(noLeak = false)))
        assertEquals("현장 육안 확인을 모두 표시해 주세요.", validateSensorDiagnostics(base.copy(commsObserved = false)))
    }

    @Test
    fun validationAcceptsCompleteDraft() {
        assertNull(validateSensorDiagnostics(validDraft()))
    }

    @Test
    fun confirmationCopyDoesNotClaimLiveSensor() {
        val detail = diagnosticsConfirmationDetail()
        assertTrue(detail.contains("미리보기"))
        assertTrue(detail.contains("실시간 센서 값은 읽지 않습니다"))
        assertTrue(detail.contains("연결되지 않았습니다"))
        assertTrue(detail.contains("서버에 저장되지 않습니다"))
        assertFalse(detail.contains("센서 연결됨"))
        assertFalse(detail.contains("측정 성공"))
        assertFalse(detail.contains("서버에 저장되었습니다"))
        assertEquals("점검 내용을 확인했습니다.", diagnosticsConfirmationTitle())
    }

    private fun validDraft() = SensorDiagnosticsDraft(
        clampSeated = true,
        noLeak = true,
        commsObserved = true,
    )
}
