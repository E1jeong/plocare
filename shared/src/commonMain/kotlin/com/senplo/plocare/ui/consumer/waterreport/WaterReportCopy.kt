package com.senplo.plocare.ui.consumer.waterreport

import com.senplo.plocare.domain.filter.PricingPolicy
import com.senplo.plocare.domain.filter.SavingsEstimate
import com.senplo.plocare.domain.filter.SavingsReportMode
import com.senplo.plocare.domain.filter.formatWaterReportDecimal
import com.senplo.plocare.ui.consumer.dashboard.formatIntWithComma
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.monthsUntil

/** Figma `4.8L`: always one decimal place (`5.0L`). */
internal fun litersLabel(value: Double): String =
    formatWaterReportDecimal(value).let { if ('.' in it) "${it}L" else "$it.0L" }

/** Figma `1,497L`. */

internal fun lifetimeLitersLabel(value: Double): String = "${formatIntWithComma(value.roundToInt())}L"

// Developer-provisional rule (UI-07): Figma shows only `설치 후 1년`.
internal fun installedForLabel(installedOn: LocalDate, today: LocalDate): String {
    val months = installedOn.monthsUntil(today)
    return when {
        months >= 12 -> "설치 후 ${months / 12}년"
        months >= 1 -> "설치 후 ${months}개월"
        else -> "설치 후 ${installedOn.daysUntil(today).coerceAtLeast(0)}일"
    }
}

internal fun wonLabel(amountKrw: Int): String = "${formatIntWithComma(amountKrw)}원"

/** `+48,000원` / `-51,500원` / `0원`. */
internal fun signedWonLabel(amountKrw: Int): String = when {
    amountKrw > 0 -> "+${wonLabel(amountKrw)}"
    amountKrw < 0 -> "-${wonLabel(abs(amountKrw))}"
    else -> wonLabel(0)
}

/**
 * Note box under the savings card (79:3889 / 94:4714). Profit compares with re-renting for the same
 * months; the hygiene-value state reuses the loss presentation (2026-11-20 user decision).
 */
internal fun savingsNote(estimate: SavingsEstimate, pricing: PricingPolicy): String? = when (estimate.mode) {
    SavingsReportMode.PROFIT -> {
        val rentalKrw = pricing.annualRentalFeeKrw * estimate.elapsedMonths / 12
        val lessKrw = rentalKrw - estimate.ploCareTotalCostKrw
        if (lessKrw > 0) {
            "새 정수기 재렌탈(연 ${wonLabel(pricing.annualRentalFeeKrw)}) 대비로는 ${wonLabel(lessKrw)} 덜 냈어요 • 단순 비교 아님"
        } else {
            null
        }
    }
    SavingsReportMode.BREAK_EVEN_PENDING -> {
        val remaining = estimate.remainingMonthsToBreakEven
        val breakEven = estimate.estimatedBreakEvenMonth
        if (remaining != null && breakEven != null) {
            "아직은 모듈값이 더 커요 • 약 ${remaining}개월 뒤(설치 ${breakEven}개월 차)부터 이득으로 바뀌어요"
        } else {
            "아직은 모듈값이 더 커요"
        }
    }
    // Developer-provisional copy (UI-07).
    SavingsReportMode.HYGIENE_VALUE -> "사용량이 많아 아직 이득으로 바뀌는 시점을 예상할 수 없어요"
}
