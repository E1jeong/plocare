package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.domain.filter.FilterType
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_chevron_forward

// Shared pieces of the Figma Partner screens (126:775).

@Composable
internal fun PartnerSectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        modifier = modifier.semantics { heading() },
    )
}

/** Figma 13/22, -0.03em caption, link, and badge text. */
@Composable
internal fun caption13(weight: FontWeight): TextStyle = PloCareTheme.typography.gnbLabel.copy(
    fontWeight = weight,
    lineHeight = 22.sp,
    letterSpacing = (-0.39).sp,
)

/** Figma status badge: padding 0 6, radius 4, Regular 13 text (`완료` / `긴급` / `일반` / `새 요청` / `선택불가`). */
@Composable
internal fun PartnerBadge(text: String, textColor: Color, fill: Color) {
    Box(
        modifier = Modifier
            .background(fill, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
    ) {
        Text(text = text, style = caption13(FontWeight.Normal), color = textColor)
    }
}

internal enum class VisitBadgeKind(val label: String) {
    DONE("완료"),
    URGENT("긴급"),
    NORMAL("일반"),
}

internal fun visitBadgeKind(view: PartnerVisitView): VisitBadgeKind = when {
    view.completed -> VisitBadgeKind.DONE
    view.urgent -> VisitBadgeKind.URGENT
    else -> VisitBadgeKind.NORMAL
}

@Composable
internal fun VisitStatusBadge(kind: VisitBadgeKind, doneFill: Color = PloCareColor.Black0Surface) {
    val colors = PloCareColor
    when (kind) {
        VisitBadgeKind.DONE -> PartnerBadge(kind.label, colors.Black40Ghost, doneFill)
        VisitBadgeKind.URGENT -> PartnerBadge(kind.label, colors.StatusAlert, colors.StatusAlert.copy(alpha = 0.1f))
        VisitBadgeKind.NORMAL -> PartnerBadge(kind.label, colors.Primary, colors.Primary.copy(alpha = 0.1f))
    }
}

/** Figma `순서 바꾸기 >` link (129:2589, 127:1310). */
@Composable
internal fun ReorderLink(onClick: () -> Unit) = PartnerTextLink("순서 바꾸기", onClick)

/** Figma bold 13 link with an 18dp chevron (`순서 바꾸기 >`, `전체 보기 >`), kept at the 48dp minimum touch size. */
@Composable
internal fun PartnerTextLink(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = caption13(FontWeight.Bold), color = PloCareColor.Primary)
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = PloCareColor.Primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Figma filter chip (126:1228, 127:1620): white, 1px #E5E9F1, radius 8, optional 12dp color swatch. */
@Composable
internal fun StageFilterChip(stage: Int, type: FilterType, showSwatch: Boolean) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .background(colors.White, shape)
            .border(1.dp, colors.Black10, shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSwatch) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(colors.filterTypeColor(type), RoundedCornerShape(4.dp)),
            )
        }
        Text(text = stageFilterLabel(stage, type), style = PloCareTheme.typography.labelMidMedium, color = colors.TextMain)
    }
}

/** Figma plain-frame `button/small` (126:1251): white, 1px #E5E9F1, radius 8, Shadow S, 18dp icon + label/mid. */
@Composable
internal fun PartnerSmallButton(text: String, icon: DrawableResource, onClick: () -> Unit) {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    Box(modifier = Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .dropShadow(shape, Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.03f), offset = DpOffset(0.dp, 4.dp)))
                .clip(shape)
                .background(colors.White, shape)
                .border(1.dp, colors.Black10, shape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.TextMain,
                modifier = Modifier.size(18.dp),
            )
            Text(text = text, style = PloCareTheme.typography.labelMidMedium, color = colors.TextMain)
        }
    }
}
