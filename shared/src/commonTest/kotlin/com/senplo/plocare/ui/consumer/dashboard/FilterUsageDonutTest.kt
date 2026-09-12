package com.senplo.plocare.ui.consumer.dashboard

import androidx.compose.ui.geometry.Offset
import com.senplo.plocare.domain.filter.FilterColorLevel
import com.senplo.plocare.domain.filter.FilterSnapshot
import com.senplo.plocare.ui.theme.UserPloCareColors
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterUsageDonutTest {
    private val today = LocalDate(2026, 9, 12)

    @Test
    fun splitsEvenlyByFilterCount() {
        assertEquals(emptyList(), donutSliceValues(0))
        assertEquals(listOf(1f), donutSliceValues(1))
        assertEquals(listOf(1f, 1f), donutSliceValues(2))
        assertEquals(List(4) { 1f }, donutSliceValues(4))
        assertEquals(List(5) { 1f }, donutSliceValues(5))
    }

    @Test
    fun leavesVisibleGapExceptForSingleFilter() {
        assertEquals(0f, donutSliceGapDegrees(0))
        assertEquals(0f, donutSliceGapDegrees(1))
        assertEquals(4f, donutSliceGapDegrees(2))
        assertEquals(4f, donutSliceGapDegrees(4))
        assertEquals(86f, donutSliceSweepDegrees(4))
    }

    @Test
    fun remainingArcFollowsFillFraction() {
        assertEquals(0f, donutRemainingSweep(86f, 0f))
        assertEquals(43f, donutRemainingSweep(86f, 0.5f))
        assertEquals(86f, donutRemainingSweep(86f, 1f))
        assertEquals(86f, donutRemainingSweep(86f, 1.4f))
        assertEquals(0f, donutRemainingSweep(86f, -0.2f))
    }

    @Test
    fun calloutLabelIsStageOnly() {
        assertEquals("1단계", donutCalloutLabel(1))
        assertEquals("4단계", donutCalloutLabel(4))
        assertEquals(43f, donutSliceMidAngle(0f, 86f))
        val right = donutCalloutLayout(
            center = Offset(100f, 100f),
            ringRadius = 50f,
            outerRadius = 60f,
            leaderLength = 16f,
            elbowLength = 10f,
            angleDegrees = 0f,
        )
        assertEquals(Offset(150f, 100f), right.dot)
        assertEquals(right.dot, right.lineStart)
        assertTrue(right.rightSide)
        assertTrue(right.lineEnd.x > right.elbow.x)
        val left = donutCalloutLayout(
            center = Offset(100f, 100f),
            ringRadius = 50f,
            outerRadius = 60f,
            leaderLength = 16f,
            elbowLength = 10f,
            angleDegrees = 180f,
        )
        assertFalse(left.rightSide)
        assertTrue(left.lineEnd.x < left.elbow.x)
    }

    @Test
    fun stageColorsAreDistinct() {
        val colors = (1..5).map { UserPloCareColors.filterStageColor(it) }
        assertEquals(5, colors.toSet().size)
        assertEquals(UserPloCareColors.AquaTeal, colors[0])
        assertEquals(UserPloCareColors.VividCyan, colors[1])
        assertEquals(UserPloCareColors.FilterStage3, colors[2])
        assertEquals(UserPloCareColors.FilterStage4, colors[3])
        assertEquals(UserPloCareColors.FilterStage5, colors[4])
    }

    @Test
    fun legendCopyFollowsFilterCount() {
        val filters = List(4) { index -> sampleFilter(id = "f$index", stage = index + 1) }
        assertEquals(4, filters.map(::remainingOverRated).size)
        assertEquals("50 L / 1,500 L", remainingOverRated(filters.first()))
    }

    @Test
    fun centerShowsNearestReplacement() {
        assertEquals("—", donutCenterValue(emptyList()))
        assertEquals(
            "D-12일",
            donutCenterValue(
                listOf(
                    sampleFilter(id = "a", days = 20),
                    sampleFilter(id = "b", days = 12),
                    sampleFilter(id = "c", days = 180),
                ),
            ),
        )
        assertEquals("수명 초과", donutCenterValue(listOf(sampleFilter(days = 0))))
    }

    private fun sampleFilter(
        id: String = "kitchen-1",
        stage: Int = 1,
        remainingL: Double = 50.0,
        rated: Double = 1_500.0,
        days: Int = 12,
    ) = FilterSnapshot(
        id = id,
        stage = stage,
        name = "세디먼트 카본",
        ratedCapacityL = rated,
        baselineL = 0.0,
        usageL = rated - remainingL,
        remainingL = remainingL,
        exhaustionPercent = ((rated - remainingL) / rated) * 100.0,
        remainingDays = days,
        estimatedDate = today,
        excessL = 0.0,
        excessDays = 0,
        colorLevel = FilterColorLevel.REPLACE_SOON,
        showReplacementRequest = true,
    )
}
