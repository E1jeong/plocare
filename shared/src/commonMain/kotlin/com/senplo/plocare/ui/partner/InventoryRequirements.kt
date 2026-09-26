package com.senplo.plocare.ui.partner

internal fun inventoryRequirements(visits: List<PartnerVisitView>): Map<Int, Int> =
    visits.asSequence()
        .filterNot { it.completed }
        .flatMap { view ->
            val targets = view.visit.targetFilterIds.toSet()
            view.snapshot?.filters.orEmpty().asSequence()
                .filter { it.id in targets }
                .map { it.stage }
        }
        .groupingBy { it }
        .eachCount()

internal fun canConfirmInventory(required: Map<Int, Int>, loaded: Map<Int, Int>): Boolean =
    required.all { (stage, count) -> (loaded[stage] ?: 0) >= count }
