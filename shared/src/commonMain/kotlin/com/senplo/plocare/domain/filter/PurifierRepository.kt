package com.senplo.plocare.domain.filter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class PurifierRepository(
    initialDevices: List<PurifierDevice>,
) {
    private val _devices = MutableStateFlow(initialDevices)
    val devices: StateFlow<List<PurifierDevice>> = _devices.asStateFlow()

    fun device(id: String): PurifierDevice? = _devices.value.find { it.id == id }

    fun replaceFilter(deviceId: String, filterId: String) {
        replaceFilters(deviceId, listOf(filterId))
    }

    fun replaceFilters(deviceId: String, filterIds: Collection<String>) {
        if (filterIds.isEmpty()) return
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.withReplacedFilters(filterIds) else device
        }
    }

    fun recordReplacement(
        deviceId: String,
        filterIds: Collection<String>,
        replacedOn: LocalDate,
        source: ReplacementSource,
        technicianMaskedName: String? = null,
    ) {
        if (filterIds.isEmpty()) return
        _devices.value = _devices.value.map { device ->
            if (device.id != deviceId) device else {
                val updated = device.withReplacedFilters(filterIds)
                updated.copy(
                    replacementHistory = listOf(
                        FilterReplacementRecord(
                            replacedOn = replacedOn,
                            filterIds = filterIds.distinct(),
                            source = source,
                            technicianMaskedName = technicianMaskedName,
                            cumulativeL = device.totalCumulativeL,
                        ),
                    ) + device.replacementHistory,
                )
            }
        }
    }

    fun requestVisit(deviceId: String, ticket: VisitTicket) {
        val device = requireNotNull(device(deviceId)) { "Unknown device: $deviceId" }
        require(ticket.targetFilterIds.isNotEmpty()) { "No filters selected" }
        require(ticket.targetFilterIds.all { id -> device.filters.any { it.id == id } }) {
            "Ticket contains a filter from another device"
        }
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.copy(visitTicket = ticket) else device
        }
    }

    fun renameDevice(deviceId: String, nickname: String) {
        val trimmed = nickname.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= 30) { "Nickname must be 1–30 characters" }
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.copy(nickname = trimmed) else device
        }
    }

    fun refreshTelemetry(deviceId: String, at: Instant) {
        _devices.value = _devices.value.map { device ->
            if (device.id == deviceId) device.withTelemetryAt(at) else device
        }
    }
}
