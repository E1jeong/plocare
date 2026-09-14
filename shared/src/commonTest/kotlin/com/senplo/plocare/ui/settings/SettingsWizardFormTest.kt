package com.senplo.plocare.ui.settings

import com.senplo.plocare.navigation.Route
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        val route = Route.DeviceSettings.partner("C-1024", "device-kitchen")
        assertEquals("C-1024", route.customerId)
        assertTrue(route.isPartnerContext)
    }

    @Test
    fun bleAndConnectivityAndFlowTestValidate() {
        val connected = SettingsDraft(connectedDeviceName = "PloCare Sensor A12", connectedDeviceId = "ble-a12")
        assertEquals("연결할 센서를 선택해 주세요.", validateSettingsStep(SettingsDraft()))
        assertNull(validateSettingsStep(connected))

        val wifi = connected.copy(step = SettingsStep.CONNECTIVITY, householdMode = true)
        assertEquals("Wi-Fi 이름을 입력해 주세요.", validateSettingsStep(wifi))
        assertEquals(
            "Wi-Fi 비밀번호를 입력해 주세요.",
            validateSettingsStep(wifi.copy(wifiSsid = "Home_5G")),
        )
        assertNull(validateSettingsStep(wifi.copy(wifiSsid = "Home_5G", wifiPassword = "secret")))

        val flow = connected.copy(step = SettingsStep.FLOW_TEST)
        assertEquals("측정 용량(mL)을 입력하거나 테스트를 건너뛰세요.", validateSettingsStep(flow))
        assertEquals("측정 용량은 500~2,000 mL만 허용됩니다.", validateSettingsStep(flow.copy(measuredMl = "100")))
        assertNull(validateSettingsStep(flow.copy(measuredMl = "900")))
        assertNull(validateSettingsStep(flow.copy(skippedFlowTest = true)))
    }

    @Test
    fun hydraulicRejectsOutOfRangePressure() {
        val draft = SettingsDraft(
            step = SettingsStep.HYDRAULIC,
            connectedDeviceName = "A12",
            pressureKgf = "9",
        )
        assertEquals("수압은 0.5~5.0 kgf/cm² 범위입니다.", validateSettingsStep(draft))
        assertNull(validateSettingsStep(draft.copy(pressureKgf = "2.0")))
    }
}
