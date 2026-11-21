package com.senplo.plocare.ui.partner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.senplo.plocare.map.KakaoMapView
import com.senplo.plocare.map.MapLocation
import com.senplo.plocare.ui.components.PloCareBottomSheet
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareCheckMark
import com.senplo.plocare.ui.components.PloCareCheckboxStyle
import com.senplo.plocare.ui.components.PloCareSegmentedTab
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_call
import plocare.shared.generated.resources.ic_content_copy
import plocare.shared.generated.resources.ic_drag_indicator
import plocare.shared.generated.resources.ic_info

private enum class VisitView(val title: String) { MAP("지도"), LIST("목록") }

// Figma 14 방문관리-지도 (126:864) and 15 방문관리-목록 (127:1567)
@Composable
internal fun VisitManagementTab(
    visitViews: List<PartnerVisitView>,
    requests: List<PartnerVisitRequest>,
    onOpenCustomer: (String) -> Unit,
    onMoveVisit: (customerId: String, offset: Int) -> Unit,
    onConfirmRequest: (requestId: String, optionIndex: Int) -> Boolean,
) {
    var selectedView by rememberSaveable { mutableStateOf(VisitView.MAP) }
    var sheetRequestId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.White),
    ) {
        // Header (56dp, title/large)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "방문 관리",
                style = PloCareTheme.typography.titleLarge,
                color = PloCareColor.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }
        PloCareSegmentedTab(
            items = VisitView.entries,
            selectedItem = selectedView,
            onItemSelected = { selectedView = it },
            label = { it.title },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            when (selectedView) {
                VisitView.MAP -> {
                    MapSection(visitViews)
                    NowVisitingSection(
                        views = visitViews,
                        onReorder = { selectedView = VisitView.LIST },
                        onOpenCustomer = onOpenCustomer,
                    )
                }
                VisitView.LIST -> {
                    if (requests.isNotEmpty()) {
                        PendingSection(requests, onPickDate = { sheetRequestId = it })
                    }
                    TodayOrderSection(visitViews, onOpenCustomer, onMoveVisit)
                }
            }
        }
    }

    val sheetRequest = requests.find { it.id == sheetRequestId }
    if (sheetRequest != null) {
        VisitDateSheet(
            request = sheetRequest,
            onDismiss = { sheetRequestId = null },
            onConfirm = { index ->
                if (onConfirmRequest(sheetRequest.id, index)) sheetRequestId = null
            },
        )
    }
}

// ---- 14 map ----

@Composable
private fun MapSection(views: List<PartnerVisitView>) {
    val colors = PloCareColor
    val mapShape = RoundedCornerShape(12.dp)
    val markers = views.map { view ->
        MapLocation(
            latitude = view.visit.latitude,
            longitude = view.visit.longitude,
            title = visitTitle(view.visit.maskedName, view.customerId),
            id = view.customerId,
            markerColor = visitPinColor(view),
        )
    }
    // Map section (126:883)
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KakaoMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(mapShape),
            markers = markers,
            fallback = { VisitRouteSchematic(views, Modifier.fillMaxWidth().height(160.dp).clip(mapShape)) },
        )
        // Summary row (126:1134)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "오늘 ${views.size}건", style = PloCareTheme.typography.labelLarge, color = colors.Primary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DotChip("완료 ${views.count { it.completed }}건", colors.StatusDone)
                DotChip("긴급 ${views.count { it.urgent && !it.completed }}건", colors.StatusAlert)
            }
        }
    }
}

@Composable
private fun visitPinColor(view: PartnerVisitView): Color = when {
    view.completed -> PloCareColor.MapDone
    view.urgent -> PloCareColor.StatusAlert
    else -> PloCareColor.Primary
}

@Composable
private fun DotChip(text: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(text = text, style = caption13(FontWeight.Normal), color = color)
    }
}

// Figma pin 24×30 teardrop with a 2dp white outline, drawn in its 28×36 export box.
private const val PIN_PATH =
    "M14 2C7.3728 2 2 7.3021 2 13.8421C2 20.1579 8.4 24.8947 14 32C19.6 24.8947 26 20.1579 26 13.8421C26 7.3021 20.6272 2 14 2Z"
