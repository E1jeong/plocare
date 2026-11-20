package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.senplo.plocare.domain.filter.FilterType
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

    private val _requests = MutableStateFlow(partnerVisitRequests(now.toLocalDateTime(timeZone).date))
    val requests: StateFlow<List<PartnerVisitRequest>> = _requests.asStateFlow()

    private val _workReports = MutableStateFlow<Map<String, WorkInformationDraft>>(emptyMap())
    val workReports: StateFlow<Map<String, WorkInformationDraft>> = _workReports.asStateFlow()

    // Figma 18 sample quantities (sediment 3, pre-carbon 1, post-carbon 2, membrane/UF 0).
    private val _stock = MutableStateFlow(
        defaultStockItems(
            mapOf(FilterType.SEDIMENT to 3, FilterType.PRE_CARBON to 1, FilterType.POST_CARBON to 2, FilterType.MEMBRANE to 0),
        ),
    )
    val stock: StateFlow<List<StockItem>> = _stock.asStateFlow()

    private val _stockLog = MutableStateFlow(sampleStockLog(now, timeZone))
    val stockLog: StateFlow<List<StockLogEntry>> = _stockLog.asStateFlow()

    /** `−` / `+` on a stock line; manual edits are logged as `직접 수정 ±N` by the technician. */
    fun adjustStock(itemId: String, delta: Int) {
        val item = _stock.value.find { it.id == itemId } ?: return
        setStockQuantity(itemId, item.quantity + delta)
    }

    fun setStockQuantity(itemId: String, quantity: Int) {
        val item = _stock.value.find { it.id == itemId } ?: return
        val next = quantity.coerceIn(0, MAX_STOCK_QUANTITY)
        val delta = next - item.quantity
        if (delta == 0) return
        _stock.value = _stock.value.map { if (it.id == itemId) it.copy(quantity = next) else it }
        logManualEdit(itemId, delta)
    }

    /** `필터 추가` modal: registers a maker's filter with quantity 0 (annotation 180:1348). */
    fun addStockItem(maker: String, filterName: String): Boolean {
        if (maker.isBlank() || filterName.isBlank()) return false
        if (_stock.value.any { it.maker == maker && it.name == filterName }) return false
        val id = "stock-${_stock.value.size + 1}"
        _stock.value = _stock.value + StockItem(id, filterName, FilterType.fromName(filterName), 0, maker)
        return true
    }

    private fun logManualEdit(itemId: String, delta: Int) {
        val head = _stockLog.value.firstOrNull()
        val merged = if (head?.itemId == itemId) head.delta + delta else delta
        val rest = if (head?.itemId == itemId) _stockLog.value.drop(1) else _stockLog.value
        _stockLog.value = if (merged == 0) {
            rest
        } else {
            listOf(
                StockLogEntry(
                    timeLabel = stockLogTime(Clock.System.now(), timeZone),
                    text = manualEditLabel(merged),
                    source = "기사 입력",
                    itemId = itemId,
                    delta = merged,
                ),
            ) + rest
        }
    }

    /**
     * Moves an open visit one or more places within today's order. Completed visits stay where they are and
     * cannot be passed.
     */
    fun moveVisit(customerId: String, offset: Int) {
        val list = _visits.value
        val from = list.indexOfFirst { it.customerId == customerId }
        if (from < 0 || offset == 0 || list[from].completed) return
        val to = (from + offset).coerceIn(0, list.lastIndex)
        val span = if (to > from) (from + 1)..to else to until from
        if (span.any { list[it].completed }) return
        _visits.value = list.toMutableList().apply { add(to, removeAt(from)) }
    }

    /** Confirms one of the customer's preferred dates locally; the request leaves the pending list. */
    fun confirmRequest(requestId: String, optionIndex: Int): Boolean {
        val request = _requests.value.find { it.id == requestId } ?: return false
        val option = request.options.getOrNull(optionIndex) ?: return false
        if (!option.available) return false
        _requests.value = _requests.value - request
        return true
    }

    /** Takes used filters from the lines of that type, default line first; never below zero. */
    private fun consumeStock(type: FilterType, count: Int) {
        var left = count
        _stock.value = _stock.value.map { item ->
            if (item.type != type || left == 0) return@map item
            val take = minOf(left, item.quantity)
            left -= take
            item.copy(quantity = item.quantity - take)
        }
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
                exhaustion = "교체 완료",
            )
        }
        _workReports.value = _workReports.value + (customerId to draft)
        val used = device.filters.filter { it.id in filterIds }.groupingBy { it.filterType }.eachCount()
        used.forEach { (type, count) -> consumeStock(type, count) }
        val time = stockLogTime(Clock.System.now(), timeZone)
        _stockLog.value = used.map { (type, count) ->
            StockLogEntry(time, "${type.label} $count 사용", "${current.customerId} ${current.maskedName}")
        } + _stockLog.value
        return true
    }
}
