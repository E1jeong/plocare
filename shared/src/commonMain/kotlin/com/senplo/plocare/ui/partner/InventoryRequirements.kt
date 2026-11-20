package com.senplo.plocare.ui.partner

import com.senplo.plocare.domain.filter.FilterType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** One vehicle stock line (Figma 18 `필터 관리` row). Quantities stay on this device only. */
internal data class StockItem(
    val id: String,
    val name: String,
    val type: FilterType,
    val quantity: Int,
    /** Set for filters added through the `필터 추가` modal; the four default lines have none. */
    val maker: String? = null,
)

/** One `최근 입출고` row (Figma 146:3098): `10/30 09:40 · 세디먼트 1 사용 · C-3072 이*수`. */
internal data class StockLogEntry(
    val timeLabel: String,
    val text: String,
    val source: String,
    /** Manual edits merge into the newest entry of the same item while it stays on top. */
    val itemId: String? = null,
    val delta: Int = 0,
)

/** Figma 18 lists the four filter types in the filter-color order. */
internal val defaultStockTypes: List<FilterType> = listOf(
    FilterType.SEDIMENT,
    FilterType.PRE_CARBON,
    FilterType.POST_CARBON,
    FilterType.MEMBRANE,
)

internal fun defaultStockItems(quantities: Map<FilterType, Int>): List<StockItem> =
    defaultStockTypes.map { type ->
        StockItem(id = "stock-${type.name.lowercase()}", name = type.label, type = type, quantity = quantities[type] ?: 0)
    }

/** Filters needed by today's open visits, counted per filter type of each target filter. */
internal fun inventoryRequirements(visits: List<PartnerVisitView>): Map<FilterType, Int> =
    visits.asSequence()
        .filterNot { it.completed }
        .flatMap { view ->
            val targets = view.visit.targetFilterIds.toSet()
            view.snapshot?.filters.orEmpty().asSequence()
                .filter { it.id in targets }
                .map { it.filterType }
        }
        .groupingBy { it }
        .eachCount()

internal fun stockByType(items: List<StockItem>): Map<FilterType, Int> =
    items.groupBy { it.type }.mapValues { (_, lines) -> lines.sumOf { it.quantity } }

/** Figma 146:3098 shortage card: the first open visit, in visit order, that needs a short filter type. */
internal data class StockShortage(
    val type: FilterType,
    val missing: Int,
    val visit: PartnerVisitView?,
)

internal fun stockShortages(visits: List<PartnerVisitView>, items: List<StockItem>): List<StockShortage> {
    val required = inventoryRequirements(visits)
    val stocked = stockByType(items)
    return defaultStockTypes.mapNotNull { type ->
        val missing = (required[type] ?: 0) - (stocked[type] ?: 0)
        if (missing <= 0) return@mapNotNull null
        val visit = visits.firstOrNull { view ->
            !view.completed && view.snapshot?.filters.orEmpty().any { it.id in view.visit.targetFilterIds && it.filterType == type }
        }
        StockShortage(type, missing, visit)
    }
}

/** Figma: `UF 1개 부족`. */
internal fun shortageTitle(shortage: StockShortage): String = "${shortage.type.label} ${shortage.missing}개 부족"

/** Figma: `16:30 최*민 방문에 필요해요 • 맞는 업체에서 구매 후 수량을 올려 주세요`. */
internal fun shortageDetail(shortage: StockShortage): String {
    val visit = shortage.visit?.visit
    val lead = if (visit == null) "남은 방문에 필요해요" else "${visit.time} ${visit.maskedName} 방문에 필요해요"
    return "$lead • 맞는 업체에서 구매 후 수량을 올려 주세요"
}

/** Typed quantities: digits only, at most three, empty while editing. */
internal fun parseStockQuantity(text: String): Int? =
    text.filter { it.isDigit() }.take(3).takeIf { it.isNotEmpty() }?.toInt()

internal const val MAX_STOCK_QUANTITY = 999

/** Figma `직접 수정 +4`. */
internal fun manualEditLabel(delta: Int): String = if (delta > 0) "직접 수정 +$delta" else "직접 수정 $delta"

/** Figma `10/30 09:40`. */
internal fun stockLogTime(instant: Instant, timeZone: TimeZone): String = stockLogTime(instant.toLocalDateTime(timeZone))

internal fun stockLogTime(time: LocalDateTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "${time.month.ordinal + 1}/${time.day} $hour:$minute"
}

/** Recent movements before this session, shaped like the Figma rows and dated today and yesterday. */
internal fun sampleStockLog(now: Instant, timeZone: TimeZone): List<StockLogEntry> {
    val today = now.toLocalDateTime(timeZone).date
    fun at(daysAgo: Int, hour: Int, minute: Int): String {
        val date = today.minus(daysAgo, DateTimeUnit.DAY)
        return stockLogTime(LocalDateTime(date, LocalTime(hour, minute)))
    }
    return listOf(
        StockLogEntry(at(0, 9, 40), "멤브레인 1 사용", "C-4096 최*아"),
        StockLogEntry(at(1, 17, 10), manualEditLabel(4), "기사 입력"),
        StockLogEntry(at(1, 11, 20), "프리카본 1 사용", "C-0815 한*우"),
    )
}

/**
 * Sample maker catalog for the `필터 추가` modal until the Master DB v2 is ingested (backlog). Filter names carry
 * the filter type so [FilterType.fromName] can match them to visit requirements.
 */
internal val sampleFilterCatalog: Map<String, List<String>> = mapOf(
    "하나필터주식회사" to listOf("하나 세디먼트", "하나 프리카본", "하나 포스트카본", "하나 UF 멤브레인"),
    "두리정수필터" to listOf("두리 세디먼트", "두리 프리카본", "두리 포스트카본"),
    "세움워터" to listOf("세움 세디먼트", "세움 RO 멤브레인"),
)
