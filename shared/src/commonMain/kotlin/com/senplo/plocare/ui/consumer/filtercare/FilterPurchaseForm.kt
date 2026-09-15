package com.senplo.plocare.ui.consumer.filtercare

internal const val FILTER_UNIT_PRICE_KRW = 24_000
internal const val SUBSCRIPTION_DISCOUNT_PERCENT = 10

internal enum class FilterSupplyMode(val label: String) {
    ONE_TIME("단건 구매"),
    SUBSCRIPTION("정기 구독"),
}

internal data class FilterPurchaseDraft(
    val contact: String,
    val address: String,
    val selectedFilterIds: Set<String>,
    val mode: FilterSupplyMode?,
)

internal fun purchaseSubtotalKrw(filterCount: Int, mode: FilterSupplyMode): Int {
    val base = filterCount * FILTER_UNIT_PRICE_KRW
    return when (mode) {
        FilterSupplyMode.ONE_TIME -> base
        FilterSupplyMode.SUBSCRIPTION -> base * (100 - SUBSCRIPTION_DISCOUNT_PERCENT) / 100
    }
}

internal fun formatKrw(amount: Int): String {
    val sign = if (amount < 0) "-" else ""
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "$sign${grouped}원"
}

internal fun validateFilterPurchase(draft: FilterPurchaseDraft): String? {
    val phoneDigits = draft.contact.filter { it.isDigit() }
    return when {
        draft.address.trim().isEmpty() -> "배송 주소를 입력해 주세요."
        phoneDigits.length < 10 -> "연락처를 입력해 주세요."
        draft.selectedFilterIds.isEmpty() -> "구매할 필터를 선택해 주세요."
        draft.mode == null -> "구매 또는 구독을 선택해 주세요."
        else -> null
    }
}

internal fun purchaseConfirmationTitle(mode: FilterSupplyMode): String = when (mode) {
    FilterSupplyMode.ONE_TIME -> "구매 내용을 확인했습니다."
    FilterSupplyMode.SUBSCRIPTION -> "구독 내용을 확인했습니다."
}

internal fun purchaseConfirmationDetail(mode: FilterSupplyMode): String = when (mode) {
    FilterSupplyMode.ONE_TIME ->
        "단건 구매 미리보기입니다. 결제와 배송은 연결되지 않았습니다. 서버에 저장되지 않습니다."
    FilterSupplyMode.SUBSCRIPTION ->
        "정기 구독 미리보기입니다. 자동 결제와 배송은 연결되지 않았습니다. 서버에 저장되지 않습니다."
}
