package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.domain.filter.PurifierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
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

    fun visit(customerId: String): PartnerVisitFixture? = findPartnerVisit(customerId, _visits.value)

    fun deviceForVisit(customerId: String): PurifierDevice? {
        val visit = visit(customerId) ?: return null
        return repository.device(visit.deviceId)
    }

    fun completeWork(customerId: String, filterIds: Set<String>): Boolean {
        val current = visit(customerId) ?: return false
        if (current.completed || filterIds.isEmpty()) return false
        repository.replaceFilters(current.deviceId, filterIds)
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
        return true
    }
}
