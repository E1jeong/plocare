package com.senplo.plocare.domain.filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

class PurifierRepository(
    initialDevices: List<PurifierDevice>,
) {
    private val _devices = MutableStateFlow(initialDevices)
    val devices: StateFlow<List<PurifierDevice>> = _devices.asStateFlow()

    fun device(id: String): PurifierDevice? = _devices.value.find { it.id == id }

    fun replaceFilter(deviceId: String, filterId: String) {
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.withSelfReplaced(filterId) else device
        }
    }

    fun refreshTelemetry(deviceId: String, at: Instant) {
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.withTelemetryAt(at) else device
        }
    }
}
