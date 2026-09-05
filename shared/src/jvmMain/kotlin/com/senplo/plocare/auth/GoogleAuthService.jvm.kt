package com.senplo.plocare.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object UnsupportedGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): Result<GoogleUser> =
        Result.failure(UnsupportedOperationException("데스크톱 Google 로그인은 아직 지원하지 않습니다."))

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

@Composable
actual fun rememberGoogleAuthService(): GoogleAuthService = remember { UnsupportedGoogleAuthService }
