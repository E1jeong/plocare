package com.senplo.plocare.scanner

data class WifiQr(val ssid: String, val password: String)

/**
 * Parses the `WIFI:T:WPA;S:<ssid>;P:<password>;;` payload that routers and phone share sheets print.
 * Backslash escapes (`\;`, `\,`, `\:`, `\\`, `\"`) are unescaped. Returns null without a non-empty SSID.
 */
fun parseWifiQr(text: String): WifiQr? {
    val trimmed = text.trim()
    if (!trimmed.startsWith("WIFI:", ignoreCase = true)) return null

    val fields = mutableMapOf<String, String>()
    val key = StringBuilder()
    val value = StringBuilder()
    var inValue = false
    var i = 5
    while (i < trimmed.length) {
        val c = trimmed[i]
        when {
            c == '\\' && i + 1 < trimmed.length -> {
                (if (inValue) value else key).append(trimmed[i + 1])
                i++
            }
            c == ':' && !inValue -> inValue = true
            c == ';' -> {
                if (key.isNotEmpty()) fields[key.toString().uppercase()] = value.toString()
                key.clear()
                value.clear()
                inValue = false
            }
            else -> (if (inValue) value else key).append(c)
        }
        i++
    }
    if (key.isNotEmpty() && inValue) fields[key.toString().uppercase()] = value.toString()

    val ssid = fields["S"]?.takeIf { it.isNotEmpty() } ?: return null
    return WifiQr(ssid = ssid, password = fields["P"].orEmpty())
}
