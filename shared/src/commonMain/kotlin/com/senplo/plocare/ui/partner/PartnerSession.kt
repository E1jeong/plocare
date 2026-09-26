package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.PurifierRepository
import com.senplo.plocare.domain.filter.ReplacementSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

internal class PartnerSession(
    savedStateHandle: SavedStateHandle,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: Instant = Clock.System.now(),
    val repository: PurifierRepository = PurifierRepository(partnerDevices(now, timeZone)),
) : ViewModel() {
    private val _now = MutableStateFlow(now)
    val now: StateFlow<Instant> = _now.asStateFlow()

    private val _visits = MutableStateFlow(partnerVisits)
    val visits: StateFlow<List<PartnerVisitFixture>> = _visits.asStateFlow()

    private val _workReports = MutableStateFlow<Map<String, WorkInformationDraft>>(emptyMap())
    val workReports: StateFlow<Map<String, WorkInformationDraft>> = _workReports.asStateFlow()

    private val _loadedStock = MutableStateFlow(mapOf(1 to 8, 2 to 5, 3 to 1, 4 to 4))
    val loadedStock: StateFlow<Map<Int, Int>> = _loadedStock.asStateFlow()
    private val _loadedConfirmed = MutableStateFlow(false)
    val loadedConfirmed: StateFlow<Boolean> = _loadedConfirmed.asStateFlow()

    fun adjustStock(stage: Int, delta: Int) {
        if (stage !in 1..4) return
        _loadedStock.value = _loadedStock.value + (stage to ((_loadedStock.value[stage] ?: 0) + delta).coerceAtLeast(0))
        _loadedConfirmed.value = false
    }

    fun toggleLoadedConfirmation() {
        val views = partnerVisitViews(_visits.value, repository.devices.value, _now.value, timeZone)
        if (!_loadedConfirmed.value && !canConfirmInventory(inventoryRequirements(views), _loadedStock.value)) return
        _loadedConfirmed.value = !_loadedConfirmed.value
    }

    fun visit(customerId: String): PartnerVisitFixture? = findPartnerVisit(customerId, _visits.value)

    fun deviceForVisit(customerId: String): PurifierDevice? {
        val visit = visit(customerId) ?: return null
        return repository.device(visit.deviceId)
    }

    fun completeWork(customerId: String, draft: WorkInformationDraft): Boolean {
        val current = visit(customerId) ?: return false
        if (current.completed || validateWorkInformation(draft) != null) return false
        val device = repository.device(current.deviceId) ?: return false
        val filterIds = draft.selectedFilterIds
        if (!filterIds.all { id -> device.filters.any { it.id == id } }) return false
        val selectedStages = device.filters.filter { it.id in filterIds }.map { it.stage }
        repository.recordReplacement(
            current.deviceId,
            filterIds,
            Clock.System.now().toLocalDateTime(timeZone).date,
            ReplacementSource.PARTNER,
            technicianMaskedName = "김*수",
        )
        _visits.value = _visits.value.map { item ->
            if (item.customerId != current.customerId) item
            else item.copy(
                completed = true,
                urgent = false,
                status = "완료",
                time = "완료",
                exhaustion = "교체 완료",
            )
        }
        _workReports.value = _workReports.value + (customerId to draft)
        selectedStages.groupingBy { it }.eachCount().forEach { (stage, count) ->
            _loadedStock.value = _loadedStock.value +
                (stage to ((_loadedStock.value[stage] ?: 0) - count).coerceAtLeast(0))
        }
        _loadedConfirmed.value = false
        return true
    }
}
