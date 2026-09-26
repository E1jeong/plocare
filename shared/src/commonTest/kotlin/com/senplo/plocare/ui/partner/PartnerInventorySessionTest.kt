package com.senplo.plocare.ui.partner

import androidx.lifecycle.SavedStateHandle
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class PartnerInventorySessionTest {
    @Test
    fun stockChangesRemainInSessionAndResetConfirmation() {
        val session = PartnerSession(
            SavedStateHandle(),
            TimeZone.of("Asia/Seoul"),
            Instant.parse("2026-09-10T03:00:00Z"),
        )
        session.toggleLoadedConfirmation()
        assertTrue(session.loadedConfirmed.value)

        session.adjustStock(1, -1)
        assertEquals(7, session.loadedStock.value[1])
        assertFalse(session.loadedConfirmed.value)
        session.adjustStock(1, -100)
        assertEquals(0, session.loadedStock.value[1])
        session.toggleLoadedConfirmation()
        assertFalse(session.loadedConfirmed.value)
    }

    @Test
    fun completedWorkConsumesSelectedStockAndClearsConfirmation() {
        val session = PartnerSession(
            SavedStateHandle(),
            TimeZone.of("Asia/Seoul"),
            Instant.parse("2026-09-10T03:00:00Z"),
        )
        session.toggleLoadedConfirmation()
        assertTrue(session.loadedConfirmed.value)

        assertTrue(session.completeWork("C-1024", workDraft()))

        assertEquals(7, session.loadedStock.value[1])
        assertEquals(4, session.loadedStock.value[2])
        assertFalse(session.loadedConfirmed.value)
        assertFalse(session.completeWork("C-1024", workDraft()))
        assertEquals(7, session.loadedStock.value[1])
    }

    private fun workDraft() = WorkInformationDraft(
        selectedFilterIds = setOf("kitchen-1", "kitchen-2"),
        type = ReplacementType.BUNDLE,
        barcode = "",
        note = "",
        sensorClamped = true,
        noLeak = true,
        commsVerified = true,
    )
}
