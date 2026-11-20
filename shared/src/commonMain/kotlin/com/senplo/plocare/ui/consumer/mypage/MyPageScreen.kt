package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.senplo.plocare.domain.filter.PurifierDevice
import com.senplo.plocare.ui.components.PloCareToggle
import com.senplo.plocare.ui.consumer.dashboard.AddPurifierSheet
import com.senplo.plocare.ui.consumer.dashboard.chipTextStyle
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_add
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_person_edit

// Figma 04 마이페이지 (79:3393)
@Composable
fun MyPageScreen(
    devices: List<PurifierDevice>,
    onOpenDeviceSettings: (deviceId: String) -> Unit = {},
    onOpenContactAddress: () -> Unit = {},
    onBookVisit: () -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var addSheetOpen by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header (79:3396)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "마이페이지",
                style = typography.titleLarge,
                color = colors.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // 79:3403
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionTitle("내 정보 • 구독 • 예약을 한곳에서")
                ProfileCard(onOpenContactAddress = onOpenContactAddress)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SubscriptionCard(modifier = Modifier.weight(1f).fillMaxHeight())
                    VisitCard(
                        onBookVisit = onBookVisit,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            // 79:3421
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle("내 정수기")
                    AddPurifierButton(onClick = { addSheetOpen = true })
                }
                ListCard {
                    devices.forEachIndexed { index, device ->
                        if (index > 0) ListDivider()
                        NavigationRow(
                            title = device.nickname,
                            subtitle = purifierSummary(device),
                            onClick = { onOpenDeviceSettings(device.id) },
                        )
                    }
                }
            }

            // 79:3656
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionTitle("이용 • 설정")
                SettingsCard()
            }
        }
    }

    if (addSheetOpen) {
        AddPurifierSheet(onDismiss = { addSheetOpen = false })
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = PloCareTheme.typography.titleMid,
        color = PloCareColor.TextMain,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun WhiteCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(PloCareColor.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

// Profile card (79:3408). Shows the Figma sample until account data is wired (2026-11-20 user decision).
@Composable
private fun ProfileCard(onOpenContactAddress: () -> Unit) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    WhiteCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.Black0Surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "김",
                    style = typography.titleMid.copy(fontWeight = FontWeight.Bold),
                    color = colors.Primary,
                    textAlign = TextAlign.Center,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "김*정 님",
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Google 계정 kim****@gmail.com",
                    style = typography.labelMidMedium,
                    color = colors.Black50Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // person_edit (79:3568): account editing has no destination yet.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_person_edit),
                    contentDescription = "내 정보 수정",
                    tint = colors.GradientMain,
                )
            }
        }
        // 79:3569
        ActionBar(
            label = "연락처 • 주소 미등록",
            action = "등록하기",
            onClick = onOpenContactAddress,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Figma 79:3569 / 79:3621: `#F6F8FD` bar with a bold link and an 18dp chevron. */
@Composable
private fun ActionBar(
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = PloCareColor
    val style = chipTextStyle()
    Row(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.Black0Surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Text(text = label, style = style, color = colors.Primary, modifier = Modifier.weight(1f, fill = false))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = action, style = style.copy(fontWeight = FontWeight.Bold), color = colors.Primary)
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// Subscription card (79:3590): the Figma sample until a subscription backend exists (NV-07, BP-01).
@Composable
private fun SubscriptionCard(modifier: Modifier = Modifier) {
    WhiteCard(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        CardHeading(label = "정기 구독", value = "이용 중")
        // 79:3629: 38dp row; Figma's 8px line height is its typography anomaly, so the chip style is used.
        Box(modifier = Modifier.height(38.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = "다음 배송 11월 5일", style = chipTextStyle(), color = PloCareColor.Primary)
        }
    }
}

// Visit card (79:3610): Figma has only the empty state, so it is shown as designed.
@Composable
private fun VisitCard(
    onBookVisit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WhiteCard(modifier = modifier) {
        CardHeading(label = "방문 예약", value = "예약 없음")
        ActionBar(action = "방문 예약하기", onClick = onBookVisit, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CardHeading(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.TextMain)
        Text(text = value, style = PloCareTheme.typography.titleMid, color = PloCareColor.TextMain)
    }
}

// 79:3630: borderless text button with an 18dp add glyph.
@Composable
private fun AddPurifierButton(onClick: () -> Unit) {
    val colors = PloCareColor
    Row(
        modifier = Modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ic_add draws the 13dp glyph centered in 24dp; Figma add_2 centers it in 18dp.
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.requiredSize(24.dp),
            )
        }
        Text(text = "정수기 추가", style = chipTextStyle().copy(fontWeight = FontWeight.Bold), color = colors.Primary)
    }
}

/** Figma "Background+Border" list card (79:3642, 79:3664). */
@Composable
private fun ListCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PloCareColor.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun ListDivider() {
    HorizontalDivider(thickness = 1.dp, color = PloCareColor.Black10)
}

@Composable
private fun RowTexts(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = PloCareTheme.typography.bodyLarge,
            color = PloCareColor.TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(text = subtitle, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.Black50Muted)
    }
}

/** List row (283:6603): title, subtitle, and a trailing 24dp chevron. */
@Composable
private fun NavigationRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowTexts(title = title, subtitle = subtitle, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = PloCareColor.Black50Muted,
        )
    }
}

// Settings list (79:3664). Destinations (subscription, history, FAQ, inquiry) do not exist yet and
// the replacement alert toggle is local state only.
@Composable
private fun SettingsCard() {
    var replacementAlert by rememberSaveable { mutableStateOf(false) }
    ListCard {
        NavigationRow(title = "구독 • 결제 관리", subtitle = "결제 수단 • 구독 해지", onClick = {})
        ListDivider()
        NavigationRow(title = "예약 • 교체 이력", subtitle = "방문 예약 • 필터 구매 내역", onClick = {})
        ListDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowTexts(title = "교체 알림", subtitle = "교체가 가까울 때 알림 받기", modifier = Modifier.weight(1f))
            PloCareToggle(
                checked = replacementAlert,
                onCheckedChange = { replacementAlert = it },
                modifier = Modifier.semantics { contentDescription = "교체 알림" },
            )
        }
        ListDivider()
        NavigationRow(title = "자주 묻는 질문", subtitle = "종류별 Q&A • 먼저 찾아보기", onClick = {})
        ListDivider()
        NavigationRow(
            title = "1:1 문의 • 약관",
            subtitle = "전화 대신 문의 → 답변 또는 담당자 전화(안심번호)",
            onClick = {},
        )
    }
}
