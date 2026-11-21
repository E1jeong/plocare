package com.senplo.plocare.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.components.PloCareToggle
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanMonthDay
import com.senplo.plocare.ui.consumer.dashboard.formatKoreanWeekday
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_add
import plocare.shared.generated.resources.ic_chevron_forward
import plocare.shared.generated.resources.ic_person_edit

// Figma 19 파트너 마이페이지 (148:3477, frame now named `04 마이페이지`).
// Profile, monthly figures, and the settings rows show the Figma sample until partner accounts,
// assignment rules, and their destinations exist; the toggles are local state only.
@Composable
internal fun PartnerMyPageTab(today: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PloCareColor.Background),
    ) {
        // Header (148:3480)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "마이페이지",
                style = PloCareTheme.typography.titleLarge,
                color = PloCareColor.TextMain,
                modifier = Modifier.semantics { heading() },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // 148:3487
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileCard()
                DutyCard(today)
            }
            MonthSection(today)
            // 148:3541
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PartnerSectionHeading("이용 • 설정")
                SettingsCard()
            }
        }
    }
}

@Composable
private fun WhiteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PloCareColor.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

// Profile card (148:3490)
@Composable
private fun ProfileCard() {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    WhiteCard {
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
                    text = "김파트너 기사님",
                    style = typography.bodyLarge,
                    color = colors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "OO센터 소속 • 성동구 / 광진구",
                    style = typography.labelMidMedium,
                    color = colors.Black50Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // person_edit (148:3498): profile editing has no destination yet.
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
    }
}

// Duty and days-off card (148:3509). Figma draws the toggle off beside `오늘 근무중`; the sample starts on duty
// so the label holds. `추가` has no Figma destination yet.
@Composable
private fun DutyCard(today: LocalDate) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var onDuty by rememberSaveable { mutableStateOf(true) }
    WhiteCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "오늘 근무중", style = typography.titleMid, color = colors.TextMain)
                Text(text = "끄면 새 요청이 배정되지 않아요", style = typography.labelMidMedium, color = colors.TextMain)
            }
            PloCareToggle(
                checked = onDuty,
                onCheckedChange = { onDuty = it },
                modifier = Modifier.semantics { contentDescription = "오늘 근무" },
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.Black10)
        // 148:3677
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "휴무일", style = typography.labelMidMedium, color = colors.TextMain)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sampleDaysOff(today).forEach { day -> DayOffChip(dayOffLabel(day)) }
                AddDayOffButton()
            }
        }
    }
}

@Composable
private fun DayOffChip(label: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(PloCareColor.Black0Surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.TextMain)
    }
}

// 148:3663: `#29B8E9` 10% fill, 18dp add glyph, padding 8 / 12.
@Composable
private fun AddDayOffButton() {
    val colors = PloCareColor
    val shape = RoundedCornerShape(8.dp)
    Box(modifier = Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(shape)
                .background(colors.GradientMain.copy(alpha = 0.1f), shape)
                .clickable(role = Role.Button, onClick = {})
                .padding(start = 8.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(text = "추가", style = PloCareTheme.typography.labelMidMedium, color = colors.Primary)
        }
    }
}

// This month (148:3697). The period follows today's month; the figures are the Figma sample.
@Composable
private fun MonthSection(today: LocalDate) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PartnerSectionHeading("이번달")
            PartnerBadge(text = monthRangeLabel(today), textColor = PloCareColor.Primary, fill = PloCareColor.Black10)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PloCareColor.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MonthMetric("방문 완료", "42건", Modifier.weight(1f))
            MonthMetric("필터 교체", "97개", Modifier.weight(1f))
            MonthMetric("확정 지연", "1건", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MonthMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val typography = PloCareTheme.typography
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = typography.labelMidMedium, color = PloCareColor.Black50Muted)
        Text(
            text = value,
            // Figma value: Bold 700, 20/29, -0.02em
            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.4).sp),
            color = PloCareColor.TextMain,
        )
    }
}

// Settings list (148:3544). No row has a destination yet; the alert toggle is local state only.
@Composable
private fun SettingsCard() {
    var alerts by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PloCareColor.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NavigationRow(title = "작업 • 정산 내역", subtitle = "방문별 작업 기록 • 월별 정산")
        ListDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowTexts(title = "알림 설정", subtitle = "새 방문 요청 • 확정 기한 알림", modifier = Modifier.weight(1f))
            PloCareToggle(
                checked = alerts,
                onCheckedChange = { alerts = it },
                modifier = Modifier.semantics { contentDescription = "알림 설정" },
            )
        }
        ListDivider()
        NavigationRow(title = "센터 문의 • 공지", subtitle = "소속 센터에 연락 • 운영 공지")
        ListDivider()
        NavigationRow(title = "1:1 문의 • 약관", subtitle = "전화 대신 문의 → 답변 또는 담당자 전화(안심번호)")
        ListDivider()
        NavigationRow(title = "로그아웃", subtitle = null)
    }
}

@Composable
private fun ListDivider() {
    HorizontalDivider(thickness = 1.dp, color = PloCareColor.Black10)
}

@Composable
private fun RowTexts(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = PloCareTheme.typography.bodyLarge,
            color = PloCareColor.TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(text = subtitle, style = PloCareTheme.typography.labelMidMedium, color = PloCareColor.Black50Muted)
        }
    }
}

/** List row (148:3545): title, optional subtitle, and a trailing 24dp chevron. */
@Composable
private fun NavigationRow(title: String, subtitle: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = {}),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowTexts(
            title = title,
            subtitle = subtitle,
            modifier = Modifier
                .weight(1f)
                .then(if (subtitle == null) Modifier.padding(vertical = 10.dp) else Modifier),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = PloCareColor.Black50Muted,
        )
    }
}

/** Figma shows `11월 8일(일)` twice; the sample uses the next two Sundays instead. */
internal fun sampleDaysOff(today: LocalDate): List<LocalDate> {
    val nextSunday = today.plus(7 - today.dayOfWeek.isoDayNumber % 7, DateTimeUnit.DAY)
    return listOf(nextSunday, nextSunday.plus(7, DateTimeUnit.DAY))
}

internal fun dayOffLabel(date: LocalDate): String = "${formatKoreanMonthDay(date)}(${formatKoreanWeekday(date)})"

/** `M월 1일 ~ 말일` for today's month (Figma's `10월 1일 ~ 30일` sample has the wrong last day). */
internal fun monthRangeLabel(today: LocalDate): String {
    val first = LocalDate(today.year, today.month, 1)
    val last = first.plus(DatePeriod(months = 1)).minus(1, DateTimeUnit.DAY)
    return "${first.month.ordinal + 1}월 1일 ~ ${last.day}일"
}
