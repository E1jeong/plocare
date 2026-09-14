package com.senplo.plocare.ui.partner

internal data class PartnerFilterStatus(
    val stage: Int,
    val name: String,
    val exhaustionPercent: Int,
    val label: String,
)

internal data class PartnerVisitFixture(
    val customerId: String,
    val maskedName: String,
    val address: String,
    val time: String,
    val filtersLabel: String,
    val exhaustion: String,
    val status: String,
    val urgent: Boolean = false,
    val completed: Boolean = false,
    val filterStatuses: List<PartnerFilterStatus>,
) {
    val displayName: String get() = "$maskedName · $customerId"
}

internal val partnerVisits: List<PartnerVisitFixture> = listOf(
    PartnerVisitFixture(
        customerId = "C-1024",
        maskedName = "김*정",
        address = "서울 성동구 성수일로 10",
        time = "10:00",
        filtersLabel = "세디먼트 · 프리카본",
        exhaustion = "최고 소진율 112%",
        status = "긴급",
        urgent = true,
        filterStatuses = listOf(
            PartnerFilterStatus(1, "세디먼트", 112, "즉시 교체"),
            PartnerFilterStatus(2, "프리카본", 93, "묶음 후보"),
            PartnerFilterStatus(3, "RO 멤브레인", 45, "정상"),
            PartnerFilterStatus(4, "포스트카본", 41, "정상"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-2048",
        maskedName = "박*현",
        address = "서울 광진구 아차산로 42",
        time = "14:00",
        filtersLabel = "포스트카본",
        exhaustion = "소진율 88%",
        status = "예정",
        filterStatuses = listOf(
            PartnerFilterStatus(4, "포스트카본", 88, "교체 임박"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-3072",
        maskedName = "이*수",
        address = "서울 성동구 왕십리로 18",
        time = "16:00",
        filtersLabel = "세디먼트",
        exhaustion = "소진율 91%",
        status = "예정",
        filterStatuses = listOf(
            PartnerFilterStatus(1, "세디먼트", 91, "교체 임박"),
        ),
    ),
    PartnerVisitFixture(
        customerId = "C-4096",
        maskedName = "최*아",
        address = "서울 광진구 능동로 7",
        time = "완료",
        filtersLabel = "RO 멤브레인",
        exhaustion = "교체 완료",
        status = "완료",
        completed = true,
        filterStatuses = listOf(
            PartnerFilterStatus(3, "RO 멤브레인", 12, "교체 완료"),
        ),
    ),
)

internal fun findPartnerVisit(customerId: String): PartnerVisitFixture? {
    val needle = customerId.trim()
    if (needle.isEmpty()) return null
    return partnerVisits.find { it.customerId.equals(needle, ignoreCase = true) }
}

internal fun searchPartnerVisits(query: String): List<PartnerVisitFixture> {
    val needle = query.trim()
    return partnerVisits.filter { visit ->
        !visit.completed && (
            needle.isEmpty() ||
                visit.customerId.contains(needle, ignoreCase = true) ||
                visit.maskedName.contains(needle, ignoreCase = true)
            )
    }
}
