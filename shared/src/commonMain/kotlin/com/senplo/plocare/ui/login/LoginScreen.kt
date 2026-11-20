package com.senplo.plocare.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.auth.GoogleUser
import com.senplo.plocare.auth.rememberGoogleAuthService
import com.senplo.plocare.auth.showsAppleSignIn
import com.senplo.plocare.ui.components.PloCareButton
import com.senplo.plocare.ui.components.PloCareSegmentedTab
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme
import com.senplo.plocare.ui.theme.rememberBrandFontFamily
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plocare.shared.generated.resources.Res
import plocare.shared.generated.resources.plocare_logo
import plocare.shared.generated.resources.social_apple
import plocare.shared.generated.resources.social_google
import plocare.shared.generated.resources.social_kakao
import plocare.shared.generated.resources.social_naver

// Figma "06 로그인_ver-1" (98:5158): 393x852 frame, 56dp status bar, 280dp content column.
private val CONTENT_WIDTH = 280.dp
private val FIGMA_BODY_HEIGHT = 796.dp
private val FIGMA_TOP_GAP = 120.dp
private val MIN_TOP_GAP = 32.dp
private val BRAND_MARK_HEIGHT = 136.83.dp

private enum class SocialProvider(
    val label: String,
    val icon: DrawableResource,
    val background: Color,
    val iconWidth: Dp,
    val iconHeight: Dp,
) {
    KAKAO("카카오", Res.drawable.social_kakao, Color(0xFFFEE500), 17.dp, 16.dp),
    NAVER("네이버", Res.drawable.social_naver, Color(0xFF03C75A), 16.dp, 16.dp),
    GOOGLE("Google", Res.drawable.social_google, Color(0xFFF6F6F6), 17.dp, 18.dp),
    APPLE("Apple", Res.drawable.social_apple, Color(0xFF333333), 14.dp, 18.dp),
}

