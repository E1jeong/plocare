package com.senplo.plocare.ui.consumer.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_page_first
import plocare.shared.generated.resources.ic_page_last
import plocare.shared.generated.resources.ic_page_next
import plocare.shared.generated.resources.ic_page_prev

/**
 * Figma 12 내 문의 (120:1157). Rows and pages are Figma samples until inquiries are stored; there is
 * no detail screen, and the terms/privacy rows have no destination yet.
 */
@Composable
internal fun MyInquiriesScreen(
    onBack: () -> Unit,
    onOpenFaq: () -> Unit,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    val colors = PloCareColor
    var page by rememberSaveable { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .statusBarsPadding(),
    ) {
        SupportHeader(title = "내 문의", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SupportSectionTitle("내가 남긴 문의")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    sampleInquiries.forEachIndexed { index, inquiry ->
                        if (index > 0) SupportDivider()
                        InquiryRow(inquiry)
                    }
                }
                Pagination(
                    page = page,
                    pageCount = SAMPLE_INQUIRY_PAGE_COUNT,
                    onPageChange = { page = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SupportSectionTitle("이용 • 설정")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.White, RoundedCornerShape(12.dp))
                        // Rows keep a 48dp touch height; 7dp + the row's centering keeps the Figma 16dp text inset.
                        .padding(horizontal = 18.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LinkRow(title = "이용약관", onClick = onOpenTerms)
                    SupportDivider()
                    LinkRow(title = "개인정보 처리 방침", onClick = onOpenPrivacy)
                    SupportDivider()
                    LinkRow(title = "자주 묻는 질문", onClick = onOpenFaq)
                }
            }
        }
    }
}

@Composable
private fun InquiryRow(inquiry: InquirySummary) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = inquiry.status,
            style = typography.labelSmall,
            color = colors.Primary,
            modifier = Modifier
                .background(colors.Primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
        Text(
            text = inquiry.title,
            style = typography.labelMidMedium,
            color = colors.TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(text = inquiry.dateLabel, style = typography.labelSmall, color = colors.Black50Muted)
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = PloCareTheme.typography.bodyLarge,
            color = PloCareColor.TextMain,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = PloCareColor.Black50Muted,
        )
    }
}

/**
 * Figma pagination row (120:1157). Figma orders the left arrows `<` `<<`; they are shown as first/previous
 * to mirror the right side. Each slot keeps a 48dp touch height around the 32dp Figma cell.
 */
@Composable
private fun Pagination(
    page: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PloCareColor
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PageArrow(Res.drawable.ic_page_first, "첫 페이지", enabled = page > 1) { onPageChange(1) }
        PageArrow(Res.drawable.ic_page_prev, "이전 페이지", enabled = page > 1) { onPageChange(page - 1) }
        (1..pageCount).forEach { number ->
            val selected = number == page
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .selectable(selected = selected, role = Role.Tab) { onPageChange(number) }
                    .semantics { contentDescription = "${number}페이지" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (selected) colors.White else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = number.toString(),
                        style = PloCareTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = if (selected) colors.Primary else colors.TextMain,
                    )
                }
            }
        }
        PageArrow(Res.drawable.ic_page_next, "다음 페이지", enabled = page < pageCount) { onPageChange(page + 1) }
        PageArrow(Res.drawable.ic_page_last, "마지막 페이지", enabled = page < pageCount) { onPageChange(pageCount) }
    }
}

@Composable
private fun RowScope.PageArrow(
    icon: DrawableResource,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = if (enabled) PloCareColor.TextMain else PloCareColor.Black40Ghost,
        )
    }
}
