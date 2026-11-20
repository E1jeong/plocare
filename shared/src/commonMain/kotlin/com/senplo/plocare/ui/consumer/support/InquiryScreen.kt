package com.senplo.plocare.ui.consumer.support

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareChip
import com.senplo.plocare.ui.components.PloCarePhotoBox
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

/**
 * Figma 12 1:1 문의 남기기 (120:721). UI only: there is no photo picker yet, so the add box does
 * nothing, and sending is not stored (`NV-07`).
 */
@Composable
internal fun InquiryScreen(
    onBack: () -> Unit,
    onOpenMyInquiries: () -> Unit,
    onSubmit: () -> Unit,
    onAddPhoto: () -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var content by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding(),
    ) {
        SupportHeader(title = "1:1 문의 남기기", onBack = onBack) {
            PloCareButton(
                text = "내 문의",
                onClick = onOpenMyInquiries,
                size = PloCareButtonSize.Small,
                variant = PloCareButtonVariant.Secondary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SupportSectionTitle("남겨 주시면 답변을 드리거나,\n담당자가 전화를 드려요")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.Black0Surface, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "전화가 필요한 내용이면 기술• 영업 담당자가 직접 연락드려요.\n고객님 번호와 담당자 번호 모두 안심번호로 연결돼요.",
                        style = typography.labelMidMedium,
                        color = colors.Black50Muted,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SupportSectionTitle("어떤 내용인가요?", modifier = Modifier.padding(horizontal = 16.dp))
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    inquiryCategories.forEach { item ->
                        PloCareChip(
                            text = item,
                            selected = item == category,
                            onClick = { category = item },
                        )
                    }
                }
                PloCareTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = "문의 내용을 입력해주세요",
                    singleLine = false,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                // Attached photos follow the add box and scroll sideways (120:721 shows four boxes).
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PloCarePhotoBox(hasPhoto = false, onAddClick = onAddPhoto)
                }
            }
        }

        // Bottom action, same frame as the contact screen (114:1793)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 24.dp),
        ) {
            PloCareButton(
                text = "문의 남기기",
                onClick = onSubmit,
                size = PloCareButtonSize.Large,
                enabled = canSubmitInquiry(category, content),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
