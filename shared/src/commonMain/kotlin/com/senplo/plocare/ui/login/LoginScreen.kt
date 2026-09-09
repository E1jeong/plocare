package com.senplo.plocare.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.auth.GoogleUser
import com.senplo.plocare.auth.rememberGoogleAuthService
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.google_logo
import plocare.shared.generated.resources.plocare_flow_ring

@Composable
fun LoginScreen(
    audience: AppAudience,
    onAudienceChange: (AppAudience) -> Unit,
    onGoogleLoginSuccess: (GoogleUser) -> Unit,
    onPartnerLoginSuccess: () -> Unit,
    onPartnerSignupClick: () -> Unit,
) {
    var isGoogleLoginInProgress by remember { mutableStateOf(false) }
    var googleLoginError by remember { mutableStateOf<String?>(null) }
    val googleAuthService = rememberGoogleAuthService()
    val coroutineScope = rememberCoroutineScope()

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
            Spacer(Modifier.height(24.dp))
            FlowRingMark()
            Spacer(Modifier.height(24.dp))
            Text(
                text = "PLOCARE",
                color = PloCareColor.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "사용량으로 관리하는 스마트 필터 케어",
                color = PloCareColor.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceDark),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    LoginTabs(
                        selectedTab = audience,
                        onTabSelected = {
                            googleLoginError = null
                            onAudienceChange(it)
                        },
                    )
                    Spacer(Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (audience) {
                            AppAudience.USER -> UserLoginContent(
                                isLoginInProgress = isGoogleLoginInProgress,
                                loginError = googleLoginError,
                                onGoogleLoginClick = {
                                    if (!isGoogleLoginInProgress) {
                                        isGoogleLoginInProgress = true
                                        googleLoginError = null
                                        coroutineScope.launch {
                                            googleAuthService.signIn()
                                                .onSuccess(onGoogleLoginSuccess)
                                                .onFailure { error ->
                                                    googleLoginError = if (
                                                        error.message?.contains("[28444]") == true
                                                    ) {
                                                        "Google 로그인 설정이 아직 활성화되지 않았습니다."
                                                    } else {
                                                        "Google 로그인에 실패했습니다. 다시 시도해 주세요."
                                                    }
                                                }
                                            isGoogleLoginInProgress = false
                                        }
                                    }
                                },
                            )

                            AppAudience.PARTNER -> PartnerLoginContent(
                                onSignupClick = onPartnerSignupClick,
                                onLoginSuccess = onPartnerLoginSuccess,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LoginTabs(
    selectedTab: AppAudience,
    onTabSelected: (AppAudience) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppAudience.entries.forEach { tab ->
            val selected = selectedTab == tab
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(tab) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) PloCareColor.AquaTeal else PloCareColor.SurfaceCard,
                    contentColor = if (selected) PloCareColor.BgDeep else PloCareColor.TextSecondary,
                ),
            ) {
                Text(
                    text = if (tab == AppAudience.USER) "사용자" else "파트너",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun UserLoginContent(
    isLoginInProgress: Boolean,
    loginError: String?,
    onGoogleLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "내 필터 상태를 바로 확인하세요",
            color = PloCareColor.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Google 계정으로 간편하게 시작할 수 있습니다.",
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            onClick = onGoogleLoginClick,
            enabled = !isLoginInProgress,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1F2937),
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isLoginInProgress) "로그인 중..." else "Google로 계속하기",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Icon(
                painter = painterResource(Res.drawable.google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Transparent,
            )
        }
        if (loginError != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = loginError,
                color = PloCareColor.StatusAlert,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PartnerLoginContent(
    onSignupClick: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    fun submitPartnerLogin() {
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isEmpty() || password.isEmpty() -> {
                formError = "이메일과 비밀번호를 입력해 주세요."
            }
            else -> {
                formError = null
                onLoginSuccess()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "현장 파트너로 로그인하세요",
            color = PloCareColor.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "승인된 파트너 계정으로 일정을 확인하세요.",
            color = PloCareColor.TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
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
            value = password,
            onValueChange = {
                password = it
                formError = null
            },
            label = "비밀번호",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submitPartnerLogin() }),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingLabel = if (passwordVisible) "숨김" else "표시",
            onTrailingClick = { passwordVisible = !passwordVisible },
        )
        Spacer(Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            onClick = { submitPartnerLogin() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PloCareColor.AquaTeal,
                contentColor = PloCareColor.BgDeep,
            ),
        ) {
            Text(
                text = "로그인",
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
            )
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Text(
                text = "계정이 없나요? ",
                color = PloCareColor.TextTertiary,
                fontSize = 13.sp,
            )
            Text(
                text = "가입 요청",
                color = PloCareColor.AquaTeal,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSignupClick),
            )
        }
    }
}

@Composable
private fun FlowRingMark() {
    Image(
        painter = painterResource(Res.drawable.plocare_flow_ring),
        contentDescription = "PloCare Flow Ring",
        modifier = Modifier.size(112.dp),
    )
}