private const val PIN_CHECK_PATH =
    "M12.3666 17.7692L8.8103 14.2129L9.52297 13.5001L12.3666 16.3437L18.477 10.2334L19.1896 10.9462L12.3666 17.7692Z"

/**
 * Where the native Kakao map is unavailable (Web, Desktop, iOS, or no app key), draws the day's stops in
 * visit order on a plain surface, using the Figma pin and route styles; the positions follow the visit
 * coordinates but are not a map.
 */
@Composable
private fun VisitRouteSchematic(views: List<PartnerVisitView>, modifier: Modifier) {
    val colors = PloCareColor
    val points = schematicMapPoints(views)
    val currentIndex = views.indexOfFirst { !it.completed }
    BoxWithConstraints(
        modifier = modifier
            .background(colors.Black0Surface)
            .clearAndSetSemantics { contentDescription = "오늘 방문 ${views.size}곳의 순서 미리보기" },
    ) {
        val width = maxWidth
        val height = maxHeight
        // Pins rise above their point, so the projection keeps a pin's height free at the top.
        val left = 24.dp
        val right = 24.dp
        val top = 40.dp
        val bottom = 12.dp
        fun xAt(p: MapPoint) = left + (width - left - right) * p.x
        fun yAt(p: MapPoint) = top + (height - top - bottom) * p.y

        Canvas(Modifier.fillMaxSize()) {
            for (i in 0 until points.lastIndex) {
                val start = Offset(xAt(points[i]).toPx(), yAt(points[i]).toPx())
                val end = Offset(xAt(points[i + 1]).toPx(), yAt(points[i + 1]).toPx())
                val next = views[i + 1]
                when {
                    next.completed -> drawLine(colors.MapDone, start, end, 3.dp.toPx(), StrokeCap.Round)
                    i + 1 == currentIndex -> drawLine(
                        colors.StatusAlert, start, end, 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                    )
                    else -> {
                        drawLine(colors.White, start, end, 3.dp.toPx())
                        drawLine(
                            colors.Primary, start, end, 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                        )
                    }
                }
            }
        }
        views.forEachIndexed { index, view ->
            val point = points[index]
            MapPin(
                color = visitPinColor(view),
                label = if (view.completed) null else (index + 1).toString(),
                modifier = Modifier.offset(x = xAt(point) - 14.dp, y = yAt(point) - 32.dp),
            )
        }
    }
}

@Composable
private fun MapPin(color: Color, label: String?, modifier: Modifier = Modifier) {
    val pin = remember { PathParser().parsePathString(PIN_PATH).toPath() }
    val check = remember { PathParser().parsePathString(PIN_CHECK_PATH).toPath() }
    Box(modifier = modifier.size(width = 28.dp, height = 36.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val scale = size.width / 28f
            drawContext.transform.scale(scale, scale, Offset.Zero)
            drawPath(pin, Color.White, style = Stroke(width = 4f))
            drawPath(pin, color, style = Fill)
            if (label == null) drawPath(check, Color.White)
        }
        if (label != null) {
            Text(
                text = label,
                style = PloCareTheme.typography.labelMidMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .width(24.dp),
            )
        }
    }
}

