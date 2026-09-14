package com.senplo.plocare.ui.consumer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.PurifierRepository
import com.senplo.plocare.navigation.CONSUMER_DEVICE_ID_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

class ConsumerDeviceSession(
    private val savedStateHandle: SavedStateHandle,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now(),
    val repository: PurifierRepository = PurifierRepository(
        DashboardFixtures.devices(now, timeZone),
    ),
) : ViewModel() {
    val devices: StateFlow<List<PurifierDevice>> = repository.devices

    private val _now = MutableStateFlow(now)
    val now: StateFlow<Instant> = _now.asStateFlow()

    val selectedId: StateFlow<String> = savedStateHandle.getStateFlow(
        CONSUMER_DEVICE_ID_KEY,
        devices.value.first().id,
    )

    fun selectedDevice(): PurifierDevice =
        repository.device(selectedId.value) ?: devices.value.first()

    fun select(id: String) {
        if (repository.device(id) != null) {
            savedStateHandle[CONSUMER_DEVICE_ID_KEY] = id
        }
    }

    fun replaceSelectedFilter(filterId: String) {
        repository.replaceFilter(selectedDevice().id, filterId)
    }

    fun refreshSelectedTelemetry(at: Instant = Clock.System.now()) {
        _now.value = at
        repository.refreshTelemetry(selectedDevice().id, at)
    }
}
