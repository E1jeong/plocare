package com.senplo.plocare.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareButtonSize
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.ic_back

/** No Figma frame exists for this screen; it follows the login screen and shared components. */
@Composable
fun PartnerSignupScreen(
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    fun submitSignupRequest() {
        when {
            name.trim().isEmpty() ||
                email.trim().isEmpty() ||
                phone.trim().isEmpty() ||
                password.isEmpty() ||
                region.trim().isEmpty() -> {
                formError = "모든 항목을 입력해 주세요."
            }
            else -> {
                formError = null
                submitted = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Same back + title row as PloCareWizardHeader.
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .clickable(onClickLabel = "로그인으로 돌아가기", role = Role.Button, onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_back),
                    contentDescription = "뒤로",
                    tint = colors.TextMain,
                )
                Text(
                    text = "파트너 가입 요청",
                    style = typography.titleLarge,
                    color = colors.TextMain,
                )
            }

            if (submitted) {
                Spacer(Modifier.height(120.dp))
                Text(
                    text = "가입 요청 미리보기",
                    color = colors.Black70,
                    style = typography.titleMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "서버에 접수되지 않았습니다. 관리자 승인 기능은 준비 중입니다.",
                    color = colors.Black60Body,
                    style = typography.bodyMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
                PloCareButton(
                    text = "로그인으로",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    size = PloCareButtonSize.Large,
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "가입 요청 화면 미리보기 · 서버 접수는 아직 지원하지 않습니다.",
                    color = colors.Black60Body,
                    style = typography.bodyMid,
                )
                Spacer(Modifier.height(24.dp))
                AuthFormField(
                    value = name,
                    onValueChange = {
                        name = it
                        formError = null
                    },
                    label = "이름",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(16.dp))
                AuthFormField(
                    value = email,
                    onValueChange = {
                        email = it
                        formError = null
                    },
                    label = "이메일",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                AuthFormField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        formError = null
                    },
                    label = "연락처",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                AuthFormField(
                    value = password,
                    onValueChange = {
                        password = it
                        formError = null
                    },
                    label = "비밀번호",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingLabel = if (passwordVisible) "숨김" else "표시",
                    onTrailingClick = { passwordVisible = !passwordVisible },
                )
                Spacer(Modifier.height(16.dp))
                AuthFormField(
                    value = region,
                    onValueChange = {
                        region = it
                        formError = null
                    },
                    label = "소속 / 활동 지역",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { submitSignupRequest() },
                    ),
                )
                if (formError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = formError.orEmpty(),
                        color = colors.StatusAlert,
                        style = typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(24.dp))
                PloCareButton(
                    text = "가입 요청",
                    onClick = { submitSignupRequest() },
                    modifier = Modifier.fillMaxWidth(),
                    size = PloCareButtonSize.Large,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "현재 입력 내용은 서버에 제출되지 않습니다.",
                    color = colors.Black50Muted,
                    style = typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
