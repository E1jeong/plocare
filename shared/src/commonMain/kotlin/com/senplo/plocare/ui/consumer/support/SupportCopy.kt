package com.senplo.plocare.ui.consumer.support

/** Inquiry types on the 1:1 inquiry form (120:721). */
internal val inquiryCategories = listOf("센서• 앱 오류", "필터• 교체", "주문• 배송", "방문 예약", "기타")

/** Sending is local until the inquiry backend (`NV-07`) exists. */
internal fun canSubmitInquiry(category: String?, content: String): Boolean =
    category != null && content.isNotBlank()

internal data class InquirySummary(
    val status: String,
    val title: String,
    val dateLabel: String,
)

/** Figma 120:1157 sample rows and page count, shown until inquiries are stored. */
internal val sampleInquiries: List<InquirySummary> =
    List(5) { InquirySummary(status = "답변 완료", title = "구독 배송일 변경 문의", dateLabel = "10/27") }

internal const val SAMPLE_INQUIRY_PAGE_COUNT = 5

internal enum class FaqCategory(val label: String) {
    ALL("전체"),
    ACCOUNT("계정"),
    SENSOR_APP("센서• 앱"),
    FILTER("필터• 교체"),
    PURCHASE("구매• 배송"),
    VISIT("방문"),
}

internal data class FaqItem(
    val id: String,
    val category: FaqCategory,
    val question: String,
    val answer: String,
)

// Figma 120:1591 shows one answer, a note that the tech team registers answers per category; every
// sample item reuses it until real FAQ content exists.
private const val SAMPLE_FAQ_ANSWER = "기술팀이 질문 종류별로 등록해요. 글, 사진, 단계 번호를 넣을 수 있게 해 주세요."

/** Figma 120:1591 sample questions (category labels unified with the chips). */
internal val sampleFaqs: List<FaqItem> = listOf(
    FaqItem("disconnected", FaqCategory.SENSOR_APP, "앱에 연결 끊김이 떠요", SAMPLE_FAQ_ANSWER),
    FaqItem("battery", FaqCategory.SENSOR_APP, "센서는 배터리를 갈아야 하나요?", SAMPLE_FAQ_ANSWER),
    FaqItem("schedule", FaqCategory.FILTER, "교체 예정일은 어떻게 계산되나요?", SAMPLE_FAQ_ANSWER),
    FaqItem("self-replace", FaqCategory.FILTER, "직접 교체한 뒤 무엇을 눌러야 하나요?", SAMPLE_FAQ_ANSWER),
    FaqItem("supplier", FaqCategory.PURCHASE, "필터는 누가 보내 주나요?", SAMPLE_FAQ_ANSWER),
    FaqItem("visit-date", FaqCategory.VISIT, "방문 날짜를 바꾸고 싶어요", SAMPLE_FAQ_ANSWER),
)

internal fun filterFaqs(items: List<FaqItem>, category: FaqCategory, query: String): List<FaqItem> {
    val keyword = query.trim()
    return items.filter { item ->
        (category == FaqCategory.ALL || item.category == category) &&
            (keyword.isEmpty() || item.question.contains(keyword, ignoreCase = true))
    }
}
