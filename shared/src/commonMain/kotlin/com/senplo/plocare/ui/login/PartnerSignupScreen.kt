package com.senplo.plocare.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

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
            .background(PloCareColor.BrandNavy),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "← 로그인",
                color = PloCareColor.AquaTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "파트너 가입 요청",
                color = PloCareColor.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "관리자 승인 후 로그인할 수 있습니다.",
                color = PloCareColor.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceDark),
            ) {
                if (submitted) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "가입 요청이 접수되었습니다.",
                            color = PloCareColor.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "관리자 승인 후 파트너 탭에서 로그인해 주세요.",
                            color = PloCareColor.TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PloCareColor.AquaTeal,
                                contentColor = PloCareColor.BgDeep,
                            ),
                        ) {
                            Text(
                                text = "로그인으로",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(20.dp)) {
                        AuthFormField(
                            value = name,
                            onValueChange = {
                                name = it
                                formError = null
                            },
                            label = "이름",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                        Spacer(Modifier.height(12.dp))
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
                        Spacer(Modifier.height(12.dp))
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
                        Spacer(Modifier.height(12.dp))
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
                        Spacer(Modifier.height(12.dp))
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
                        Spacer(Modifier.height(20.dp))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            onClick = { submitSignupRequest() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PloCareColor.AquaTeal,
                                contentColor = PloCareColor.BgDeep,
                            ),
                        ) {
                            Text(
                                text = "가입 요청",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (formError != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = formError.orEmpty(),
                                color = PloCareColor.StatusAlert,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "제출 후에도 승인 전에는 로그인할 수 없습니다.",
                            color = PloCareColor.TextTertiary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
