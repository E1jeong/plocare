package com.senplo.plocare.ui.consumer.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareBottomSheet
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.components.PloCareButtonVariant
import com.senplo.plocare.ui.components.PloCareTextField
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_back

// Figma 11 연락처·주소 등록 (114:1721)
@Composable
internal fun ContactAddressScreen(
    onBack: () -> Unit,
    onSave: (ContactAddressDraft) -> Unit,
    initial: ContactAddressDraft = ContactAddressDraft(),
    onSearchAddress: () -> Unit = {},
    onOpenTerms: (ContactConsent) -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var draft by remember { mutableStateOf(initial) }
    val signature = remember { SignatureState() }
    val canSave = draft.hasContactBasics(requireName = false) && draft.hasRequiredAgreement(signature.isSigned)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding(),
    ) {
        // Header (114:1724)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painter = painterResource(Res.drawable.ic_back), contentDescription = "뒤로", tint = colors.TextMain)
                Text(
                    text = "연락처 • 주소 등록",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ContactPhoneField(
                    digits = draft.phoneDigits,
                    onDigitsChange = { draft = draft.copy(phoneDigits = it) },
                    safeNumberBadge = false,
                )
                ContactAddressFields(
                    address = draft.address,
                    onAddressChange = { draft = draft.copy(address = it) },
                    addressDetail = draft.addressDetail,
                    onAddressDetailChange = { draft = draft.copy(addressDetail = it) },
                    onSearchAddress = onSearchAddress,
                )
                ContactNoteField(
                    label = "요청사항",
                    note = draft.note,
                    onNoteChange = { draft = draft.copy(note = it) },
                    showCounter = true,
                )
                ContactConsentRows(
                    draft = draft,
                    onConsentChange = { consent, agreed -> draft = draft.withConsent(consent, agreed) },
                    onOpenTerms = onOpenTerms,
                )
                ContactSignatureField(state = signature, padHeight = 100.dp)
            }
            // 114:1884
            Text(
                text = "필터 관리에서 구매• 예약을 처음 누를 때도 같은 내용을 받아요. 등록한 정보는 이 화면에서 언제든 바꿀 수 있어요",
                style = typography.labelMidMedium,
                color = colors.Black50Muted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Bottom action (114:1793)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 16.dp),
        ) {
            PloCareButton(
                text = "동의하고 저장",
                onClick = { onSave(draft) },
                size = PloCareButtonSize.Large,
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private enum class ContactSheetStep { CONTACT, AGREEMENT }

/** First purchase/visit entry: contact sheet (79:1713), then the agreement sheet (180:1079). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactOnboardingSheet(
    onDismiss: () -> Unit,
    onSave: (ContactAddressDraft) -> Unit,
    initial: ContactAddressDraft = ContactAddressDraft(),
    onSearchAddress: () -> Unit = {},
    onOpenTerms: (ContactConsent) -> Unit = {},
) {
    val colors = PloCareColor
    val typography = PloCareTheme.typography
    var step by remember { mutableStateOf(ContactSheetStep.CONTACT) }
    var draft by remember { mutableStateOf(initial) }
    val signature = remember { SignatureState() }

    PloCareBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val (title, subtitle) = when (step) {
                ContactSheetStep.CONTACT -> "주문•방문에 필요한 정보예요" to "처음 한 번만 받고 마이페이지에 저장돼요"
                ContactSheetStep.AGREEMENT -> "서비스 이용을 위해 동의해주세요" to "원활한 서비스 이용을 위해 꼭 필요한 항목만 받아요"
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = typography.titleLarge,
                    color = colors.TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(text = subtitle, style = typography.labelMidMedium, color = colors.GrayChateau)
            }

            when (step) {
                ContactSheetStep.CONTACT -> Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    ContactField(label = "성함") {
                        PloCareTextField(
                            value = draft.name,
                            onValueChange = { draft = draft.copy(name = it) },
                            placeholder = "예) 홍길동",
                        )
                    }
                    ContactPhoneField(
                        digits = draft.phoneDigits,
                        onDigitsChange = { draft = draft.copy(phoneDigits = it) },
                        safeNumberBadge = true,
                    )
                    ContactAddressFields(
                        address = draft.address,
                        onAddressChange = { draft = draft.copy(address = it) },
                        addressDetail = draft.addressDetail,
                        onAddressDetailChange = { draft = draft.copy(addressDetail = it) },
                        onSearchAddress = onSearchAddress,
                    )
                }

                ContactSheetStep.AGREEMENT -> Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "서비스 이용 동의", style = typography.labelMid, color = colors.TextMain)
                        ContactConsentRows(
                            draft = draft,
                            onConsentChange = { consent, agreed -> draft = draft.withConsent(consent, agreed) },
                            onOpenTerms = onOpenTerms,
                        )
                    }
                    ContactSignatureField(state = signature, padHeight = 120.dp)
                    ContactNoteField(
                        label = "특이사항",
                        note = draft.note,
                        onNoteChange = { draft = draft.copy(note = it) },
                        showCounter = false,
                        optional = true,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (step) {
                    ContactSheetStep.CONTACT -> {
                        PloCareButton(
                            text = "나중에",
                            onClick = onDismiss,
                            variant = PloCareButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                        PloCareButton(
                            text = "다음",
                            onClick = { step = ContactSheetStep.AGREEMENT },
                            enabled = draft.hasContactBasics(requireName = true),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    ContactSheetStep.AGREEMENT -> {
                        PloCareButton(
                            text = "이전",
                            onClick = { step = ContactSheetStep.CONTACT },
                            variant = PloCareButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                        PloCareButton(
                            text = "동의하고 저장",
                            onClick = { onSave(draft) },
                            enabled = draft.hasRequiredAgreement(signature.isSigned),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
