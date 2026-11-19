package com.senplo.plocare.ui.consumer.filtercare

import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.domain.filter.PricingPolicy
import com.senplo.plocare.domain.filter.SamplePricingPolicy

internal val defaultPricingPolicy: PricingPolicy = SamplePricingPolicy()

internal enum class FilterSupplyMode(val label: String) {
    ONE_TIME("1회 구매"),
    SUBSCRIPTION("정기 구독"),
}

internal fun purchaseSubtotalKrw(
    filterCount: Int,
    mode: FilterSupplyMode,
    pricingPolicy: PricingPolicy = defaultPricingPolicy,
): Int {
    val types = List(filterCount.coerceAtLeast(0)) { FilterType.UNSPECIFIED }
    return pricingPolicy.purchaseSubtotalKrw(
        filterTypes = types,
        isSubscription = mode == FilterSupplyMode.SUBSCRIPTION,
    )
}

internal fun formatKrw(amount: Int): String {
    val sign = if (amount < 0) "-" else ""
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "$sign${grouped}원"
}

/** Figma 98:5885 tab labels; the subscription tab carries the policy discount. */
internal fun supplyModeTabLabel(
    mode: FilterSupplyMode,
    pricingPolicy: PricingPolicy = defaultPricingPolicy,
): String = when (mode) {
    FilterSupplyMode.ONE_TIME -> mode.label
    FilterSupplyMode.SUBSCRIPTION -> "${mode.label} • ${pricingPolicy.subscriptionDiscountPercent}% 할인"
}

internal fun purchaseCtaLabel(mode: FilterSupplyMode): String = when (mode) {
    FilterSupplyMode.ONE_TIME -> "구매하기"
    FilterSupplyMode.SUBSCRIPTION -> "구독 시작하기"
}

internal fun purchaseConfirmationTitle(mode: FilterSupplyMode): String = when (mode) {
    FilterSupplyMode.ONE_TIME -> "구매 내용을 확인했습니다."
    FilterSupplyMode.SUBSCRIPTION -> "구독 내용을 확인했습니다."
}

internal fun purchaseConfirmationDetail(mode: FilterSupplyMode): String = when (mode) {
    FilterSupplyMode.ONE_TIME ->
        "1회 구매 미리보기입니다. 결제와 배송은 연결되지 않았습니다. 서버에 저장되지 않습니다."
    FilterSupplyMode.SUBSCRIPTION ->
        "정기 구독 미리보기입니다. 자동 결제와 배송은 연결되지 않았습니다. 서버에 저장되지 않습니다."
}
