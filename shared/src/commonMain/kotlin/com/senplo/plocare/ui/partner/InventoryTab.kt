package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.ui.components.PloCareBottomSheet
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_add
import plocare.shared.generated.resources.ic_chevron_down
import plocare.shared.generated.resources.ic_error

/** Annotation 180:1348: the list scrolls once it holds 5 or more filters. */
private const val STOCK_SCROLL_THRESHOLD = 5
private val stockRowHeight = 48.dp

// Figma 18 파트너 재고 관리 (146:3098)
@Composable
internal fun InventoryTab(
    visitViews: List<PartnerVisitView>,
    stock: List<StockItem>,
    stockLog: List<StockLogEntry>,
    onAdjustStock: (itemId: String, delta: Int) -> Unit,
    onSetStock: (itemId: String, quantity: Int) -> Unit,
    onAddStock: (maker: String, filterName: String) -> Boolean,
) {
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val required = inventoryRequirements(visitViews)
    val shortages = stockShortages(visitViews, stock)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.White),
    ) {
        // Header (146:3101); the title follows the tab label `재고 관리` (development-side spacing fix).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "재고 관리",
                style = PloCareTheme.typography.titleLarge,
                color = PloCareColor.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (shortages.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    shortages.forEach { ShortageBanner(it) }
                }
            }
            StockSection(
                stock = stock,
                required = required,
                shortTypes = shortages.map { it.type }.toSet(),
                openVisits = visitViews.count { !it.completed },
                onAdjustStock = onAdjustStock,
                onSetStock = onSetStock,
                onAddFilter = { showAddSheet = true },
            )
            StockLogSection(stockLog)
        }
    }

    if (showAddSheet) {
        AddFilterSheet(
            stock = stock,
            onDismiss = { showAddSheet = false },
            onAdd = { maker, filter -> if (onAddStock(maker, filter)) showAddSheet = false },
        )
    }
}

@Composable
private fun ShortageBanner(shortage: StockShortage) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    // Banner (146:3289)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.StatusAlert.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_error),
                contentDescription = null,
                tint = colors.StatusAlert,
                modifier = Modifier.size(18.dp),
            )
            Text(text = shortageTitle(shortage), style = typography.labelMidMedium, color = colors.StatusAlert)
        }
        Text(text = shortageDetail(shortage), style = typography.labelMidMedium, color = colors.StatusAlert)
    }
}

@Composable
private fun StockSection(
    stock: List<StockItem>,
    required: Map<FilterType, Int>,
    shortTypes: Set<FilterType>,
    openVisits: Int,
    onAdjustStock: (String, Int) -> Unit,
    onSetStock: (String, Int) -> Unit,
    onAddFilter: () -> Unit,
) {
    val colors = PloCareColor
    // 필터 관리 (146:3295)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("필터 관리")
            Text(text = "남은 방문 ${openVisits}건 기준", style = caption13(FontWeight.Medium), color = colors.Primary)
        }
        // Rows are 48dp for the touch targets (Figma 28dp buttons, 16dp gaps); 5+ rows scroll in place.
        val scrolls = stock.size >= STOCK_SCROLL_THRESHOLD
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .then(
                    if (scrolls) {
                        Modifier
                            .heightIn(max = stockRowHeight * (STOCK_SCROLL_THRESHOLD - 0.5f))
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    },
                ),
        ) {
            val firstOfType = stock.groupBy { it.type }.mapValues { (_, lines) -> lines.first().id }
            stock.forEach { item ->
                StockRow(
                    item = item,
                    // `필요 N` belongs to the type, so only its first line shows it.
                    required = if (firstOfType[item.type] == item.id) required[item.type] ?: 0 else null,
                    short = item.type in shortTypes,
                    onAdjust = { delta -> onAdjustStock(item.id, delta) },
                    onSet = { quantity -> onSetStock(item.id, quantity) },
                )
            }
        }
        PloCareButton(
            text = "필터 추가",
            onClick = onAddFilter,
            variant = PloCareButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StockRow(
    item: StockItem,
    required: Int?,
    short: Boolean,
    onAdjust: (Int) -> Unit,
    onSet: (Int) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(stockRowHeight),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(colors.filterTypeColor(item.type), RoundedCornerShape(4.dp)),
            )
            Text(text = item.name, style = typography.labelMidMedium, color = colors.TextMain, maxLines = 1)
            if (required != null) {
                Text(
                    text = "필요 $required",
                    style = typography.labelSmall,
                    color = colors.Black50Muted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperButton(
                enabled = item.quantity > 0,
                description = "${item.name} 1개 빼기",
                onClick = { onAdjust(-1) },
            ) { tint ->
                Box(Modifier.size(width = 10.dp, height = 1.5.dp).background(tint, RoundedCornerShape(1.dp)))
            }
            QuantityField(item = item, short = short, onSet = onSet)
            StepperButton(
                enabled = item.quantity < MAX_STOCK_QUANTITY,
                description = "${item.name} 1개 추가",
                onClick = { onAdjust(1) },
            ) { tint ->
                Icon(painter = painterResource(Res.drawable.ic_add), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Figma 28dp `remove` / `add_2` circle (#F6F8FD) inside a 48dp touch target. */
@Composable
private fun StepperButton(
    enabled: Boolean,
    description: String,
    onClick: () -> Unit,
    glyph: @Composable (tint: Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(PloCareColor.Black0Surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            glyph(if (enabled) PloCareColor.Primary else PloCareColor.Black30)
        }
    }
}

/** Count box widened from Figma's 40dp so three typed digits fit; red when that filter type is short. */
@Composable
private fun QuantityField(item: StockItem, short: Boolean, onSet: (Int) -> Unit) {
    val colors = PloCareColor
    var text by remember(item.id, item.quantity) { mutableStateOf(item.quantity.toString()) }
    BasicTextField(
        value = text,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }.take(3)
            text = digits
            parseStockQuantity(digits)?.let(onSet)
        },
        singleLine = true,
        textStyle = PloCareTheme.typography.labelMidMedium.copy(
            color = if (short) colors.StatusAlert else colors.TextMain,
            textAlign = TextAlign.End,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(52.dp)
            .height(32.dp)
            .background(if (short) colors.StatusAlert.copy(alpha = 0.1f) else colors.Black0Surface, RoundedCornerShape(8.dp))
            .semantics { contentDescription = "${item.name} 수량" },
        decorationBox = { inner ->
            Box(
                modifier = Modifier.padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterEnd,
            ) { inner() }
        },
    )
}

@Composable
private fun StockLogSection(log: List<StockLogEntry>) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(12.dp)
    // 최근 입출고 (146:3123). The 48dp `전체 보기` target stands in for most of the 18dp gap.
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("최근 입출고")
            // 전체 보기 has no Figma destination yet.
            PartnerTextLink("전체 보기", onClick = {})
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.White, shape)
                    .border(1.dp, colors.Black10, shape)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (log.isEmpty()) {
                    Text(text = "아직 입출고 기록이 없어요", style = typography.labelMidMedium, color = colors.Black50Muted)
                }
                log.take(3).forEach { entry ->
                    Row(
                        modifier = Modifier.semantics(mergeDescendants = true) {},
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entry.timeLabel,
                            style = typography.labelMidMedium,
                            color = colors.Black50Muted,
                            modifier = Modifier.width(80.dp),
                        )
                        Text(
                            text = entry.text,
                            style = typography.labelMidMedium,
                            color = colors.TextMain,
                            modifier = Modifier.weight(1f),
                        )
                        Text(text = entry.source, style = typography.labelMidMedium, color = colors.Primary)
                    }
                }
            }
            Text(
                text = "교체 작업을 기록하면 자동으로 줄어요 • 구매한 만큼 +로 올려요",
                style = typography.labelMidMedium,
                color = colors.Black60Body,
            )
        }
    }
}

