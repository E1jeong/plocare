package com.senplo.plocare.domain.filter

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Configurable pricing policy interface (`BP-01`).
 * Production values must come from administrator/server configuration;
 * [SamplePricingPolicy] provides local preview defaults without claiming server sync.
 */
interface PricingPolicy {
    val modulePriceKrw: Int
    val monthlySubscriptionFeeKrw: Int
    val defaultFilterUnitPriceKrw: Int
    val subscriptionDiscountPercent: Int
    val visitServiceBaseFeeKrw: Int

    fun filterUnitPriceKrw(filterType: FilterType): Int = defaultFilterUnitPriceKrw

    fun purchaseSubtotalKrw(
        filterTypes: List<FilterType>,
        isSubscription: Boolean,
    ): Int {
        val raw = filterTypes.sumOf { filterUnitPriceKrw(it) }
        return if (isSubscription) {
            raw * (100 - subscriptionDiscountPercent.coerceIn(0, 100)) / 100
        } else {
            raw
        }
    }
}

data class SamplePricingPolicy(
    override val modulePriceKrw: Int = 50_000,
    override val monthlySubscriptionFeeKrw: Int = 500,
    override val defaultFilterUnitPriceKrw: Int = 24_000,
    override val subscriptionDiscountPercent: Int = 10,
    override val visitServiceBaseFeeKrw: Int = 15_000,
    private val filterPricesByType: Map<FilterType, Int> = emptyMap(),
) : PricingPolicy {
    override fun filterUnitPriceKrw(filterType: FilterType): Int =
        filterPricesByType[filterType] ?: defaultFilterUnitPriceKrw
}

enum class SavingsReportMode {
    /** Cumulative savings > 0: household is already saving money vs calendar replacement. */
    PROFIT,

    /** Initial loss period (savings <= 0), but projected monthly savings will reach break-even. */
    BREAK_EVEN_PENDING,

    /** Sustained heavy usage keeps PloCare cost >= calendar replacement cost: show hygiene value instead. */
    HYGIENE_VALUE,
}

data class SavingsEstimate(
    val elapsedMonths: Int,
    val scheduledReplacementCostKrw: Int,
    val ploCareTotalCostKrw: Int,
    val netSavingsKrw: Int,
    val estimatedBreakEvenMonth: Int?,
    val remainingMonthsToBreakEven: Int?,
    val mode: SavingsReportMode,
    val isSampleEstimate: Boolean = true,
)

object SavingsCalculator {
    const val MAX_BREAK_EVEN_HORIZON_MONTHS = 84

    /**
     * Computes `Savings(t) = ScheduledReplacementCost(t) - (ModulePrice + SubscriptionFees(t) + ActualFilterCost(t) + OtherIncludedFees(t))`
     * without clamping negative values during the initial loss period (`calculation-models.md` §1.8).
     */
    fun calculate(
        elapsedMonths: Int,
        scheduledMonthlyFilterCostKrw: Double,
        actualMonthlyFilterCostKrw: Double,
        pricingPolicy: PricingPolicy = SamplePricingPolicy(),
        otherIncludedFeesKrw: Int = 0,
    ): SavingsEstimate {
        val months = max(1, elapsedMonths)
        val scheduledCost = (scheduledMonthlyFilterCostKrw * months).roundToInt()
        val subscriptionTotal = pricingPolicy.monthlySubscriptionFeeKrw * months
        val actualFilterTotal = (actualMonthlyFilterCostKrw * months).roundToInt()
        val ploCareTotal =
            pricingPolicy.modulePriceKrw + subscriptionTotal + actualFilterTotal + otherIncludedFeesKrw
        val netSavings = scheduledCost - ploCareTotal

        val monthlyAdvantage =
            scheduledMonthlyFilterCostKrw - (actualMonthlyFilterCostKrw + pricingPolicy.monthlySubscriptionFeeKrw)
        val fixedUpfront = (pricingPolicy.modulePriceKrw + otherIncludedFeesKrw).toDouble()

        val breakEvenMonth: Int? = if (monthlyAdvantage <= 0.0) {
            null
        } else {
            val requiredMonths = ceil(fixedUpfront / monthlyAdvantage).toInt().coerceAtLeast(1)
            if (requiredMonths <= MAX_BREAK_EVEN_HORIZON_MONTHS) requiredMonths else null
        }

        val mode = when {
            breakEvenMonth == null -> SavingsReportMode.HYGIENE_VALUE
            netSavings > 0 -> SavingsReportMode.PROFIT
            else -> SavingsReportMode.BREAK_EVEN_PENDING
        }

        val remainingMonths = breakEvenMonth?.let { max(0, it - months) }

        return SavingsEstimate(
            elapsedMonths = months,
            scheduledReplacementCostKrw = scheduledCost,
            ploCareTotalCostKrw = ploCareTotal,
            netSavingsKrw = netSavings,
            estimatedBreakEvenMonth = breakEvenMonth,
            remainingMonthsToBreakEven = remainingMonths,
            mode = mode,
        )
    }
}
