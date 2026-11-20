package com.senplo.plocare.ui.consumer.support

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportCopyTest {
    @Test
    fun inquiryNeedsCategoryAndContent() {
        assertFalse(canSubmitInquiry(null, "앱이 꺼져요"))
        assertFalse(canSubmitInquiry("기타", "   "))
        assertTrue(canSubmitInquiry("기타", "앱이 꺼져요"))
    }

    @Test
    fun faqFiltersByCategoryAndQuery() {
        assertEquals(sampleFaqs, filterFaqs(sampleFaqs, FaqCategory.ALL, ""))
        assertEquals(
            listOf("disconnected", "battery"),
            filterFaqs(sampleFaqs, FaqCategory.SENSOR_APP, "").map { it.id },
        )
        assertEquals(listOf("schedule"), filterFaqs(sampleFaqs, FaqCategory.ALL, "  예정일 ").map { it.id })
        assertEquals(emptyList(), filterFaqs(sampleFaqs, FaqCategory.VISIT, "배터리"))
        assertEquals(emptyList(), filterFaqs(sampleFaqs, FaqCategory.ACCOUNT, ""))
    }

    @Test
    fun faqSampleLabelsMatchChips() {
        // Figma row labels (`센서•앱`, `필터. 교체`) are unified with the chip labels.
        assertTrue(sampleFaqs.all { it.category != FaqCategory.ALL })
        assertTrue(sampleFaqs.none { it.question.startsWith(" ") })
    }
}
