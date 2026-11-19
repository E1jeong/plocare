package com.senplo.plocare.ui.consumer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.senplo.plocare.domain.filter.DashboardFixtures
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.PurifierRepository
import com.senplo.plocare.domain.filter.ReplacementSource
import com.senplo.plocare.domain.filter.VisitTicket
import com.senplo.plocare.navigation.CONSUMER_DEVICE_ID_KEY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.senplo.plocare.ui.consumer.filtercare.VisitRequestDraft
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
        repository.recordReplacement(
            selectedDevice().id,
            listOf(filterId),
            Clock.System.now().toLocalDateTime(timeZone).date,
            ReplacementSource.SELF,
        )
    }

    internal fun requestVisit(draft: VisitRequestDraft) {
        // The local ticket holds one slot; the first preference stands in until center confirmation exists.
        val first = requireNotNull(draft.preferences.firstOrNull()) { "No visit preference" }
        repository.requestVisit(
            selectedDevice().id,
            VisitTicket(
                first.date, first.window.startHour, 0, null, draft.selectedFilterIds.toList(),
                windowEndHour = first.window.endHour,
                contact = draft.contact.trim(),
                address = draft.address.trim(),
            ),
        )
    }

    fun renameSelectedDevice(nickname: String) {
        repository.renameDevice(selectedDevice().id, nickname)
    }

    fun removeDevice(id: String) {
        repository.removeDevice(id)
        if (selectedId.value == id) {
            savedStateHandle[CONSUMER_DEVICE_ID_KEY] = devices.value.first().id
        }
    }

    fun refreshSelectedTelemetry(at: Instant = Clock.System.now()) {
        _now.value = at
        repository.refreshTelemetry(selectedDevice().id, at)
    }
}
