package com.senplo.plocare.ui.consumer.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareChip
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_expand_less
import plocare.shared.generated.resources.ic_expand_more
import plocare.shared.generated.resources.ic_search

/** Figma 13 자주 묻는 질문 (120:1591). Questions are Figma samples; search and chips filter them locally. */
@Composable
internal fun FaqScreen(
    onBack: () -> Unit,
    onOpenInquiry: () -> Unit,
) {
    val colors = PloCareColor
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(FaqCategory.ALL) }
    // Figma shows the first question open.
    var expandedId by rememberSaveable { mutableStateOf<String?>(sampleFaqs.firstOrNull()?.id) }
    val items = filterFaqs(sampleFaqs, category, query)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding(),
    ) {
        SupportHeader(title = "자주 묻는 질문", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SupportSectionTitle("궁금한 내용을 먼저 찾아보세요", modifier = Modifier.padding(horizontal = 16.dp))
                PloCareTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "질문을 입력해보세요",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    trailingContent = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            tint = colors.Primary,
                        )
                    },
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FaqCategory.entries.forEach { item ->
                        PloCareChip(
                            text = item.label,
                            selected = item == category,
                            onClick = { category = item },
                        )
                    }
                }
            }

            // With no match (e.g. `계정`), the inquiry card right below is the fallback.
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                items.forEach { item ->
                    FaqRow(
                        item = item,
                        expanded = item.id == expandedId,
                        onToggle = { expandedId = if (expandedId == item.id) null else item.id },
                    )
                }
            }

            InquiryCard(onClick = onOpenInquiry, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun FaqRow(
    item: FaqItem,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val questionStyle = typography.bodyLarge.copy(fontWeight = if (expanded) FontWeight.Bold else FontWeight.Medium)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onToggle)
                .semantics { stateDescription = if (expanded) "펼쳐짐" else "접힘" }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Q", style = questionStyle, color = colors.Primary)
                Text(
                    text = item.question,
                    style = questionStyle,
                    color = if (expanded) colors.Primary else colors.TextMain,
                    modifier = Modifier.weight(1f),
                )
                Text(text = item.category.label, style = typography.labelSmall, color = colors.Black50Muted)
            }
            Icon(
                painter = painterResource(if (expanded) Res.drawable.ic_expand_less else Res.drawable.ic_expand_more),
                contentDescription = null,
                tint = if (expanded) colors.Primary else colors.Black40Ghost,
            )
        }
        FaqDivider()
        if (expanded) {
            Text(
                text = item.answer,
                style = typography.bodyLarge,
                color = colors.TextMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Black0Surface)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            )
            FaqDivider()
        }
    }
}

@Composable
private fun FaqDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PloCareColor.Black20))
}

@Composable
private fun InquiryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.Black0Surface, shape)
            .border(1.dp, colors.Black10, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "원하는 답이 없나요?",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.TextMain,
            )
            Text(text = "1:1 문의하기", style = typography.labelLarge, color = colors.TextMain)
        }
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = colors.Black50Muted,
        )
    }
}