@Composable
fun LoginScreen(
    audience: AppAudience,
    onAudienceChange: (AppAudience) -> Unit,
    onGoogleLoginSuccess: (GoogleUser) -> Unit,
    onPartnerLoginSuccess: () -> Unit,
    onPartnerSignupClick: () -> Unit,
) {
    var isGoogleLoginInProgress by remember { mutableStateOf(false) }
    var loginNotice by remember { mutableStateOf<String?>(null) }
    var loginNoticeIsError by remember { mutableStateOf(false) }
    val googleAuthService = rememberGoogleAuthService()
    val coroutineScope = rememberCoroutineScope()
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.White)
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        // Keep the Figma 120dp top gap on a 796dp body and shrink it on shorter screens.
        val topGap = (FIGMA_TOP_GAP - (FIGMA_BODY_HEIGHT - maxHeight).coerceAtLeast(0.dp))
            .coerceAtLeast(MIN_TOP_GAP)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(topGap))
            BrandMark()
            Spacer(Modifier.height(24.dp))
            Text(
                text = "쓴 만큼 보고, 필요할 때 갈아요",
                color = colors.Black70,
                style = typography.titleMid,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            ValueSummary()
            Spacer(Modifier.height(43.dp))
            PloCareSegmentedTab(
                items = AppAudience.entries,
                selectedItem = audience,
                onItemSelected = {
                    loginNotice = null
                    onAudienceChange(it)
                },
                label = { if (it == AppAudience.USER) "사용자" else "파트너(기사)" },
                modifier = Modifier.width(CONTENT_WIDTH),
            )
            Spacer(Modifier.height(24.dp))

            when (audience) {
                AppAudience.USER -> {
                    SocialLoginRow(
                        enabled = !isGoogleLoginInProgress,
                        onProviderClick = { provider ->
                            if (provider != SocialProvider.GOOGLE) {
                                loginNoticeIsError = false
                                loginNotice = "${provider.label} 로그인은 준비 중입니다."
                            } else if (!isGoogleLoginInProgress) {
                                isGoogleLoginInProgress = true
                                loginNotice = null
                                coroutineScope.launch {
                                    googleAuthService.signIn()
                                        .onSuccess(onGoogleLoginSuccess)
                                        .onFailure { error ->
                                            loginNoticeIsError = true
                                            loginNotice = if (
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
                    val notice = if (isGoogleLoginInProgress) "로그인 중..." else loginNotice
                    if (notice != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = notice,
                            color = if (loginNoticeIsError && !isGoogleLoginInProgress) {
                                colors.StatusAlert
                            } else {
                                colors.Black60Body
                            },
                            style = typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }

                AppAudience.PARTNER -> PartnerLoginContent(
                    onSignupClick = onPartnerSignupClick,
                    onLoginSuccess = onPartnerLoginSuccess,
                )
            }

            Spacer(Modifier.height(43.dp))
            Spacer(Modifier.weight(1f))
            Text(
                text = when (audience) {
                    AppAudience.USER -> "시작하면 이용약관과 개인정보 처리방침에 동의하게 돼요\n" +
                        "Apple은 iPhone에서만 • 연락처 • 주소는 구매 • 예약 때 받아요"
                    // No backend yet: say so instead of implying real partner authentication.
                    AppAudience.PARTNER -> "파트너 화면 미리보기예요\n계정 검증은 아직 연결되지 않았어요"
                },
                color = colors.Black50Muted,
                style = typography.bodySmall.copy(lineHeight = 16.8.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Splash brand logo on a primary tile, replacing the Figma top graphic (98:5163) slot. */
@Composable
private fun BrandMark() {
    val colors = PloCareColor
    Column(
        modifier = Modifier.height(BRAND_MARK_HEIGHT),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(colors.Primary, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.plocare_logo),
                contentDescription = null,
                modifier = Modifier.height(56.dp),
            )
        }
        Text(
            text = "PloCare",
            color = colors.Primary,
            style = TextStyle(
                fontFamily = rememberBrandFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
        )
    }
}

@Composable
private fun ValueSummary() {
    val colors = PloCareColor
    Row(
        // Grows past the Figma 72dp only when a large system font wraps the labels.
        modifier = Modifier.width(CONTENT_WIDTH).heightIn(min = 72.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            "D-Day" to "교체일 예측",
            "L" to "실제 사용량",
            "₩" to "아낀 금액",
        ).forEachIndexed { index, (value, label) ->
            if (index > 0) {
                Box(
                    Modifier
                        .size(width = 1.dp, height = 72.dp)
                        .background(colors.Black10),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = value,
                    color = colors.Primary,
                    style = PloCareTheme.typography.titleMid,
                    maxLines = 1,
                )
                Text(
                    text = label,
                    color = colors.Primary,
                    style = PloCareTheme.typography.labelMidMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SocialLoginRow(
    enabled: Boolean,
    onProviderClick: (SocialProvider) -> Unit,
) {
    // Figma 98:5158 spreads four 60dp buttons over the content width; hiding Apple on Android keeps
    // that gap and centers the remaining three.
    val gap = (CONTENT_WIDTH - 60.dp * SocialProvider.entries.size) / (SocialProvider.entries.size - 1)
    Row(
        modifier = Modifier.width(CONTENT_WIDTH),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SocialProvider.entries.filter { it != SocialProvider.APPLE || showsAppleSignIn }.forEach { provider ->
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(provider.background)
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                    ) { onProviderClick(provider) },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(provider.icon),
                    contentDescription = "${provider.label}로 로그인",
                    modifier = Modifier.size(width = provider.iconWidth, height = provider.iconHeight),
                )
            }
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
    val colors = PloCareColor
    val typography = PloCareTheme.typography

    fun submitPartnerLogin() {
        onLoginSuccess()
    }

    Column(
        modifier = Modifier.width(CONTENT_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuthFormField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            showLabel = false,
        )
        Spacer(Modifier.height(12.dp))
        AuthFormField(
            value = password,
            onValueChange = { password = it },
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
            showLabel = false,
        )
        Spacer(Modifier.height(20.dp))
        PloCareButton(
            text = "로그인",
            onClick = { submitPartnerLogin() },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "계정이 없나요? ",
                color = colors.Black50Muted,
                style = typography.bodyMid,
            )
            Text(
                text = "가입 요청",
                color = colors.Primary,
                style = typography.labelMidMedium,
                modifier = Modifier.clickable(role = Role.Button, onClick = onSignupClick),
            )
        }
    }
}