@Composable
private fun NowVisitingSection(
    views: List<PartnerVisitView>,
    onReorder: () -> Unit,
    onOpenCustomer: (String) -> Unit,
) {
    val current = currentVisit(views) ?: return
    val colors = PloCareColor
    // 지금 방문 (126:1146). The 48dp `순서 바꾸기` target stands in for part of the 24dp top padding.
    Column(
        modifier = Modifier.padding(start = 16.dp, top = 11.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                PartnerSectionHeading("지금 방문")
                currentVisitPosition(views)?.let { position ->
                    Box(
                        modifier = Modifier
                            .background(colors.Primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(text = position, style = PloCareTheme.typography.labelMidMedium, color = colors.Primary)
                    }
                }
            }
            ReorderLink(onClick = onReorder)
        }
        CurrentVisitCard(current, onStart = { onOpenCustomer(current.customerId) })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurrentVisitCard(view: PartnerVisitView, onStart: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val visit = view.visit
    // Card (126:1183)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Black0Surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = visitTitle(visit.maskedName, visit.customerId), style = typography.titleMid, color = colors.TextMain)
            VisitStatusBadge(visitBadgeKind(view))
            Text(
                text = visit.time,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.Primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        // Contact block (127:1307). 복사 and 전화 wait for the feature phase (clipboard, safe-number relay).
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContactRow(
                text = "${visit.address}\n${visit.addressDetail}",
                button = { PartnerSmallButton("복사", Res.drawable.ic_content_copy, onClick = {}) },
            )
            ContactRow(
                text = safeNumberLines(visit),
                button = { PartnerSmallButton("전화", Res.drawable.ic_call, onClick = {}) },
            )
        }
        // Arrival guide (127:1296)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.PrimaryTint.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = arrivalGuide(visit),
                style = typography.labelMidMedium,
                color = colors.Primary,
                modifier = Modifier.weight(1f),
            )
        }
        // Filters + max exhaustion (126:1228)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                visitTargetFilters(view).forEach { (stage, type) -> StageFilterChip(stage, type, showSwatch = false) }
            }
            Text(
                text = maxExhaustionLabel(view),
                style = typography.labelMidMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.Primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 길 안내 waits for the Kakao Navi intent (backlog).
            PloCareButton(
                text = "길 안내",
                onClick = {},
                variant = PloCareButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            PloCareButton(text = "방문 시작", onClick = onStart, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ContactRow(text: String, button: @Composable () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = PloCareTheme.typography.labelMidMedium,
            color = PloCareColor.Black70,
            modifier = Modifier.weight(1f),
        )
        button()
    }
}

// ---- 15 list ----

@Composable
private fun PendingSection(requests: List<PartnerVisitRequest>, onPickDate: (String) -> Unit) {
    val colors = PloCareColor
    // 확정 대기 (127:1610)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("확정 대기")
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .background(colors.Primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = requests.size.toString(), style = PloCareTheme.typography.labelMidMedium, color = colors.Primary)
            }
            Text(
                text = confirmDeadlineLabel(requests).orEmpty(),
                style = PloCareTheme.typography.labelMidMedium,
                color = colors.Primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        requests.forEach { request -> PendingRequestCard(request, onPickDate = { onPickDate(request.id) }) }
    }
}

@Composable
private fun PendingRequestCard(request: PartnerVisitRequest, onPickDate: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    // Card (127:1620)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(shape, Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.05f), offset = DpOffset(0.dp, 4.dp)))
            .background(colors.White, shape)
            .border(1.dp, colors.Black10, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = visitTitle(request.maskedName, request.customerId), style = typography.titleMid, color = colors.TextMain)
            PartnerBadge("새 요청", colors.Primary, colors.Primary.copy(alpha = 0.1f))
            Text(
                text = "자동 배정",
                style = typography.labelMidMedium,
                color = colors.Primary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = visitFullAddress(request.address, request.addressDetail),
                style = typography.labelMidMedium,
                color = colors.Black70,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                request.filters.forEach { (stage, type) -> StageFilterChip(stage, type, showSwatch = true) }
            }
        }
        // Customer's three preferred dates (127:1750)
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            request.options.forEach { option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.Black0Surface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = shortDateOptionLabel(option),
                        style = typography.labelMidMedium,
                        color = colors.TextMain,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 다른 날짜 제안 has no Figma destination yet.
            PloCareButton(
                text = "다른 날짜 제안",
                onClick = {},
                variant = PloCareButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            PloCareButton(text = "날짜 고르기", onClick = onPickDate, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TodayOrderSection(
    views: List<PartnerVisitView>,
    onOpenCustomer: (String) -> Unit,
    onMoveVisit: (String, Int) -> Unit,
) {
    val colors = PloCareColor
    // 오늘 방문 순서 (127:1755)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("오늘 방문 순서")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_drag_indicator),
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(text = "끌어서 순서 변경", style = PloCareTheme.typography.labelMidMedium, color = colors.Primary)
            }
        }
        ReorderableVisitList(views, onOpenCustomer, onMoveVisit)
    }
}

