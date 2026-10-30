package com.senplo.plocare.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WifiQrTest {
    @Test
    fun parsesFieldsInAnyOrder() {
        assertEquals(WifiQr("Home", "secret123"), parseWifiQr("WIFI:T:WPA;S:Home;P:secret123;;"))
        assertEquals(WifiQr("Home", "secret123"), parseWifiQr("WIFI:P:secret123;S:Home;T:WPA;H:false;;"))
    }

    @Test
    fun unescapesReservedCharacters() {
        assertEquals(WifiQr("My;Net,1", "a:b\\c\"d"), parseWifiQr("""WIFI:S:My\;Net\,1;P:a\:b\\c\"d;;"""))
    }

    @Test
    fun openNetworkHasEmptyPasswordAndTrailingTerminatorIsOptional() {
        assertEquals(WifiQr("Cafe", ""), parseWifiQr("WIFI:T:nopass;S:Cafe;;"))
        assertEquals(WifiQr("Cafe", "pw"), parseWifiQr(" wifi:S:Cafe;P:pw "))
    }

    @Test
    fun rejectsNonWifiPayloadsAndMissingSsid() {
        assertNull(parseWifiQr("https://plocare.example/sensor/42"))
        assertNull(parseWifiQr("WIFI:T:WPA;P:secret;;"))
        assertNull(parseWifiQr("WIFI:S:;P:secret;;"))
        assertNull(parseWifiQr(""))
    }
}
