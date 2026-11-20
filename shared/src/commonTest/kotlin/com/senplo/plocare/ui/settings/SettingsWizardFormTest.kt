package com.senplo.plocare.ui.settings

import com.senplo.plocare.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsWizardFormTest {
    @Test
    fun consumerSettingsRouteNeverCarriesPartnerContext() {
        val route = Route.DeviceSettings.consumer("device-kitchen")
        assertEquals(Route.DeviceSettings.CONSUMER_SETTINGS_AUDIENCE, route.audience)
        assertEquals("", route.customerId)
        assertFalse(route.isPartnerContext)
    }

    @Test
    fun partnerSettingsRouteKeepsCustomerId() {
        val route = Route.DeviceSettings.partner("C-1024", "device-c-2048", "아차산 정수기")
        assertEquals("C-1024", route.customerId)
        assertEquals("device-c-2048", route.deviceId)
        assertEquals("아차산 정수기", route.deviceNickname)
        assertTrue(route.isPartnerContext)
    }

    @Test
    fun wizardHasFourStepsInFigmaOrder() {
        assertEquals(
            listOf("정수기 설정", "센서 연결", "정수기 연결", "통신 연결"),
            SettingsStep.entries.map { it.title },
        )
        val last = SettingsDraft(step = SettingsStep.NETWORK)
        assertEquals(SettingsStep.NETWORK, last.advance().step)
        assertEquals(SettingsStep.PURIFIER, SettingsDraft().back().step)
    }

    @Test
    fun eachScanStepNeedsItsCodeBeforeMovingOn() {
        assertTrue(SettingsDraft().canAdvance())

        val sensorStep = SettingsDraft(step = SettingsStep.SENSOR)
        assertFalse(sensorStep.canAdvance())
        val sensor = assertNotNull(sensorStep.applyScan(ScanTarget.SENSOR, "  A12 "))
        assertEquals("A12", sensor.sensorCode)
        assertTrue(sensor.canAdvance())

        val modelStep = sensor.advance()
        assertFalse(modelStep.canAdvance())
        val model = assertNotNull(modelStep.applyScan(ScanTarget.MODEL, "CHP-7300R"))
        assertEquals("CHP-7300R", model.scannedModelCode)
        assertEquals(sampleModelChoice, model.model)
        assertTrue(model.canAdvance())

        val networkStep = model.advance()
        assertFalse(networkStep.canAdvance())
        val wifi = assertNotNull(networkStep.applyScan(ScanTarget.WIFI, "WIFI:T:WPA;S:PloCare_5G;P:secret123;;"))
        assertEquals("PloCare_5G", wifi.wifiSsid)
        assertEquals("secret123", wifi.wifiPassword)
        assertTrue(wifi.canAdvance())
    }

    @Test
    fun unusableScanTextLeavesDraftUnchanged() {
        val draft = SettingsDraft()
        assertNull(draft.applyScan(ScanTarget.SENSOR, "   "))
        assertNull(draft.applyScan(ScanTarget.MODEL, ""))
        assertNull(draft.applyScan(ScanTarget.WIFI, "https://example.com"))
    }

    @Test
    fun summaryShowsUnknownUntilModelIsSet() {
        assertTrue(SettingsDraft().modelSummaryRows().all { it.second == UNKNOWN_VALUE })
        assertEquals(
            listOf("제조사 구분" to "대기업", "제조사" to "제조사 A", "모델명" to "A-IN900", "필터 구성" to "4개"),
            SettingsDraft(model = sampleModelChoice).modelSummaryRows(),
        )
        assertEquals("제조사 A• A-IN900 으로 인식했어요", sampleModelChoice.recognizedHeadline())
        assertEquals("필터 4개 구성이 자동으로 들어갑니다", sampleModelChoice.filterCountLine())
        assertEquals(
            listOf("1단 세디먼트", "2단 프리카본", "3단 포스트카본", "4단 UF 멤브레인"),
            sampleModelChoice.filters.map { it.label },
        )
    }
}