/**
 * Today's visits with drag-to-reorder: drag the handle, or long-press anywhere on a card. Screen readers get
 * `위로 이동` / `아래로 이동` actions. Completed visits stay put and cannot be passed (PartnerSession.moveVisit).
 */
@Composable
private fun ReorderableVisitList(
    views: List<PartnerVisitView>,
    onOpenCustomer: (String) -> Unit,
    onMoveVisit: (String, Int) -> Unit,
) {
    val heights = remember { mutableStateMapOf<String, Int>() }
    val tops = remember { mutableStateMapOf<String, Float>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    // The dragged card follows the finger from where it started; its layout slot moves as visits swap.
    var dragStartTop by remember { mutableFloatStateOf(0f) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    // The order list a swap was made from; no further swap until the new order has arrived.
    var swappedFrom by remember { mutableStateOf<List<PartnerVisitView>?>(null) }
    val currentViews by rememberUpdatedState(views)
    val move by rememberUpdatedState(onMoveVisit)
    val currentId = currentVisit(views)?.customerId

    fun canMove(id: String, offset: Int): Boolean {
        val list = currentViews
        val index = list.indexOfFirst { it.customerId == id }
        val target = list.getOrNull(index + offset) ?: return false
        return index >= 0 && !list[index].completed && !target.completed
    }

    fun centerOf(id: String): Float = (tops[id] ?: 0f) + (heights[id] ?: 0) / 2f

    fun startDrag(id: String) {
        draggingId = id
        dragStartTop = tops[id] ?: 0f
        dragTotal = 0f
        swappedFrom = null
    }

    fun endDrag() {
        draggingId = null
        dragTotal = 0f
    }

    // Swaps once the dragged card's center passes a neighbor's center, measured from the actual layout so
    // cards that change height (the current visit gains `방문 시작`) do not make the order bounce back.
    fun dragBy(id: String, dy: Float) {
        dragTotal += dy
        val list = currentViews
        if (swappedFrom === list) return
        val index = list.indexOfFirst { it.customerId == id }
        if (index < 0) return
        val center = dragStartTop + dragTotal + (heights[id] ?: 0) / 2f
        val previous = list.getOrNull(index - 1)
        val next = list.getOrNull(index + 1)
        val offset = when {
            previous != null && canMove(id, -1) && center < centerOf(previous.customerId) -> -1
            next != null && canMove(id, 1) && center > centerOf(next.customerId) -> 1
            else -> 0
        }
        if (offset != 0) {
            swappedFrom = list
            move(id, offset)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        views.forEachIndexed { index, view ->
            key(view.customerId) {
                val id = view.customerId
                val dragging = draggingId == id
                val movable = !view.completed
                val dragGesture: Modifier = if (movable) {
                    Modifier.pointerInput(id) {
                        detectDragGestures(
                            onDragStart = { startDrag(id) },
                            onDragEnd = { endDrag() },
                            onDragCancel = { endDrag() },
                            onDrag = { change, amount -> change.consume(); dragBy(id, amount.y) },
                        )
                    }
                } else {
                    Modifier
                }
                val longPressGesture: Modifier = if (movable) {
                    Modifier.pointerInput(id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startDrag(id) },
                            onDragEnd = { endDrag() },
                            onDragCancel = { endDrag() },
                            onDrag = { change, amount -> change.consume(); dragBy(id, amount.y) },
                        )
                    }
                } else {
                    Modifier
                }
                OrderCard(
                    order = index + 1,
                    view = view,
                    current = id == currentId,
                    onStart = { onOpenCustomer(id) },
                    handleModifier = dragGesture,
                    modifier = Modifier
                        // Measured outside the graphics layer, so the drag translation is not included.
                        .onPlaced { tops[id] = it.positionInParent().y }
                        .onSizeChanged { heights[id] = it.height }
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (dragging) dragStartTop + dragTotal - (tops[id] ?: dragStartTop) else 0f
                            shadowElevation = if (dragging) 8.dp.toPx() else 0f
                            shape = RoundedCornerShape(12.dp)
                        }
                        .then(longPressGesture)
                        .semantics {
                            if (movable) {
                                customActions = listOfNotNull(
                                    CustomAccessibilityAction("위로 이동") { move(id, -1); true }
                                        .takeIf { canMove(id, -1) },
                                    CustomAccessibilityAction("아래로 이동") { move(id, 1); true }
                                        .takeIf { canMove(id, 1) },
                                )
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Int,
    view: PartnerVisitView,
    current: Boolean,
    onStart: () -> Unit,
    handleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val done = view.completed
    val shape = RoundedCornerShape(12.dp)
    val visit = view.visit
    // Cards 127:1762 (completed), 127:1804 (current), 127:1815
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (done) colors.Black0Surface else colors.White, shape)
            .border(1.dp, colors.Black10, shape)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_drag_indicator),
                contentDescription = null,
                tint = if (done) colors.Black20 else colors.Primary,
                modifier = Modifier.size(24.dp).then(handleModifier),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (done) colors.Black10 else colors.Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = order.toString(),
                            style = typography.labelMidMedium,
                            color = if (done) colors.Black40Ghost else colors.Primary,
                        )
                    }
                    Text(
                        text = visitTitle(visit.maskedName, visit.customerId),
                        style = typography.titleMid,
                        color = if (done) colors.Black50Muted else colors.TextMain,
                    )
                    VisitStatusBadge(visitBadgeKind(view), doneFill = colors.Black10)
                }
                Text(
                    text = visitFullAddress(visit.address, visit.addressDetail),
                    style = typography.labelMidMedium,
                    color = if (done) colors.Black50Muted else colors.Black70,
                )
                Text(
                    text = "방문 시간 ${visit.time}",
                    style = typography.labelMidMedium,
                    color = if (done) colors.Black40Ghost else colors.Primary,
                )
            }
        }
        if (current) {
            PloCareButton(text = "방문 시작", onClick = onStart, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---- 127:1869 date sheet ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDateSheet(
    request: PartnerVisitRequest,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var selected by rememberSaveable(request.id) { mutableStateOf(defaultDateOption(request)) }

    PloCareBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "방문 날짜 고르기",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = dateSheetSubtitle(request), style = typography.labelMidMedium, color = colors.GrayChateau)
            }
            Column(modifier = Modifier.selectableGroup()) {
                request.options.forEachIndexed { index, option ->
                    val isSelected = selected == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = isSelected,
                                enabled = option.available,
                                role = Role.RadioButton,
                                onClick = { selected = index },
                            )
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateOptionLabel(option),
                                    style = typography.labelLarge,
                                    color = if (option.available) colors.TextMain else colors.Black40Ghost,
                                )
                                if (!option.available) {
                                    PartnerBadge("선택불가", colors.Black40Ghost, colors.Black0Surface)
                                }
                            }
                            Text(
                                text = dateOptionDetail(option),
                                style = typography.labelMidMedium,
                                color = if (option.available) colors.Black50Muted else colors.Black30,
                            )
                        }
                        PloCareCheckMark(
                            checked = isSelected,
                            uncheckedStyle = if (option.available) {
                                PloCareCheckboxStyle.OutlineEmpty
                            } else {
                                PloCareCheckboxStyle.SubtleCheck
                            },
                            shape = CircleShape,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 다른 날짜 제안 has no Figma destination yet.
                PloCareButton(
                    text = "다른 날짜 제안",
                    onClick = {},
                    variant = PloCareButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                PloCareButton(
                    text = "날짜 확정",
                    onClick = { selected?.let(onConfirm) },
                    enabled = selected != null,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