// ---- 필터 추가 modal (380:2250) ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFilterSheet(
    stock: List<StockItem>,
    onDismiss: () -> Unit,
    onAdd: (maker: String, filter: String) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var maker by rememberSaveable { mutableStateOf<String?>(null) }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val filterOptions = maker?.let { selected ->
        sampleFilterCatalog[selected].orEmpty().filterNot { name -> stock.any { it.maker == selected && it.name == name } }
    }.orEmpty()

    PloCareBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "어떤 필터를 추가할까요?",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = "제조사와 필터를 선택해주세요", style = typography.labelMidMedium, color = colors.GrayChateau)
            }
            SelectField(
                label = "제조사",
                value = maker,
                placeholder = "제조사를 선택해주세요",
                options = sampleFilterCatalog.keys.toList(),
                onSelect = {
                    if (it != maker) filter = null
                    maker = it
                },
            )
            // Annotation 180:1348: the filter field appears once a maker is chosen.
            if (maker != null) {
                SelectField(
                    label = "필터",
                    value = filter,
                    placeholder = if (filterOptions.isEmpty()) "추가할 필터가 없어요" else "필터를 선택해주세요",
                    options = filterOptions,
                    onSelect = { filter = it },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PloCareButton(
                    text = "닫기",
                    onClick = onDismiss,
                    variant = PloCareButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                PloCareButton(
                    text = "필터 추가",
                    onClick = {
                        val selectedMaker = maker
                        val selectedFilter = filter
                        if (selectedMaker != null && selectedFilter != null) onAdd(selectedMaker, selectedFilter)
                    },
                    // Enabled only with both choices (Figma's Filled state shows it enabled with no filter yet).
                    enabled = maker != null && filter != null,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Figma `input field/box` dropdown (180:1309): 48dp #F6F8FD box, `Label/large` value, 24dp arrow. */
@Composable
private fun SelectField(
    label: String,
    value: String?,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = typography.labelMid, color = colors.TextMain)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.Black0Surface)
                    .clickable(enabled = options.isNotEmpty(), role = Role.DropdownList) { expanded = true }
                    .semantics { contentDescription = "$label, ${value ?: placeholder}" }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value ?: placeholder,
                    style = typography.labelLarge,
                    color = if (value == null) colors.Black30 else colors.Black100,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = colors.TextMain,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.White,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, style = typography.labelLarge, color = colors.TextMain) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
